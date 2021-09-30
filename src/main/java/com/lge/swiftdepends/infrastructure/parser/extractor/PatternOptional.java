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
import java.util.List;
import java.util.Map;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PatternOptional {
	private String name = "";
	private String location = "";
	private Map<String, String> pattern = new HashMap<>(); // type, target

	private String keyword = "";
	private String projectName = "";

	private ASTExtractor extractor = new ASTExtractor();
	private static final Logger logger = LogManager.getLogger(PatternOptional.class);

	public PatternOptional(String keyword, String projectName, ASTMapNode node) {
		this.keyword = keyword;
		this.projectName = projectName;

		if (node.getData().contains(this.keyword)) {
			// 1. getParent() is (pattern
			// 2. getParent().getParent() is if or guard statement
			this.location = extractor.extractLocation(node.getParent().getParent().getData());
		}

		for (var entry : node.getChilds().entrySet()) {
			findOptional(entry.getValue());
		}
	}

	private void findOptional(ASTMapNode node) {
    	if (node.getData().contains("(pattern_named")){
			try {
				String[] splitStr = node.getData().split(" ");
            	this.name = splitStr[splitStr.length-1].replaceAll("\\W", "");	
			} catch (Exception e) {
				logger.error(node.getData());
				this.name = "";
				return;
			}
            
            String typeData = extractor.extractEqualKeywordWithQuote("type", node.getData());
            List<String> typeList = extractor.extractGenericAndDict(typeData);
            
            typeList.forEach( type ->{
            	if(pattern.get(type) == null) {
            		pattern.put(type, type);
            	}
            });
        }
    	else if (node.getData().contains("(component id")){
    		String id = extractor.extractEqualKeywordWithQuote("id", node.getData());
    		String bind = extractor.extractBindFromComponent(node.getData()).replace(this.projectName +".", "");
    		
    		pattern.put(id, bind);
    	}
    	
        for (var entry : node.getChilds().entrySet()) {
        	findOptional(entry.getValue());
        } 	
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public Map<String, String> getPattern() {
		return pattern;
	}
}
