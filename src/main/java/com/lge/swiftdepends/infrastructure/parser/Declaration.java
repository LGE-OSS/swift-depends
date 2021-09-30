/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.infrastructure.parser.extractor.ASTExtractor;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Declaration {
	private static final Map<String, ComponentType> declarationKeywordMap = new HashMap<>();
	private final String extensionKeyword = "(extension_decl";
    private ASTExtractor extractor = new ASTExtractor();

	private static final Logger logger = LogManager.getLogger(Declaration.class);
	
	public Declaration(){
		declarationKeywordMap.put("(protocol", ComponentType.PROTOCOL);
		declarationKeywordMap.put("(class_decl", ComponentType.CLASS);
		declarationKeywordMap.put("(struct_decl", ComponentType.STRUCT);
		declarationKeywordMap.put("(enum_decl", ComponentType.ENUM);
		declarationKeywordMap.put("(enum_element_decl", ComponentType.VARIABLE);
		declarationKeywordMap.put("(var_decl", ComponentType.VARIABLE);
		declarationKeywordMap.put("(func_decl", ComponentType.FUNCTION);
		declarationKeywordMap.put("(constructor_decl", ComponentType.FUNCTION);
	}

    public void makeDeclaration(ASTMapNode swiftASTMapTree, Map<String, ASTComponentNode> componentNodeMap){
		String currentSourceFile = "";
		
		if(swiftASTMapTree.getKey().contains("source_file")){
			String fileName = "";
			try{
				fileName = extractor.extractSourceFileName(swiftASTMapTree.getData());
			}
			catch (NullPointerException ne){
				logger.warn(swiftASTMapTree.getData());
			}
			currentSourceFile = extractor.extractQuote("\"", swiftASTMapTree.getData());
			Component sourceFileComponent = new Module(fileName, ComponentType.FILE, currentSourceFile);

			ASTComponentNode sourceFileCompNode = new ASTComponentNode(sourceFileComponent);
			sourceFileCompNode.setAstMapNode(swiftASTMapTree);
			componentNodeMap.put(fileName, sourceFileCompNode);

			addDeclarationComponent(swiftASTMapTree, sourceFileCompNode);
		}
    }

    private void addDeclarationComponent(ASTMapNode nodes, ASTComponentNode componentNode){
		if (nodes != null){
			for (Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()) {
				if (isDeclaration(entry.getValue().getData())){
					makeDeclarationComponent(entry.getValue(), componentNode);
				}

				if( entry.getValue().getData().contains(extensionKeyword)){
					try{
						makeExtensionComponent(entry.getValue(), componentNode);
					}
					catch(Exception e){
						logger.error("makeExtensionComponent " + componentNode.getAstMapNode().getData());
					}
				}
			}
		}
	}

	private void makeDeclarationComponent(ASTMapNode nodes, ASTComponentNode componentNode){
		ComponentType componentType = getComponentType(nodes.getData());

		if(componentType == null){
			logger.error("new ComponentType " + nodes.getData());
			return;
		}

		Component childComponent;
		String componentName = extractor.extractQuote("\"", nodes.getData());
		
		if(nodes.getData().contains("(func_decl")) {
			componentName += extractor.extracFunctionProperty(nodes.getData());
		}

		if (childHasDeclaration(nodes)){
			childComponent = new Module(componentName, componentType);
			ASTComponentNode childComponentNode = makeComponent(nodes, componentNode, childComponent);

			addDeclarationComponent(nodes, childComponentNode);
		} 
		else if (underChildHasDeclaration(nodes)){
			childComponent = new Module(componentName, componentType);
			ASTComponentNode childComponentNode = makeComponent(nodes, componentNode, childComponent);

			for (Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()) {
				makeFuncDeclaration(entry.getValue(), childComponentNode);
			}
		}
		else{
			childComponent = new Element(componentName, componentType);
			
			try {
				makeComponent(nodes, componentNode, childComponent);
			}
			catch(UnsupportedOperationException ue) {
				logger.warn("Element cannot have child component " + nodes.getData());
			}
		}
	}

	private void makeFuncDeclaration(ASTMapNode nodes, ASTComponentNode componentNode){
		for (Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()) {
			if (isDeclaration(entry.getValue().getData())){
				makeDeclarationComponent(entry.getValue(), componentNode);
			}
			else if(underChildHasDeclaration(nodes)){
				makeFuncDeclaration(entry.getValue(), componentNode);
			}
		}
	}

	private ASTComponentNode makeComponent(ASTMapNode nodes, ASTComponentNode componentNode, Component childComponent) {
		ASTComponentNode childComponentNode;
		if(componentNode.getChilds().get(childComponent.getName()) == null){
			childComponentNode = new ASTComponentNode(childComponent);
			childComponentNode.setAstMapNode(nodes);
			componentNode.addChild(childComponentNode);
		}else{
			childComponentNode = componentNode.getChilds().get(childComponent.getName());
		}
		
		return childComponentNode;
	}
	
	private void makeExtensionComponent(ASTMapNode nodes, ASTComponentNode componentNode){
		String subStringData = "";
		String componentName = "";
		try{
			subStringData = nodes.getData().substring(nodes.getData().lastIndexOf("] ") + 2, nodes.getData().length());
			componentName = subStringData.split(" ")[0].trim();
		}
		catch(Exception e){
			logger.error(nodes.getData());
			return;
		}

		if(componentNode.getChilds().get(componentName) != null){
			addDeclarationComponent(nodes, componentNode.getChilds().get(componentName));
		}
		else{
			addDeclarationComponent(nodes, componentNode);
		}
	}

    private boolean isDeclaration(String line){
		for( String keyword : declarationKeywordMap.keySet()){
			if(line.contains(keyword)){
				return true;
			}
		}
		return false;
	}

    private boolean childHasDeclaration(ASTMapNode nodes){
		for (Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()) {
			if (isDeclaration(entry.getValue().getData())){
				return true;
			}
		}
		return false;
	}
	
	private boolean underChildHasDeclaration(ASTMapNode nodes){
		for (Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()) {
			if (childHasDeclaration(entry.getValue())){
				return true;
			}

			if(underChildHasDeclaration(entry.getValue())){
				return true;
			}
		}

		return false;
	}
    
    private ComponentType getComponentType(String line){
		for( Entry<String, ComponentType> entry : declarationKeywordMap.entrySet()){
			if(line.contains(entry.getKey())){
				return entry.getValue();
			}
		}
		return null;
	}
}
