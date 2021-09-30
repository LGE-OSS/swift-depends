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
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DependencyInherits extends AbstractDependency {
    private final String fileSyntax = ".(file)";
    private static final Logger logger = LogManager.getLogger(DependencyInherits.class);

    public DependencyInherits(String projectName, String keyword) {
        super(projectName, keyword);
    }

    @Override
    protected void makeDependency(ASTComponentNode root) {
        for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
            if (entry.getValue().getComponent() instanceof Module
                && ( entry.getValue().getComponent().getType() == ComponentType.CLASS 
                    || entry.getValue().getComponent().getType() == ComponentType.STRUCT)){
                makeInheritDependency(entry.getValue());
            }
        }
    }

    private void makeInheritDependency(ASTComponentNode classNode){        
        if (classNode.getAstMapNode().getData().contains(this.keyword)){
            String[] inheritTarget = extractor.extractAfterLastColon(classNode.getAstMapNode().getData());
            String refLocation = extractor.extractLocation(classNode.getAstMapNode().getData());

            for (String inheritClass : inheritTarget) {
                ASTComponentNode parentComponentNode = getComponentNodeFromMap(inheritClass);

                if (parentComponentNode != null){
                    classNode.getComponent().addDependency(new Dependency(parentComponentNode.getComponent(), DependencyType.REFERENCE, refLocation));
                }
                else{
                    String parent = findInheritParent(inheritClass, classNode.getAstMapNode());
                    Component externalComponent;
                    if (parent != null){
                        externalComponent = this.externalComponentManager.makeElement(parent);
                    }
                    else{
                        externalComponent = this.externalComponentManager.makeModule(inheritClass);
                    }
                    classNode.getComponent().addDependency(new Dependency(externalComponent, DependencyType.REFERENCE, refLocation));
                }
            }
        }
    }

    private String findInheritParent(String inheritClassName, ASTMapNode root){
        for (Entry<Integer, ASTMapNode> entry : root.getChilds().entrySet()) {
            if (entry.getValue().getData().contains("."+inheritClassName)){
                String extractFileLocation = extractor.extractFileSyntax(entry.getValue().getData());
                String classFileLocation = "";

                try{
                    for (String strData : extractFileLocation.replace(fileSyntax, "").split("\\.")) {
                        if( strData.equals(inheritClassName)){
                            classFileLocation = classFileLocation.concat(strData);
                            return classFileLocation;
                        }
                        else{
                            classFileLocation = classFileLocation.concat(strData).concat(".");
                        }
                    }
                }
                catch(Exception e){
                    logger.error(extractFileLocation);
                    return null;
                }
            }
        }

        for (Entry<Integer, ASTMapNode> entry : root.getChilds().entrySet()) {
            return findInheritParent(inheritClassName, entry.getValue());
        }
        return null;
    }
}
