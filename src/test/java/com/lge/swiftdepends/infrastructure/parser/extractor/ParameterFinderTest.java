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

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.junit.Test;

public class ParameterFinderTest {
    @Test
    public void testParameterFinder(){
        ASTMapNode rootMapNode = new ASTMapNode("(func_decl range=[./ProductsTableViewController.swift:27:12 - line:35:3] \"prepare(for:sender:)\" interface type='(ProductsTableViewController) -> (UIStoryboardSegue, Any?) -> ()' access=internal override=UIKit.(file).UIViewController.prepare(for:sender:) @objc dynamic");
        ASTMapNode mapNode1_1 = new ASTMapNode("(parameter \"self\" type='ProductsTableViewController' interface type='ProductsTableViewController')");
        ASTMapNode mapNode1_2 = new ASTMapNode("(parameter_list");
        ASTMapNode mapNode1_2_1 = new ASTMapNode("(parameter \"segue\" apiName=for type='UIStoryboardSegue' interface type='UIStoryboardSegue')");

        rootMapNode.addChild(mapNode1_1);
        rootMapNode.addChild(mapNode1_2);
        
        mapNode1_2.addChild(mapNode1_2_1);

        final List<String> parameter = List.of("UIStoryboardSegue");
        final String location = "27:12";

        ParameterFinder pf = new ParameterFinder();
        pf.setData(mapNode1_2_1.getData(), rootMapNode.getData());

        assertEquals(parameter, pf.getParameterList());
        assertEquals(location, pf.getLocation());
    }

    @Test
    public void testParamterFinderArrow(){
        ASTMapNode rootMapNode = new ASTMapNode("(func_decl range=[./ProductsTableViewController.swift:27:12 - line:35:3] \"prepare(for:sender:)\" interface type='(ProductsTableViewController) -> (UIStoryboardSegue, Any?) -> ()' access=internal override=UIKit.(file).UIViewController.prepare(for:sender:) @objc dynamic");
        ASTMapNode mapNode1_1 = new ASTMapNode("(parameter \"self\" type='ProductsTableViewController' interface type='ProductsTableViewController')");
        ASTMapNode mapNode1_2 = new ASTMapNode("(parameter_list");
        ASTMapNode mapNode1_2_1 = new ASTMapNode("(parameter \"cb_handler\" apiName=cb_handler type='(Int?) -> Void' interface type='(Int?) -> Void') ");

        rootMapNode.addChild(mapNode1_1);
        rootMapNode.addChild(mapNode1_2);
        
        mapNode1_2.addChild(mapNode1_2_1);

        final List<String> parameter = List.of("Void");
        final String location = "27:12";

        ParameterFinder pf = new ParameterFinder();
        pf.setData(mapNode1_2_1.getData(), rootMapNode.getData());

        assertEquals(parameter, pf.getParameterList());
        assertEquals(location, pf.getLocation());
    }
}

