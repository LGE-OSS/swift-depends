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

import com.lge.swiftdepends.domain.Module;

import java.util.Map.Entry;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;
import com.lge.swiftdepends.infrastructure.parser.extractor.IFStatement;
import com.lge.swiftdepends.infrastructure.parser.extractor.PatternBinding;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DependencyTopLevelCode extends AbstractDependency {
    private static final Logger logger = LogManager.getLogger(DependencyTopLevelCode.class);
    public DependencyTopLevelCode(String projectName, String keyword) {
        super(projectName, keyword);
    }
    
    private ASTComponentNode getEnumElementComponent(String enumElement, ASTComponentNode fromMapComponentNode) {
    	ASTComponentNode returnComponentNode = fromMapComponentNode;
    	
    	if (enumElement.equals("") == false && getComponentNodeFromMap(enumElement) != null) {
    		returnComponentNode = getComponentNodeFromMap(enumElement);
		}	
    	
    	return returnComponentNode;
    }

    @Override
    protected void makeDependency(ASTComponentNode root) {
        if ( root.getComponent() instanceof Module && root.getComponent().getType() == ComponentType.FILE){
            for (Entry<Integer, ASTMapNode> entry : root.getAstMapNode().getChilds().entrySet()) {
                if(entry.getValue().getData().contains(this.keyword)){
                    PatternBinding patternBinding = new PatternBinding(this.projectName, entry.getValue());
                    
                    if(patternBinding.isVerify()){
                        addDependency(root, patternBinding);
                    }
                    makeCallDependency(entry.getValue(), root);

                    IFStatement ifs = new IFStatement(entry.getValue());
                    if( ifs.getData() != null){
                        addComponentDependency(root.getComponent(), ifs.getData());
                    }
                }
            }
        }
    }

    private void addDependency(ASTComponentNode root, PatternBinding patternBinding) {
        patternBinding.getBindInfo().forEach((type, target) -> {
            try {
                ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(type);
            
                if (fromMapComponentNode != null){
                    if (fromMapComponentNode.getComponent().getType() == ComponentType.ENUM) {
                        fromMapComponentNode = getEnumElementComponent(patternBinding.getEnumElement(), fromMapComponentNode);
                    }

                    if (fromMapComponentNode != null){
                        root.getChilds().get(patternBinding.getName()).getComponent().addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, patternBinding.getLocation()));
                    }
                    else{
                        logger.info("ENUM component not found");        
                    }
                }
                else{
                    Component externalComponent = this.externalComponentManager.makeElement(target);
                    root.getChilds().get(patternBinding.getName()).getComponent().addDependency(new Dependency(externalComponent, DependencyType.REFERENCE, patternBinding.getLocation()));
                }
            } catch (Exception e) {
                logger.info("component not found");
            }
            
        });
        
    }
}
