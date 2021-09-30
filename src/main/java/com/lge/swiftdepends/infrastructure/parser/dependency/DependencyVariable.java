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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DependencyVariable extends AbstractDependency{
    String enumElementKeyword = "(enum_element_decl";
    String lazyKeyword = "(lazy_initializer_expr";
    private static final Logger logger = LogManager.getLogger(DependencyVariable.class);

    public DependencyVariable(String projectName, String keyword) {
        super(projectName, keyword);
    }

    @Override
    protected void makeDependency(ASTComponentNode root) {
        if(root.getComponent().getType() == ComponentType.VARIABLE){
            if(root.getAstMapNode().getData().contains(this.keyword)){
                for(var entry : root.getAstMapNode().getChilds().entrySet()){
                    makeCallDependency(entry.getValue(), root);
                }
                findLazy(root.getAstMapNode(), root);
            }
        }

        if(root.getAstMapNode().getData().contains(enumElementKeyword)){
            makeEnumCaseDependency(root);
        }

        for( var entry : root.getChilds().entrySet()){
            makeDependency(entry.getValue());
        }
    }
    
    private void findLazy(ASTMapNode mapNode, ASTComponentNode parent) {
    	if( mapNode.getData().contains(lazyKeyword)){
    		makeCallDependency(mapNode, parent);
    	}
    	
    	for(var entry : mapNode.getChilds().entrySet()) {
    		findLazy(entry.getValue(), parent);
    	}
    }

    private List<String> getTypeList(String type){
        List<String> typeList = new ArrayList<>();

        try{
            String[] splitArrow = type.split("->");
        
            for (String str : splitArrow) {
                String[] splitData = str.replaceAll("[^a-zA-Z0-9._,]", "").split(",");
                typeList.addAll(Arrays.asList(splitData));
            }
        }
        catch(Exception e){
            logger.error(type);
            return null;
        }

        return typeList;
    }

    private void makeEnumCaseDependency(ASTComponentNode  enumCaseComponent){
        String parentName = enumCaseComponent.getParent().getComponent().getName();
        String enumCaseStatement = enumCaseComponent.getAstMapNode().getData();

        List<String> typeList = getTypeList(enumCaseStatement);
        String location = extractor.extractLocation(enumCaseStatement);

        List<String> dependencyTypeList = new ArrayList<>();

        for (String type : typeList) {
            if(type.contains(parentName) == false){
                dependencyTypeList.add(type);
            }
        }
        addComponentDependency(enumCaseComponent.getComponent(), dependencyTypeList, location);
    }
}
