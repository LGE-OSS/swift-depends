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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParameterFinder {
    private ASTExtractor extractor = new ASTExtractor();
    private List<String> parameterList = new ArrayList<>();
    private String location = null;
    private static final Logger logger = LogManager.getLogger(ParameterFinder.class);

    public boolean setData(String line, String functionDecl){
        try{
            if(line.contains("(parameter")){
                String parameterType = extractor.extractEqualKeywordWithQuote("type", line);
                this.location = extractor.extractLocation(functionDecl);
    
                if(parameterType == null){
                    return false;
                }
    
                if(parameterType.contains("->")){
                    String[] splitStr = parameterType.split(" ");
                    parameterType = splitStr[splitStr.length-1];
                }
                parameterList = extractor.extractGenericAndDict(parameterType);
    
                if(parameterList.size() > 0 && this.location != null){
                    return true;
                }
            }
        }
        catch(Exception e){
            logger.error(line + " " + functionDecl);
            return false;
        }

        return false;
    }

    public List<String> getParameterList() {
        return parameterList;
    }

    public String getLocation() {
        return location;
    }
}
