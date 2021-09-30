/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lge.swiftdepends.domain.ASTParser;
import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.infrastructure.parser.dependency.ExternalComponentManager;
import com.lge.swiftdepends.infrastructure.parser.node.ASTComponentNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapNode;
import com.lge.swiftdepends.infrastructure.parser.node.ASTMapTree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import com.lge.swiftdepends.infrastructure.parser.dependency.AbstractDependency;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyFunction;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyImport;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyInherits;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyPatternBinding;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyPatternOptional;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyTopLevelCode;
import com.lge.swiftdepends.infrastructure.parser.dependency.DependencyVariable;

public class SwiftASTParser implements ASTParser{
	private String projectName ="";
	private List<String> jsonDataList = new ArrayList<>();
	private List<ASTMapNode> swiftASTMapTree = new ArrayList<>();
	private Map<String, ASTComponentNode> componentNodeMap = Collections.synchronizedMap(new HashMap<>());
	private ExternalComponentManager externalComponentManager = new ExternalComponentManager();

	private static final Logger logger = LogManager.getLogger(SwiftASTParser.class);

	@Override
	public List<Component> read(String projectName, Path path) {
		this.projectName = projectName;
		this.jsonDataList = extractJson(path);

		makeMap();

		makeComponents();

		makeDependency();

		return makeResult();
	}

	private void makeMap(){
		ASTMapTree treeMaker = new ASTMapTree();

		for (var jsonData :this.jsonDataList){
			this.swiftASTMapTree.add(treeMaker.makeTree(jsonData));
		}
	}

	private void makeComponents(){
		ProgressBarBuilder pbb = new ProgressBarBuilder()
										.setStyle(ProgressBarStyle.ASCII)
										.setTaskName("Make Components")
										.setUpdateIntervalMillis(100)
										.setInitialMax(swiftASTMapTree.size());
		
		try(ProgressBar pb = pbb.build()){
			swiftASTMapTree.stream().parallel().forEach( mapNode -> {
				Declaration declration = new Declaration();
				declration.makeDeclaration(mapNode, componentNodeMap);
				pb.step();
			});
		}
	}
	
	public List<Component> makeResult(){
		List<Component> components = new ArrayList<>();
		for (Entry<String, ASTComponentNode> entry : componentNodeMap.entrySet()) {
			components.add(entry.getValue().getComponent());
		}

		List<Component> result = components.stream()
											.sorted(Comparator.comparing(Component::getName))
											.collect(Collectors.toList());

		result.addAll(externalComponentManager.getComponents());

		return result;		
	}

	public List<String> extractJson(Path path){
		List<String> outputs = new ArrayList<>();
		String readLines = "";

		try{
			ProgressBarBuilder pbb = new ProgressBarBuilder()
										.setStyle(ProgressBarStyle.ASCII)
										.setTaskName("Read line of the AST File")
										.setUpdateIntervalMillis(100);

			for (String line : ProgressBar.wrap(Files.readAllLines(path), pbb)){
				if(line.contains("output") && line.contains("(source_file")){
					try {
						String sourceFileData = line.substring(line.indexOf("(source_file"), line.lastIndexOf(","));
						readLines = "{\"output\":\"" + sourceFileData + "}";	
					} catch (Exception e) {
						logger.error(line);
						continue;
					}
					
					JsonElement element = JsonParser.parseString(readLines);

					if (element.getAsJsonObject().get("output") != null){
						String data = element.getAsJsonObject().get("output").getAsString();
						String[] arrayStr = data.lines().toArray(String[]::new);
						ArrayList<String> strList = new ArrayList<>();
						for(int i = 0; i < arrayStr.length; i++){
							if ( arrayStr[i].equals("") || arrayStr[i].replace(" ", "").equals("")){
								continue;
							}

							if ( arrayStr[i].charAt(0) != '(' && arrayStr[i].charAt(0) != ' '){
								if(strList.size() > 0){
									String previous = strList.get(strList.size() -1);
									strList.remove(strList.size() -1);
									strList.add(previous.concat(arrayStr[i]));
								}
							}
							else{
								if( arrayStr[i].replace(" ", "").charAt(0) != '(' 
								&& ( (arrayStr[i].lastIndexOf(",") == arrayStr[i].length() -1 ) 
									|| arrayStr[i].lastIndexOf(")") == arrayStr[i].length() -1 )){
									if(strList.size() > 0){
										String previous = strList.get(strList.size() -1);
										strList.remove(strList.size() -1);
										strList.add(previous.concat(arrayStr[i].replace(" ", "")));
									}
								}
								else{
									strList.add(arrayStr[i]);
								}
							}
						}
						outputs.add(String.join(System.lineSeparator(), strList));
					}
				}
			}
		}
	 	catch (IOException e) {
			System.out.println("[Error] (IOException) extractJson");
			logger.error("extractJson");
			return null;
		}
		return outputs;
	}

	public void makeDependency(){
		AbstractDependency dependencyImport = new DependencyImport(this.projectName, "(import_decl");	
		dependencyImport.setDependency(componentNodeMap, externalComponentManager);

		AbstractDependency dependencyInherits = new DependencyInherits(this.projectName, "inherits");
		dependencyInherits.setDependency(componentNodeMap, externalComponentManager);

		AbstractDependency dependencyTopLevelCode = new DependencyTopLevelCode(this.projectName, "(top_level_code_decl");
		dependencyTopLevelCode.setDependency(componentNodeMap, externalComponentManager);

		AbstractDependency dependencyPatternBinding = new DependencyPatternBinding(this.projectName, "(pattern_binding_decl");
		dependencyPatternBinding.setDependency(componentNodeMap, externalComponentManager);
		
		AbstractDependency dependencyPatternOptional = new DependencyPatternOptional(this.projectName, "(pattern_optional_some");
		dependencyPatternOptional.setDependency(componentNodeMap, externalComponentManager);

		AbstractDependency dependencyFunction = new DependencyFunction(this.projectName, "(func_decl");
		dependencyFunction.setDependency(componentNodeMap, externalComponentManager);

		AbstractDependency dependencyVariable = new DependencyVariable(this.projectName, "(var_decl");
		dependencyVariable.setDependency(componentNodeMap, externalComponentManager);
	}
}
