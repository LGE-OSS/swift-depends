/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser.dependency;

import java.util.Map.Entry;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.infrastructure.parser.extractor.PatternOptional;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

public class DependencyPatternOptional extends AbstractDependency{
	public DependencyPatternOptional(String projectName, String keyword) {
		super(projectName, keyword);
	}

	@Override
	protected void makeDependency(ASTComponentNode root) {
		if ( root.getComponent() instanceof Module && root.getComponent().getType() != ComponentType.FILE){
			for (Entry<Integer, ASTMapNode> entry : root.getAstMapNode().getChilds().entrySet()) {
				if (entry.getValue().getData().contains(this.keyword)){
					makePatternOptional(root, entry.getValue());
				}
			}

			if (root.getComponent().getType().equals(ComponentType.FUNCTION) 
					|| root.getComponent().getType().equals(ComponentType.METHOD)
					|| root.getComponent().getType().equals(ComponentType.VARIABLE)){
				makeInternalPatternOptional(root, root.getAstMapNode());
			}
		}

		for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
			makeDependency(entry.getValue());
		} 
	}
	
	private void makeInternalPatternOptional(ASTComponentNode root, ASTMapNode nodes){
		if(nodes.getData().contains(this.keyword)){
			makePatternOptional(root, nodes);
		}

		for(Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()){
			makeInternalPatternOptional(root, entry.getValue());
		}
	}

	private void makePatternOptional(ASTComponentNode parent, ASTMapNode node) {
		PatternOptional patternOptional = new PatternOptional(this.keyword, this.projectName, node);
		
		Component childComponent = new Element(patternOptional.getName(), ComponentType.VARIABLE);
		ASTComponentNode childComponentNode = makeComponent(parent, childComponent, node);
		
		patternOptional.getPattern().forEach( (type, target) -> {
			addDependency(childComponentNode.getComponent(), target, patternOptional.getLocation());
		});
	}
	
	private void addDependency(Component component, String target, String location) {
		ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(target);
		
		if(fromMapComponentNode != null) {
			component.addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, location));
		}
		else {
			Component externalComponent = this.externalComponentManager.makeElement(target);
			component.addDependency(new Dependency(externalComponent, DependencyType.REFERENCE, location));
		}
	}
	
	private ASTComponentNode makeComponent(ASTComponentNode parentComponentNode, Component childComponent, ASTMapNode mapNode) {
		ASTComponentNode childComponentNode;
		if(parentComponentNode.getChilds().get(childComponent.getName()) == null){
			childComponentNode = new ASTComponentNode(childComponent);
			childComponentNode.setAstMapNode(mapNode);
			parentComponentNode.addChild(childComponentNode);
		}else{
			childComponentNode = parentComponentNode.getChilds().get(childComponent.getName());
		}
		
		return childComponentNode;
	}
}
