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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASTMapTree {
    private ASTMapNode root;

    private static final Logger logger = LogManager.getLogger(ASTMapTree.class);

    public ASTMapTree(){
        this.root = null;
    }

    public ASTMapNode makeTree(String lines){
        lines.lines().forEach( line -> {
            int currentDepth = line.substring(0, line.indexOf("(") + 1).length() / 2;
            if (line.length() > 0){
                if(currentDepth == 0 && line.contains("#")){
                    try {
                        currentDepth = line.substring(0, line.indexOf("#") + 1).length() / 2;    
                    } catch (Exception e) {
                        logger.error(line);
                    }
                }

                if (currentDepth == 0){
                    if (line.contains("(source_file")){
                        root = new ASTMapNode(line);
                    }
                    else{
                        if(line.charAt(0) == '(' ){
                            logger.warn("Unexpected data " + currentDepth + line);
                        }
                    }
                }
                else{
                    try{
                        if(line.contains("(capture_list")){
                            doubleDepthPreprocessing(line, currentDepth);
                        }
                        else{
                            ASTMapNode parent = findParent(root, currentDepth-1);
                            ASTMapNode child = new ASTMapNode(line);
                            child.setParent(parent);
                            parent.addChild(child);
                        }
                    }
                    catch (NullPointerException e){
                        logger.warn("Cannot find parent " + currentDepth + line);
                    }
                }
            }
        });
        return root;
    }

    private ASTMapNode findParent(ASTMapNode data, int depth){
        if (depth == 0){            
            return data;
        }else{
            return findParent(data.getLastChild(), --depth);
        }        
    }

    private void doubleDepthPreprocessing(String line, int depth){
        ASTMapNode parent = findParent(root, depth-1);
        ASTMapNode child = new ASTMapNode(line);
        child.setParent(parent);
        parent.addChild(child);

        ASTMapNode grandChild = new ASTMapNode("  " + line);
        grandChild.setParent(child);
        child.addChild(grandChild);
    }
}
