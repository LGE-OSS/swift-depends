/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

public class IFStatement {
    private Map<String, String> dataMap = new HashMap<>();
    private String asStatement = "(conditional_checked_cast_expr";
    private String isStatement = "(is_subtype_expr";
    private ASTExtractor extractor = new ASTExtractor();

    public IFStatement(ASTMapNode root){
        for (Entry<Integer, ASTMapNode> entry : root.getChilds().entrySet()) {
            findIFStatement(entry.getValue());
        }
    }

    private void findIFStatement(ASTMapNode node){
        if(node.getData().contains(asStatement) || node.getData().contains(isStatement)){
            String location = extractor.extractLocation(node.getData());
            String type = extractor.extractEqualKeywordWithQuote("writtenType", node.getData()).replaceAll("\\W", "");
            dataMap.put(location, type);
        }

        for(Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()){
            findIFStatement(entry.getValue());
        }
    }

    public Map<String, String> getData(){
        if (dataMap.size() <= 0){
            return null;
        }
        return dataMap;
    } 
}
