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

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.junit.Test;

public class CallExpressionTest {

    @Test
    public void testCallExpression(){
        ASTMapNode root = new ASTMapNode("(call_expr type='Void' location=./ContactViewController.swift:15:11 range=[./ContactViewController.swift:15:5 - line:15:23] nothrow arg_labels=");
        ASTMapNode node1 = new ASTMapNode("(dot_syntax_call_expr type='() -> Void' location=./ContactViewController.swift:15:11 range=[./ContactViewController.swift:15:5 - line:15:11] super nothrow");
        ASTMapNode node1_1 = new ASTMapNode("(declref_expr type='(UIViewController) -> () -> Void' location=./ContactViewController.swift:15:11 range=[./ContactViewController.swift:15:11 - line:15:11] decl=UIKit.(file).UIViewController.viewDidLoad() function_ref=single)");
        ASTMapNode node1_2 = new ASTMapNode("(super_ref_expr type='UIViewController' location=./ContactViewController.swift:15:5 range=[./ContactViewController.swift:15:5 - line:15:5]))");

        root.addChild(node1);
        node1.addChild(node1_1);
        node1.addChild(node1_2);

        final String location = "15:11";
        final String target = "UIKit.UIViewController.viewDidLoad()";

        CallExpression ce = new CallExpression(root);

        assertEquals(location, ce.getLocation());
        assertEquals(target, ce.getTarget());
    }

    @Test
    public void testMakeExtentionTarget(){
        ASTMapNode root = new ASTMapNode("(call_expr type='Void' location=./ContactViewController.swift:15:11 range=[./ContactViewController.swift:15:5 - line:15:23] nothrow arg_labels=");
        CallExpression ce = new CallExpression(root, "Project");

        String declValue = "(declref_expr type='() -> Value' location=/Users/Common/Global/Some.swift:122:65 range=[/Users/Common/Global/Some.swift:122:65 - line:122:65] decl=Project.(file).SomeCaseInitial extension.case(_:_:).value@/Users/Common/Global/Some.swift:120:45 function_ref=single)";
        String declWKA = "(declref_expr type='(URLSession.AuthChallengeDisposition, URLCredential?) -> Void' location=/Users/ViewController/Common/WKAiShoppingViewController.swift:410:9 range=[/Users/ViewController/Common/WKAiShoppingViewController.swift:410:9 - line:410:9] decl=Project.(file).WKAiShoppingViewController extension.webView(_:didReceive:completionHandler:).completionHandler@/Users/ViewController/Common/WKAiShoppingViewController.swift:403:90 function_ref=single)";
        
        final String declValueExpected = "Project.Some.case(_:_:).value";
        final String declWKAExpected = "Project.WKAiShoppingViewController.webView(_:didReceive:completionHandler:).completionHandler";

        ce.makeExtentionTarget(declValue);
        assertEquals(declValueExpected, ce.getTarget());

        ce.makeExtentionTarget(declWKA);
        assertEquals(declWKAExpected, ce.getTarget());
    }
}
