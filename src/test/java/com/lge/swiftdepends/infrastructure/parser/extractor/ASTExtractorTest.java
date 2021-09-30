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

import org.junit.Before;
import org.junit.Test;

public class ASTExtractorTest {
    ASTExtractor extractor = new ASTExtractor();
    
    @Before
    public void setUp() {
        final String test_dir = "test_dir/";
        if (System.getProperty("logDirectory") == null){
            System.setProperty("logDirectory", test_dir);
        }
    }

    @Test 
    public void testExtractQuote(){
        String singleQuote = "(import_decl range=[./ProductsTableViewController.swift:8:1 - line:8:8] 'UIKit')";
        String doubleQuote = "(class_decl range=[./ProductsTableViewController.swift:10:1 - line:36:1] \"ProductsTableViewController\" interface type='ProductsTableViewController.Type' access=internal non-resilient inherits: UITableViewController";
        String douvleQuoteFunc = "(func_decl range=[./ProductsTableViewController.swift:27:12 - line:35:3] \"prepare(for:sender:)\" interface type='(ProductsTableViewController) -> (UIStoryboardSegue, Any?) -> ()' access=internal override=UIKit.(file).UIViewController.prepare(for:sender:) @objc dynamic";

        final String singleExpected = "UIKit";
        final String doubleExpected = "ProductsTableViewController";
        final String doubleFuncExpected = "prepare(for:sender:)";

        assertEquals(singleExpected, extractor.extractQuote("'", singleQuote));
        assertEquals(doubleExpected, extractor.extractQuote("\"", doubleQuote));
        assertEquals(doubleFuncExpected, extractor.extractQuote("\"", douvleQuoteFunc));
    }

    @Test
    public void testExtractSourceFileName(){
        String pvc = "(source_file \"src/test/resources/GoodAsOldPhones/ProductViewController.swift\"";
        String cvc = "(source_file \"./ContactViewController.swift\"";
        String fvs = "(source_file \"FunctionVariableSample.swift\"";
        String macPath= "(source_file \"....../Users/ios/Classes/GlobalConstString.swift\"";

        final String pvcExpected = "ProductViewController";
        final String cvcExpected = "ContactViewController";
        final String fvsExpected = "FunctionVariableSample";
        final String macPathExpected = "GlobalConstString";

        assertEquals(pvcExpected, extractor.extractSourceFileName(pvc));
        assertEquals(cvcExpected, extractor.extractSourceFileName(cvc));
        assertEquals(fvsExpected, extractor.extractSourceFileName(fvs));
        assertEquals(macPathExpected, extractor.extractSourceFileName(macPath));
    }

    @Test
    public void testExtractLocation(){
        String pvc = "(import_decl range=[src/test/resources/GoodAsOldPhones/ProductViewController.swift:8:1 - line:8:8] 'UIKit')";
        String ad = "(var_decl range=[./AppDelegate.swift:13:7 - line:13:7] \"window\" type='UIWindow?' interface type='UIWindow?' access=internal @objc readImpl=stored writeImpl=stored readWriteImpl=stored";
        String blank = "(pattern_binding_decl range=[/Users/ios/ViewController/Customer/ViewModel/QNAViewModel.swift:469:11 - line:469:28]";

        final String pvcExpected = "8:1";
        final String adExpected = "13:7";
        final String blankExpected = "469:11";

        assertEquals(pvcExpected, extractor.extractLocation(pvc));
        assertEquals(adExpected, extractor.extractLocation(ad));
        assertEquals(blankExpected, extractor.extractLocation(blank));
    }

    @Test
    public void testExtractFileSyntax(){
        String str = "(string_literal_expr type='String' location=src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 range=[src/test/resources/GoodAsOldPhones/ProductsTableViewController.swift:12:27 - line:12:27] encoding=utf8 value=\"productCell\" builtin_initializer=Swift.(file).String extension.init(_builtinStringLiteral:utf8CodeUnitCount:isASCII:) initializer=**NULL**)";
        String ui = "(component id='UIImageView' bind=UIKit.(file).UIImageView))))";

        final String strExpected = "Swift.(file).String";
        final String uiExpected = "UIKit.(file).UIImageView))))";

        assertEquals(strExpected, extractor.extractFileSyntax(str));
        assertEquals(uiExpected, extractor.extractFileSyntax(ui));
    }

    @Test
    public void testExtracEqualKeyword(){
        String str = "(pattern_named type='String' 'identifer')";
        String decl = "(declref_expr type='(String?) -> String' location=/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:81:32 range=[/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:81:32 - line:81:32] decl=Project.(file).localizedStringUX30@/Users/ios/Classes/GlobalConstString.swift:29:6 function_ref=single)";
        String member = "(member_ref_expr type='@lvalue ((Double?) -> Void)?' location=/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:85:9 range=[/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:85:9 - line:85:9] decl=Project.(file).AutoOrderDialogViewController.callback@/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:26:17";
        
        final String strExpected = "'String'";
        final String strReplaceExpected = "String";
        final String declExpected = "Project.(file).localizedStringUX30@/Users/ios/Classes/GlobalConstString.swift:29:6";
        final String memberExpected = "Project.(file).AutoOrderDialogViewController.callback@/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:26:17";

        assertEquals(strExpected, extractor.extractEqualKeyword("type", str));
        assertEquals(strReplaceExpected, extractor.extractEqualKeyword("type", str).replaceAll("\\W", ""));
        assertEquals(declExpected, extractor.extractEqualKeyword("decl", decl));
        assertEquals(memberExpected, extractor.extractEqualKeyword("decl", member));
    }

    @Test
    public void testExtractEqualKeywordWithQuote(){
        String str = "(pattern_named type='String' 'identifer')";
        String strDict = "(pattern_named type='[String : String]' 'httpBody')";
        String decl = "(declref_expr type='(String?) -> String' location=/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:81:32 range=[/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:81:32 - line:81:32] decl=Project.(file).localizedStringUX30@/Users/ios/Classes/GlobalConstString.swift:29:6 function_ref=single)";
        String member = "(member_ref_expr type='@lvalue ((Double?) -> Void)?' location=/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:85:9 range=[/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:85:9 - line:85:9] decl=Project.(file).AutoOrderDialogViewController.callback@/Users/ios/Screens/AutoOrder/View/AutoOrderDialogViewController.swift:26:17";
        

        final String strExpected = "String";
        final String strDictExpected = "[String : String]";
        final String declExpected = "(String?) -> String";
        final String memberExpected = "@lvalue ((Double?) -> Void)?";

        assertEquals(strExpected, extractor.extractEqualKeywordWithQuote("type", str));
        assertEquals(strDictExpected, extractor.extractEqualKeywordWithQuote("type", strDict));
        assertEquals(declExpected, extractor.extractEqualKeywordWithQuote("type", decl));
        assertEquals(memberExpected, extractor.extractEqualKeywordWithQuote("type", member));
    }


    @Test
    public void testExtractAfterLastColon(){
        String oneInherits = "(class_decl range=[./ContactViewController.swift:10:1 - line:31:1] \"ContactViewController\" interface type='ContactViewController.Type' access=internal non-resilient inherits: UIViewController";
        String twoInherits = "(class_decl range=[./AppDelegate.swift:11:1 - line:19:1] \"AppDelegate\" interface type='AppDelegate.Type' access=internal non-resilient inherits: UIResponder, UIApplicationDelegate";

        final String[] oneExpected = {"UIViewController"};
        final String[] twoExpected = {"UIResponder", "UIApplicationDelegate"};

        assertEquals(oneExpected[0], extractor.extractAfterLastColon(oneInherits)[0]);
        assertEquals(twoExpected[0], extractor.extractAfterLastColon(twoInherits)[0]);
        assertEquals(twoExpected[1], extractor.extractAfterLastColon(twoInherits)[1]);
    }

    @Test
    public void testExtractDecl(){
        String strDecl = "(member_ref_expr type='String?' location=./ProductsTableViewController.swift:28:14 range=[./ProductsTableViewController.swift:28:8 - line:28:14] decl=UIKit.(file).UIStoryboardSegue.identifier";
        String strDeclref = "(declref_expr type='(UITableView) -> (String, IndexPath) -> UITableViewCell' location=./ProductsTableViewController.swift:50:30 range=[./ProductsTableViewController.swift:50:30 - line:50:30] decl=UIKit.(file).UITableView.dequeueReusableCell(withIdentifier:for:) function_ref=single)";
        
        final String strDeclExpected = "UIKit.UIStoryboardSegue.identifier";
        final String strDeclrefExpected = "UIKit.UITableView.dequeueReusableCell(withIdentifier:for:)";

        assertEquals(strDeclExpected, extractor.extractDecl(strDecl));
        assertEquals(strDeclrefExpected, extractor.extractDecl(strDeclref));
    }

    @Test
    public void testExtractBindFromComponent(){
        String strBindLocal = "(component id='Product' bind=pjt01.(file).Product@./Product.swift:10:7)))))";
        String strBindExternal = "(component id='Int' bind=Swift.(file).Int)))";
        String strBindExtension = "(component id='Self' bind=Project.(file).RepositoryTestSupport extension.Self)))";

        final String strBindLocalExpected = "pjt01.Product";
        final String strBindExternalExpected = "Swift.Int";
        final String strBindExtensionExpected = "Project.RepositoryTestSupport";

        assertEquals(strBindLocalExpected, extractor.extractBindFromComponent(strBindLocal));
        assertEquals(strBindExternalExpected, extractor.extractBindFromComponent(strBindExternal));
        assertEquals(strBindExtensionExpected, extractor.extractBindFromComponent(strBindExtension));
    }

    @Test
    public void testExtractGenericAndDict(){
        String strDict = "[First:Second]";
        String strDotSyntax = "First.Second";
        String strGeneric = "Temp<First>";
        String strGenericDual = "Temp<First, Second>";

        final List<String> strDictExpected = List.of("First", "Second");
        final List<String> strDotSyntaxExpected = List.of("First.Second");
        final List<String> strGenericExpected = List.of("Temp", "First");
        final List<String> strGenericDualExpected = List.of("Temp", "First", "Second");

        assertEquals(strDictExpected, extractor.extractGenericAndDict(strDict));
        assertEquals(strDotSyntaxExpected, extractor.extractGenericAndDict(strDotSyntax));
        assertEquals(strGenericExpected, extractor.extractGenericAndDict(strGeneric));
        assertEquals(strGenericDualExpected, extractor.extractGenericAndDict(strGenericDual));
    }
    
    @Test
    public void testExtracFunctionProperty() {
    	String strSend = "(func_decl range=[/Users/ios/Manager/RegistertTypeLog.swift:86:5 - line:117:5] \"sendScanListlog(_:)\" interface type='(RegistertTypeLog.Type) -> (Array<Dictionary<String, Any>>) -> ()' access=internal final type";
    	String strConcat = "(func_decl range=[/Users/ios/Pods/RxSwift/RxSwift/Traits/Completable.swift:184:12 - line:186:5] \"concat(_:)\" interface type='<Self where Self : PrimitiveSequenceType, Self.Element == Never, Self.Trait == CompletableTrait> (Self) -> (Completable) -> Completable' access=public captures=(<generic> )";
    	String strConcatSequence = "(func_decl range=[/Users/ios/Pods/RxSwift/RxSwift/Traits/Completable.swift:195:12 - line:199:5] \"concat(_:)\" <Sequence : Sequence> interface type='<Self, Sequence where Self : PrimitiveSequenceType, Sequence : Sequence, Self.Element == Never, Self.Trait == CompletableTrait, Sequence.Element == Completable> (Self.Type) -> (Sequence) -> Completable' access=public captures=(<generic> ) type";
    	String strConcatCollection = "(func_decl range=[/Users/ios/Pods/RxSwift/RxSwift/Traits/Completable.swift:208:12 - line:212:5] \"concat(_:)\" <Collection : Collection> interface type='<Self, Collection where Self : PrimitiveSequenceType, Collection : Collection, Self.Element == Never, Self.Trait == CompletableTrait, Collection.Element == Completable> (Self.Type) -> (Collection) -> Completable' access=public captures=(<generic> ) type";
    	
    	final String strSendExpected = "";
    	final String strConcatExpected = "";
    	final String strConcatSequenceExpected = "<Sequence : Sequence>";
    	final String strConcatCollectionExpected = "<Collection : Collection>";
    	
    	assertEquals(strSendExpected, extractor.extracFunctionProperty(strSend));
    	assertEquals(strConcatExpected, extractor.extracFunctionProperty(strConcat));
    	assertEquals(strConcatSequenceExpected, extractor.extracFunctionProperty(strConcatSequence));
    	assertEquals(strConcatCollectionExpected, extractor.extracFunctionProperty(strConcatCollection));
    }
}