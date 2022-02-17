/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.application;

import java.nio.file.Path;
import java.util.List;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentFactory;
import com.lge.swiftdepends.domain.ComponentRepository;
import com.lge.swiftdepends.infrastructure.generator.SwiftASTGenerator;
import com.lge.swiftdepends.infrastructure.parser.SwiftASTParser;
import com.lge.swiftdepends.infrastructure.persistence.JsonRepository;
import com.lge.swiftdepends.infrastructure.persistence.LattixRepository;
import com.lge.swiftdepends.infrastructure.persistence.LdiXmlRepository;

public class SwiftDependsApplicationService implements ISwiftDependsApplicationService {

	@Override
	public boolean analyzeDependency(String projectName, Path inputPath, Path outputPath) {
		// Set the folder where the log will be saved
		if(System.getProperty("logDirectory") == null) {
			System.setProperty("logDirectory", outputPath.toString());
		}
		
		// create AST parsed json file 
		SwiftASTGenerator generator = new SwiftASTGenerator();
		if(!generator.generateAST(projectName, inputPath, outputPath)) {
			System.out.println("Fail to generate AST...");
			return false;
		}
		
		// create Component object list from AST and dependency analysis
		System.out.println("\nDependency Analysis start...");
		ComponentFactory factory = new ComponentFactory();
		List<Component> components = factory.createComponents(new SwiftASTParser(), projectName, outputPath.resolve(projectName + "_AST.json"));
		System.out.println("\nDependency Analysis is completed...");
		
		// create a excel file for lattix architect project
		System.out.println("Start creating an excel file for Lattix...");
		ComponentRepository lattixRepository = new LattixRepository(outputPath.resolve(projectName + ".xlsx"));
		if(!lattixRepository.create(components)) {
			System.out.println("Fail to create an excel file for Lattix...");
			return false;
		}
		System.out.println("Excel file creation is completed...");
		
		// Create a json file of component objects list
		System.out.println("Start creating a json file of component object list...");
		ComponentRepository jsonRepository = new JsonRepository(outputPath.resolve(projectName + ".json"));
		if (!jsonRepository.create(components)) {
			System.out.println("Fail to create a json file of component object list...");
			return false;
		}
		System.out.println("Json file creation is completed...");
		
		// Create a LDI XML file
		System.out.println("Start creating a LDI XML file...");
		ComponentRepository ldiXmlRepository = new LdiXmlRepository(outputPath.resolve(projectName + ".ldi.xml"));
		if (!ldiXmlRepository.create(components)) {
			System.out.println("Fail to create a LDI XML file...");
			return false;
		}
		System.out.println("LDI XML file creation is completed...");
		
		
		return true;
	}

}
