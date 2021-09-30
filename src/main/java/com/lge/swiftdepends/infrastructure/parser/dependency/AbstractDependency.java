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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;
import com.lge.swiftdepends.infrastructure.parser.extractor.CallExpression;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import com.lge.swiftdepends.infrastructure.parser.extractor.ASTExtractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractDependency {
	protected String keyword;
	protected String projectName;
    protected Map<String, ASTComponentNode> rootMap;
	protected ExternalComponentManager externalComponentManager;
	protected ASTExtractor extractor = new ASTExtractor();

	private static final Logger logger = LogManager.getLogger(AbstractDependency.class);

    public AbstractDependency(String projectName, String keyword){
		this.projectName = projectName;
		this.keyword = keyword;
    }
	
    public void setDependency(Map<String, ASTComponentNode> rootMap, ExternalComponentManager externalComponentManager) {
        this.rootMap = rootMap;
        this.externalComponentManager = externalComponentManager;

		String className = this.getClass().getSimpleName().replace("Dependency", "");

		ProgressBarBuilder pbb = new ProgressBarBuilder()
										.setStyle(ProgressBarStyle.ASCII)
										.setTaskName("Make " + className + " Dependency" )
										.setUpdateIntervalMillis(100)
										.setInitialMax(this.rootMap.size());
	
		ProgressBar.wrap(this.rootMap.entrySet().parallelStream(), pbb).forEach( entry -> {
			makeDependency(entry.getValue());
		});
	}

    protected ASTComponentNode getComponentNodeFromMap(String componentName){
		ASTComponentNode result = null;

		if(componentName.contains(".")){
			return getChildComponentNodeFromMap(componentName);
		}

		if(this.rootMap.get(componentName) != null){
			if(this.rootMap.get(componentName).getChilds().get(componentName) == null){
				return this.rootMap.get(componentName);
			}
		}

        for (Entry<String, ASTComponentNode> files : this.rootMap.entrySet()) {
			result = findComponent(files.getValue(), componentName);
			if(result != null){
				return result;
			}

		}
		return null;
	}

	private ASTComponentNode getChildComponentNodeFromMap(String componentName){
		String[] splitComponentName;
		try{
			splitComponentName = componentName.split("\\.");
		}
		catch(Exception e){
			logger.error(componentName);
			return null;
		}

		ASTComponentNode result = null;
		ASTComponentNode parent = null;
		
		parent = getComponentNodeFromMap(splitComponentName[0]);

		if(parent != null){
			for( int i = 1; i < splitComponentName.length ; i++){
				result = getChildComponentNodeFromNode(parent, splitComponentName[i]);
				if(result == null){
					return parent;
				}
				else{
					parent = result;
				}
			}
		}
		return result;
	}
	
	private ASTComponentNode getChildComponentNodeFromNode(ASTComponentNode parent, String componentName){
		return parent.getChilds().get(componentName);
	}

	protected ASTComponentNode findComponent(ASTComponentNode root, String componentName){		
		for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
			String entryComponentName = "";
			try{
				entryComponentName = entry.getValue().getComponent().getName().split("\\)")[0];
			}
			catch(NullPointerException ne){
				logger.error(componentName);
				return null;
			}
			
			if(entryComponentName.contains("(")) {
				entryComponentName += ")";
			}
			
			if(entryComponentName.equals(componentName) 
			|| entryComponentName.replaceAll("\\W", "").replace("_", "").equals(componentName)){
				return entry.getValue();
			}
		}

		for (Entry<String, ASTComponentNode> entry : root.getChilds().entrySet()) {
			return findComponent(entry.getValue(), componentName); 
		}
		return null;
	}

	protected void createDependency(String strTarget, String strLocation, ASTComponentNode sourceComponent, DependencyType dependencyType){
		Component targetComponent = null;
		strTarget = strTarget.replace(this.projectName+".", "");

		String[] splitDecl;
		try{
			splitDecl = strTarget.split("@")[0].split("\\.");
		}
		catch(Exception e){
			logger.error(strTarget);
			return;
		}

		ASTComponentNode targetComponentNode = getComponentNodeFromMap(splitDecl[0]);
		
		if(targetComponentNode != null ){
			for( int i = 1 ; i < splitDecl.length; i++ ){
				if (targetComponentNode.getChilds().get(splitDecl[i]) == null){
					break;
				}
				else{
					targetComponentNode = targetComponentNode.getChilds().get(splitDecl[i]);
				}
			}
			targetComponent = targetComponentNode.getComponent();
		}

        if(targetComponent == null){
            targetComponent = this.externalComponentManager.makeElement(strTarget);
        }
        sourceComponent.getComponent().addDependency(new Dependency(targetComponent, dependencyType, strLocation));
	}
	
	protected void makeCallDependency(ASTMapNode root, ASTComponentNode parent){
        if(root.getData().contains("(call_expr")){
			CallExpression callExpression = new CallExpression(root, this.projectName);
			if(callExpression.isVerify()){
				createDependency(callExpression.getTarget(), callExpression.getLocation(), parent, DependencyType.INVOCATION);
			}
        }

        for (Entry<Integer, ASTMapNode> entry : root.getChilds().entrySet()) {
            if(root.getData().contains("(pattern_binding_decl") == false){
                makeCallDependency(entry.getValue(), parent);
            }
        }
    }

	protected void addComponentDependency(Component component, Map<String, String> mapData){
        mapData.forEach( (location, type) -> {
            ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(type);

			if (fromMapComponentNode != null){
				component.addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, location));
			}
        });
    }

	protected void addComponentDependency(Component component, List<String> reference, String location){
		reference.forEach( ref -> {
			ASTComponentNode fromMapComponentNode = getComponentNodeFromMap(ref);

			if (fromMapComponentNode != null){
				component.addDependency(new Dependency(fromMapComponentNode.getComponent(), DependencyType.REFERENCE, location));
			}
		});
	}

	abstract protected void makeDependency(ASTComponentNode root);
}
