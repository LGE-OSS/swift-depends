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

import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.infrastructure.parser.extractor.PatternBinding;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;
import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DependencyPatternBinding extends AbstractDependency {
	private static final Logger logger = LogManager.getLogger(DependencyPatternBinding.class);

	public DependencyPatternBinding(String projectName, String keyword) {
		super(projectName, keyword);
	}

	@Override
	protected void makeDependency(ASTComponentNode root) {
		if ( root.getComponent() instanceof Module && root.getComponent().getType() != ComponentType.FILE){
			for (Entry<Integer, ASTMapNode> entry : root.getAstMapNode().getChilds().entrySet()) {
				if (entry.getValue().getData().contains(this.keyword)){
					makePatternBinding(root, entry.getValue());
				}
			}

			if (root.getComponent().getType().equals(ComponentType.FUNCTION) 
					|| root.getComponent().getType().equals(ComponentType.METHOD)
					|| root.getComponent().getType().equals(ComponentType.VARIABLE)){
				makeInternalPatternBinding(root, root.getAstMapNode());
			}
		}

		for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
			makeDependency(entry.getValue());
		} 
	}

	private void makeInternalPatternBinding(ASTComponentNode root, ASTMapNode nodes){
		if(nodes.getData().contains(this.keyword)){
			makePatternBinding(root, nodes);
		}

		for(Entry<Integer, ASTMapNode> entry : nodes.getChilds().entrySet()){
			makeInternalPatternBinding(root, entry.getValue());
		}
	}

	private void makePatternBinding(ASTComponentNode root, ASTMapNode node) {
		try{
			PatternBinding patternBinding = new PatternBinding(this.projectName, node);
			
			if (patternBinding.isVerify()){
				addDependency(root, patternBinding);
			}
		}
		catch(NullPointerException e){
			
		}
		catch(Exception e) {
			logger.warn(root.getComponent().getName());
		}
	}

	private void addDependency(ASTComponentNode root, PatternBinding patternBinding) {
		patternBinding.getBindInfo().forEach((type, target) ->{
			
			if(type.contains(".")) {
				createDependency(type, patternBinding.getLocation(), root.getChilds().get(patternBinding.getName()), DependencyType.INVOCATION);
			}
			else {
				ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(type);

				if (fromMapComponentNode != null){
					root.getChilds().get(patternBinding.getName()).getComponent().addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, patternBinding.getLocation()));
				}
				else{
					Component externalComponent = this.externalComponentManager.makeElement(target);
					root.getChilds().get(patternBinding.getName()).getComponent().addDependency(new Dependency(externalComponent, DependencyType.REFERENCE, patternBinding.getLocation()));
				}
			}
		});
	}
}

