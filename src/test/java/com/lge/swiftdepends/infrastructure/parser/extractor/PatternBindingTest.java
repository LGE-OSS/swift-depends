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
import static org.junit.Assert.assertTrue;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.junit.Test;

public class PatternBindingTest {
    @Test
    public void testPatternBindingComponentId(){
        ASTMapNode root = new ASTMapNode("(pattern_binding_decl range=[src/test/resources/GoodAsOldPhones/ProductViewController.swift:12:13 - line:12:46]");
        ASTMapNode depth1 = new ASTMapNode("(pattern_typed type='UIImageView?'");
        root.addChild(depth1);
        
        ASTMapNode depth2_1 = new ASTMapNode("(pattern_named type='UIImageView?' 'productImageView')");
        ASTMapNode depth2_2 = new ASTMapNode("(type_implicitly_unwrapped_optional");
        depth1.addChild(depth2_1);
        depth1.addChild(depth2_2);

        ASTMapNode depth3 = new ASTMapNode("(type_ident");
        depth2_2.addChild(depth3);

        ASTMapNode depth4 = new ASTMapNode("(component id='UIImageView' bind=UIKit.(file).UIImageView))))");
        depth3.addChild(depth4);

        ASTMapNode depth4_1 = new ASTMapNode("(component id='Bool' bind=Swift.(file).Bool)))))");
        depth3.addChild(depth4_1);

        final String expectedName = "productImageView";
        final String expectedType = "UIImageView";
        final String expectedTarget = "UIKit.UIImageView";
        final String expectedLocation = "12:13";
        final String expectedOtherType = "Bool";
        final String expectedOtherTarget = "Swift.Bool";

        PatternBinding pb = new PatternBinding("PB",root);

        assertEquals(expectedName, pb.getName());
        assertTrue(pb.getBindInfo().containsKey(expectedType));
        assertEquals(expectedTarget, pb.getBindInfo().get(expectedType));
        assertEquals(expectedLocation, pb.getLocation());
        assertTrue(pb.getBindInfo().containsKey(expectedOtherType));
        assertEquals(expectedOtherTarget, pb.getBindInfo().get(expectedOtherType));
    }

    @Test
    public void testPatternBindingDictionaryComponentId(){
        ASTMapNode root = new ASTMapNode("(pattern_binding_decl range=[src/test/resources/GoodAsOldPhones/ProductViewController.swift:12:13 - line:12:46]");
        ASTMapNode depth1 = new ASTMapNode("(pattern_typed type='[String : AutoOrderInfo]'");
        root.addChild(depth1);
        
        ASTMapNode depth2_1 = new ASTMapNode("(pattern_named type='[String : AutoOrderInfo]' 'supplyModelDict')");
        ASTMapNode depth2_2 = new ASTMapNode("(type_dictionary");
        depth1.addChild(depth2_1);
        depth1.addChild(depth2_2);

        ASTMapNode depth3 = new ASTMapNode("(type_ident");
        depth2_2.addChild(depth3);

        ASTMapNode depth4 = new ASTMapNode("(component id='String' bind=Swift.(file).String))");
        depth3.addChild(depth4);

        ASTMapNode depth3_1 = new ASTMapNode("(type_ident");
        depth2_2.addChild(depth3_1);

        ASTMapNode depth4_1 = new ASTMapNode("(component id='AutoOrderInfo' bind=Project.(file).AutoOrderInfo@/Users/Screens/AutoOrder/Model/AutoOrderInfo.swift:14:8))))");
        depth3_1.addChild(depth4_1);

        final String expectedName = "supplyModelDict";
        final String expectedType = "String";
        final String expectedTarget = "Swift.String";
        final String expectedLocation = "12:13";
        final String expectedOtherType = "AutoOrderInfo";
        final String expectedOtherTarget = "Project.AutoOrderInfo";

        PatternBinding pb = new PatternBinding("PB",root);

        assertEquals(expectedName, pb.getName());
        assertTrue(pb.getBindInfo().containsKey(expectedType));
        assertEquals(expectedTarget, pb.getBindInfo().get(expectedType));
        assertEquals(expectedLocation, pb.getLocation());
        assertTrue(pb.getBindInfo().containsKey(expectedOtherType));
        assertEquals(expectedOtherTarget, pb.getBindInfo().get(expectedOtherType));
    }

    @Test
    public void testPatternBindingInitCase(){
        ASTMapNode root = new ASTMapNode("pattern_binding_decl range=[src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:11 - line:12:27]");
        ASTMapNode node1 = new ASTMapNode("(pattern_named type='String' 'identifer')");
        ASTMapNode node2 = new ASTMapNode("Original init:");
        ASTMapNode node3 = new ASTMapNode("(string_literal_expr type='String' location=src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 range=[src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 - line:12:27] encoding=utf8 value=\"productCell\" builtin_initializer=Swift.(file).String extension.init(_builtinStringLiteral:utf8CodeUnitCount:isASCII:) initializer=**NULL**)");
        ASTMapNode node4 = new ASTMapNode("Processed init:");
        ASTMapNode node5 = new ASTMapNode("(string_literal_expr type='String' location=src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 range=[src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 - line:12:27] encoding=utf8 value=\"productCell\" builtin_initializer=Swift.(file).String extension.init(_builtinStringLiteral:utf8CodeUnitCount:isASCII:) initializer=**NULL**))");

        root.addChild(node1);
        root.addChild(node2);
        root.addChild(node3);
        root.addChild(node4);
        root.addChild(node5);

        final String expectedName = "identifer";
        final String expectedType = "String";
        final String expectedTarget = "Swift.String";
        final String expectedLocation = "12:11";

        PatternBinding pb = new PatternBinding("PB", root);

        assertEquals(expectedName, pb.getName());
        assertTrue(pb.getBindInfo().containsKey(expectedType));
        assertEquals(expectedTarget, pb.getBindInfo().get(expectedType));
        assertEquals(expectedLocation, pb.getLocation());
    }
    
    @Test
    public void testPatternBindingUnresolvedDotExpr(){
    	final String expectedName = "mySpoon";
        final String expectedType = "Spoon";
        final String expectedLocation = "4:1";
        final String expectedEnumElement = "gold";
        
        ASTMapNode root = new ASTMapNode("(top_level_code_decl range=[main.swift:4:1 - line:4:21]");
		
    	ASTMapNode depth1 = new ASTMapNode("(brace_stmt implicit range=[main.swift:4:1 - line:4:21]");
    	root.addChild(depth1);
        ASTMapNode depth2 = new ASTMapNode("(pattern_binding_decl range=[main.swift:4:1 - line:4:21] trailing_semi");
		depth1.addChild(depth2);
    
    	ASTMapNode depth3_1 = new ASTMapNode("(pattern_named type='Spoon' 'mySpoon')");
        ASTMapNode depth3_2 = new ASTMapNode("Original init:");
        ASTMapNode depth3_3 = new ASTMapNode("(unresolved_dot_expr type='<null>' field 'gold' function_ref=unapplied");
        ASTMapNode depth3_4 = new ASTMapNode("Processed init:");
        ASTMapNode depth3_5 = new ASTMapNode("(dot_syntax_call_expr type='Spoon' location=main.swift:4:21 range=[main.swift:4:15 - line:4:21] nothrow");
        depth2.addChild(depth3_1);
        depth2.addChild(depth3_2);
        depth2.addChild(depth3_3);
        depth2.addChild(depth3_4);
        depth2.addChild(depth3_5);
        
        ASTMapNode depth4_1 = new ASTMapNode("(type_expr type='Spoon.Type' location=main.swift:4:15 range=[main.swift:4:15 - line:4:15] typerepr='Spoon'))");
        ASTMapNode depth4_2 = new ASTMapNode("(declref_expr type='(Spoon.Type) -> Spoon' location=main.swift:4:21 range=[main.swift:4:21 - line:4:21] decl=EnumTest.(file).Spoon.gold@Spoon.swift:6:8 function_ref=unapplied)");
        ASTMapNode depth4_3 = new ASTMapNode("(type_expr type='Spoon.Type' location=main.swift:4:15 range=[main.swift:4:15 - line:4:15] typerepr='Spoon')))");
        depth3_3.addChild(depth4_1);
        depth3_5.addChild(depth4_2);
        depth3_5.addChild(depth4_3);
        
        PatternBinding pb = new PatternBinding("PB", root);
    	
        assertEquals(expectedName, pb.getName());
        assertTrue(pb.getBindInfo().containsKey(expectedType));
        assertEquals(expectedLocation, pb.getLocation());
        assertEquals(expectedEnumElement, pb.getEnumElement());
    }
}
