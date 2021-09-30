/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser.node;

import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASTMapNode {
    private ASTMapNode parent;
    private TreeMap<Integer, ASTMapNode> childs;
    private String key;
    private String data;
    private int childCount;
    private static final Logger logger = LogManager.getLogger(ASTMapNode.class);

    public ASTMapNode(String line){
        try {
            this.key = line.split(" ")[0];    
        } catch (Exception e) {
            logger.error(line);
            this.key = null;
        }
        
        this.data = line;
        this.childs = new TreeMap<>();
        this.childCount = 0;
    }

    public ASTMapNode getParent() {
        return parent;
    }

    public void setParent(ASTMapNode parent) {
        this.parent = parent;
    }

    public TreeMap<Integer, ASTMapNode> getChilds() {
        return this.childs;
    }

    public ASTMapNode getLastChild() {
        return this.childs.lastEntry().getValue();
    }

    public void addChild(ASTMapNode child){
        this.childs.put(childCount, child);
        this.childCount++;
    }

    public void setChilds(TreeMap<Integer, ASTMapNode> childs) {
        this.childs = childs;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String value) {
        this.data = value;
    }

}
