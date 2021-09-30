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

import java.util.HashMap;
import com.lge.swiftdepends.domain.Component;

public class ASTComponentNode {
    private ASTComponentNode parent;
    private HashMap<String, ASTComponentNode> childs;
    private Component component;
    private ASTMapNode astMapNode;

    public ASTComponentNode(Component component){
        this.component = component;
        this.childs = new HashMap<>();
    }

    public ASTComponentNode getParent() {
        return parent;
    }

    public void setParent(ASTComponentNode parent) {
        this.parent = parent;
    }

    public HashMap<String, ASTComponentNode> getChilds() {
        return childs;
    }

    public void addChild(ASTComponentNode childNode){
        childNode.getComponent().setPath(this.component.getPath());
        childNode.setParent(this);
        this.childs.put(childNode.getComponent().getName(), childNode);

        childNode.getComponent().setParent(this.component);
        this.component.add(childNode.getComponent());
    }

    public void setChilds(HashMap<String, ASTComponentNode> childs) {
        this.childs = childs;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public ASTMapNode getAstMapNode() {
        return astMapNode;
    }

    public void setAstMapNode(ASTMapNode astMapNode) {
        this.astMapNode = astMapNode;
    }
}
