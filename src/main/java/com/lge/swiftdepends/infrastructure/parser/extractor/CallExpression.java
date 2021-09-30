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

import java.util.Map.Entry;

import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CallExpression {
    private String location = null;
    private String target = null;
    private ASTExtractor extractor = new ASTExtractor();
    private String projectName = "";
    private final String extensionKeyword = "extension.";
    private static final Logger logger = LogManager.getLogger(CallExpression.class);

    public CallExpression(ASTMapNode root){
        findCallExpr(root);
    }

    public CallExpression(ASTMapNode root, String projectName){
        this.projectName = projectName;
        findCallExpr(root);
    }

    private void findCallExpr(ASTMapNode root){        
        if(root.getData().contains("decl=") && root.getData().contains("declref_expr")){
            this.location = extractor.extractLocation(root.getData());
            if(root.getData().contains(extensionKeyword) && root.getData().contains(this.projectName + ".")
             && root.getData().contains("function_ref=single")){
                makeExtentionTarget(root.getData());
                return;
            }
            else{
                this.target = extractor.extractDecl(root.getData());
                return;
            }
        }

        for (Entry<Integer, ASTMapNode> entry : root.getChilds().entrySet()) {
            if(!isVerify()){
                findCallExpr(entry.getValue());
            }
        }
    }

    public void makeExtentionTarget(String line){
        try {
            String[] lineSplit = line.split(" ");

            for (String split : lineSplit) {
                if(split.contains(extensionKeyword) && split.contains("@")){
                    String componentName = line.substring(line.lastIndexOf("/")+1, line.lastIndexOf("."));
                    String functionName = split.split("@")[0].replace(extensionKeyword, "");
                    this.target = this.projectName+"."+ componentName + "." + functionName;
                }
            }
        } catch (Exception e) {
            logger.error(line);
            this.target = "";
            return;
        }
    }

    public boolean isVerify(){
        if(this.location != null && this.target != null){
            return true;
        }
        return false;
    }

    public String getLocation() {
        return location;
    }

    public String getTarget() {
        return target;
    }
}
