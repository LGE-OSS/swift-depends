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
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

public class DependencyImport extends AbstractDependency {

    public DependencyImport(String projectName, String keyword) {
        super(projectName, keyword);
    }

    @Override
    protected void makeDependency(ASTComponentNode root) {
        String fileName = root.getComponent().getName();

        for (Entry<Integer, ASTMapNode> entry : root.getAstMapNode().getChilds().entrySet()) {
            if( entry.getValue().getData().contains(this.keyword)){
                if (rootMap.get(fileName).getComponent() != null){
                    String externalComponentName = extractor.extractQuote("'", entry.getValue().getData());
                    String externalComponentLocation = extractor.extractLocation(entry.getValue().getData());
                    Component externalComponent = externalComponentManager.makeModule(externalComponentName);
                    rootMap.get(fileName).getComponent().addDependency(new Dependency(externalComponent, DependencyType.IMPORT, externalComponentLocation));
                }
            }
        }
    }
}
