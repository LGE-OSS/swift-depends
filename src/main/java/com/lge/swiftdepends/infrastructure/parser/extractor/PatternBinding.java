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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PatternBinding {
    private String name ="";
    private String type = "";
    private String target = "";
    private String location = "";
    private final String fileSyntax = ".(file)";
    private String enumElement = "";
    private ASTExtractor extractor = new ASTExtractor();
    private String projectName = "";

    private Map<String, String> bindInfo = new HashMap<>();
    private List<String> typeList = new ArrayList<>();
    private int componentCount = 0;
    private static final Logger logger = LogManager.getLogger(PatternBinding.class);

    public PatternBinding(String projectName, ASTMapNode node){
        this.projectName = projectName;

        if (node.getData().contains("pattern_binding_decl")){
            this.location = extractor.extractLocation(node.getData());
            getComponentCount(node);
        }

        for (Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()) {
            findBinding(entry.getValue());
        }
        
        if (isVerify() == false){
            componentCount = 1;
            for (Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()) {
                findBindingInitCase(entry.getValue());
            }

            this.typeList.forEach(type ->{
                if(this.bindInfo.containsKey(type) == false){
                    this.bindInfo.put(type, type);
                }
            });
        }

        for (var entry : node.getChilds().entrySet()) {
            findAdditionalKeyword(entry.getValue());
        }
    }

    private void getComponentCount(ASTMapNode node){
        if (node.getData().contains("(component id")){
            componentCount += 1;
        }
        for (Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()) {
            getComponentCount(entry.getValue());
        }
    }

    private void findBinding(ASTMapNode node){
        if (node.getData().contains("(pattern_named")){
            try {
                String[] splitStr = node.getData().split(" ");
                this.name = splitStr[splitStr.length-1].replaceAll("\\W", "");    
            } catch (Exception e) {
                logger.error(node.getData());
                this.name = "";
                return;
            }
        	
        }
        else if (node.getData().contains("(component id")){
            try {
                this.type = node.getData().split("'")[1];    
            } catch (Exception e) {
                logger.error(node.getData());
                this.type = "";
                return;
            }
            
            this.target = extractor.extractBindFromComponent(node.getData()).replace(this.projectName +".", "");

            bindInfo.put(this.type, this.target);
        }
        else if (node.getData().contains("(unresolved_dot_expr")) {
            try {
                if ("field".equals(node.getData().split("'")[2].strip())) {
                    enumElement = node.getData().split("'")[3].strip();
                }	    
            } catch (Exception e) {
                logger.error(node.getData());
                enumElement = "";
                return;
            }
        	
        }

        if(isVerify() == false){
            for (Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()) {
                findBinding(entry.getValue());
            } 	
        }
    }

    private void findBindingInitCase(ASTMapNode node){
    	if (node.getData().contains("pattern_binding_decl")){
            this.location = extractor.extractLocation(node.getData());
        }
        else if (node.getData().contains("(pattern_named")){
            try {
                String[] splitStr = node.getData().split(" ");
                this.name = splitStr[splitStr.length-1].replaceAll("\\W", "");    
            } catch (Exception e) {
                logger.error(node.getData());
                this.name = "";
                return;
            }
            
            this.type = extractor.extractEqualKeywordWithQuote("type", node.getData()).replaceAll("\\W", "");

            String typeData = extractor.extractEqualKeywordWithQuote("type", node.getData());
            this.typeList = extractor.extractGenericAndDict(typeData);
        }
        else if(extractor.extractFileSyntax(node.getData()) != null){
            try {
                this.target = extractor.extractFileSyntax(node.getData()).replace(fileSyntax, "").replace(this.projectName+".", "").split("@")[0];    
            } catch (Exception e) {
                logger.error(node.getData());
                this.target = "";
                return;
            }

            if( this.target.contains(")")){
                try {
                    this.target = this.target.substring(0, this.target.lastIndexOf(")"));    
                } catch (Exception e) {
                    logger.error(this.target + " " + node.getData());
                    return;
                }
            }
            addTargetBracket();

            if( !this.type.equals("") && !this.target.equals("")){
                bindInfo.put(this.type, this.target);
            }
        }

        for (Entry<Integer, ASTMapNode> entry : node.getChilds().entrySet()) {
            if (isVerify()){
                break;
            }
            findBindingInitCase(entry.getValue());
        }
    }

    private void addTargetBracket(){
        if (this.target.contains("(")){
            this.target = this.target.concat(")");
        }
    }

    public boolean isVerify(){
        if ( !this.name.equals("") && !this.location.equals("")
            && !this.target.equals("") && !this.type.equals("")
            && this.bindInfo.size() >= this.componentCount){
            return true;
        }
        return false;
    }

    private void findAdditionalKeyword(ASTMapNode node){
        if(node.getData().contains("Processed init:")){
            return ;
        }

        if(node.getData().contains("optional_evaluation_expr")){
            for( var entry : node.getChilds().entrySet()){
                if(entry.getValue().getData().contains("conditional_checked_cast_expr")){
                    String writtenType = extractor.extractEqualKeywordWithQuote("writtenType", entry.getValue().getData());
                    List<String> typeList = extractor.extractGenericAndDict(writtenType);
                    
                    typeList.forEach(data -> {
                        this.bindInfo.put(data, data);
                    });
                }
            }
        }
        
        if(node.getData().contains("dot_syntax_call_expr")) {
        	for( var entry : node.getChilds().entrySet()) {
        		if(entry.getValue().getData().contains("declref_expr")) {
        			String declData = extractor.extractDecl(entry.getValue().getData());
        			this.bindInfo.put(declData, declData);
        		}
        	}
        }

        for(var entry : node.getChilds().entrySet()){
            findAdditionalKeyword(entry.getValue());
        }
    }
    
    public String getName() {
        return name;
    }

    public Map<String, String> getBindInfo(){
        return bindInfo;
    }

    public String getLocation() {
        return location;
    }

	public String getEnumElement() {
		return enumElement;
	}
}
