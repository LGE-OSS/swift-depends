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
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;
import com.lge.swiftdepends.infrastructure.parser.extractor.IFStatement;
import com.lge.swiftdepends.infrastructure.parser.extractor.ParameterFinder;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

public class DependencyFunction extends AbstractDependency {

    public DependencyFunction(String projectName, String keyword) {
        super(projectName, keyword);
    }

    @Override
    protected void makeDependency(ASTComponentNode root) {
        for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
            if(entry.getValue().getAstMapNode().getData().contains(this.keyword)){
                for (Entry<Integer, ASTMapNode> innerEntry : entry.getValue().getAstMapNode().getChilds().entrySet()) {
                    if(innerEntry.getValue().getData().contains("(parameter_list")){
                        makeParameterDependency(innerEntry.getValue(), entry.getValue());
                    }
                    if(innerEntry.getValue().getData().contains("(result")){
                        makeReturnDependency(innerEntry.getValue(), entry.getValue());
                    }
                }
                makeCallDependency(entry.getValue().getAstMapNode(), entry.getValue());
                
                IFStatement ifs = new IFStatement(entry.getValue().getAstMapNode());
                if( ifs.getData() != null){
                    addComponentDependency(entry.getValue().getComponent(), ifs.getData());
                }
            }
        }

        for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()){
            makeDependency(entry.getValue());
        }
    }

    private void makeParameterDependency(ASTMapNode roots, ASTComponentNode parent){
        for (Entry<Integer, ASTMapNode> entry : roots.getChilds().entrySet()) {
            ParameterFinder parameterFinder = new ParameterFinder();

            if(parameterFinder.setData(entry.getValue().getData(), parent.getAstMapNode().getData()) == true){
                parameterFinder.getParameterList().forEach(param -> {
                    addComponentDependency(param, parameterFinder.getLocation(), parent.getComponent());    
                });
            }
        }
    }

    private void makeReturnDependency(ASTMapNode roots, ASTComponentNode parent){
        for (Entry<Integer, ASTMapNode> entry : roots.getChilds().entrySet()) {
            for (Entry<Integer, ASTMapNode> innerEntry : entry.getValue().getChilds().entrySet()) {
                if(innerEntry.getValue().getData().contains("(component")){
                    String strBind = extractor.extractBindFromComponent(innerEntry.getValue().getData());
                    String strLocation = extractor.extractLocation(parent.getAstMapNode().getData());
                    if(strBind != null && strLocation != null) {
                        createDependency(strBind, strLocation, parent, DependencyType.REFERENCE);
                    }
                }
            }
        }
    }

    private void addComponentDependency(String type, String location, Component parent){
        ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(type);

        if (fromMapComponentNode != null){
            parent.addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, location));
        }
        else{
            Component externalComponent = this.externalComponentManager.makeElement(type);
            parent.addDependency(new Dependency(externalComponent, DependencyType.REFERENCE, location));
        }
    }
}
