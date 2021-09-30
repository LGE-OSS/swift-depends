/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.persistence;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;

public class JsonRepositoryTest {
	private String jsonString;
	private List<Component> components = new ArrayList<Component>();

	@Before
	public void setUp() throws Exception {
		jsonString = "[\n"
				+ "  {\n"
				+ "    \"name\": \"UIKIT\",\n"
				+ "    \"path\": \"\",\n"
				+ "    \"type\": \"EXTERNAL\",\n"
				+ "    \"components\": [\n"
				+ "      {\n"
				+ "        \"Component\": \"Element\",\n"
				+ "        \"name\": \"UILabel\",\n"
				+ "        \"path\": \"\",\n"
				+ "        \"type\": \"EXTERNAL\",\n"
				+ "        \"dependencies\": []\n"
				+ "      },\n"
				+ "      {\n"
				+ "        \"Component\": \"Element\",\n"
				+ "        \"name\": \"UIKIT.UILabel.text\",\n"
				+ "        \"path\": \"\",\n"
				+ "        \"type\": \"EXTERNAL\",\n"
				+ "        \"dependencies\": []\n"
				+ "      }\n"
				+ "    ],\n"
				+ "    \"dependencies\": []\n"
				+ "  },\n"
				+ "  {\n"
				+ "    \"name\": \"AppDelegate.swift\",\n"
				+ "    \"path\": \"GoodAsOldPhones/AppDelegate.swift\",\n"
				+ "    \"type\": \"FILE\",\n"
				+ "    \"components\": [\n"
				+ "      {\n"
				+ "        \"Component\": \"Module\",\n"
				+ "        \"name\": \"AppDelegate\",\n"
				+ "        \"path\": \"GoodAsOldPhones/AppDelegate.swift\",\n"
				+ "        \"type\": \"CLASS\",\n"
				+ "        \"components\": [\n"
				+ "          {\n"
				+ "            \"Component\": \"Element\",\n"
				+ "            \"name\": \"application\",\n"
				+ "            \"path\": \"GoodAsOldPhones/AppDelegate.swift\",\n"
				+ "            \"type\": \"FUNCTION\",\n"
				+ "            \"dependencies\": []\n"
				+ "          },\n"
				+ "          {\n"
				+ "            \"Component\": \"Element\",\n"
				+ "            \"name\": \"window\",\n"
				+ "            \"path\": \"GoodAsOldPhones/AppDelegate.swift\",\n"
				+ "            \"type\": \"VARIABLE\",\n"
				+ "            \"dependencies\": []\n"
				+ "          }\n"
				+ "        ],\n"
				+ "        \"dependencies\": []\n"
				+ "      }\n"
				+ "    ],\n"
				+ "    \"dependencies\": []\n"
				+ "  },\n"
				+ "  {\n"
				+ "    \"name\": \"ContactViewController.swift\",\n"
				+ "    \"path\": \"GoodAsOldPhones/ContactViewController.swift\",\n"
				+ "    \"type\": \"FILE\",\n"
				+ "    \"components\": [\n"
				+ "      {\n"
				+ "        \"Component\": \"Module\",\n"
				+ "        \"name\": \"ContactViewController\",\n"
				+ "        \"path\": \"GoodAsOldPhones/ContactViewController.swift\",\n"
				+ "        \"type\": \"CLASS\",\n"
				+ "        \"components\": [\n"
				+ "          {\n"
				+ "            \"Component\": \"Element\",\n"
				+ "            \"name\": \"viewDidLoad\",\n"
				+ "            \"path\": \"GoodAsOldPhones/ContactViewController.swift\",\n"
				+ "            \"type\": \"FUNCTION\",\n"
				+ "            \"dependencies\": [\n"
				+ "              {\n"
				+ "                \"toComponent_name\": \"application\",\n"
				+ "                \"toComponent_path\": \"GoodAsOldPhones/AppDelegate.swift\",\n"
				+ "                \"toComponent_type\": \"FUNCTION\",\n"
				+ "                \"type\": \"INVOCATION\",\n"
				+ "                \"location\": \"3:3\"\n"
				+ "              },\n"
				+ "              {\n"
				+ "                \"toComponent_name\": \"UILabel\",\n"
				+ "                \"toComponent_path\": \"\",\n"
				+ "                \"toComponent_type\": \"EXTERNAL\",\n"
				+ "                \"type\": \"REFERENCE\",\n"
				+ "                \"location\": \"5:5\"\n"
				+ "              }\n"
				+ "            ]\n"
				+ "          },\n"
				+ "          {\n"
				+ "            \"Component\": \"Element\",\n"
				+ "            \"name\": \"viewDidLayoutSubviews\",\n"
				+ "            \"path\": \"GoodAsOldPhones/ContactViewController.swift\",\n"
				+ "            \"type\": \"FUNCTION\",\n"
				+ "            \"dependencies\": [\n"
				+ "              {\n"
				+ "                \"toComponent_name\": \"UIKIT.UILabel.text\",\n"
				+ "                \"toComponent_path\": \"\",\n"
				+ "                \"toComponent_type\": \"EXTERNAL\",\n"
				+ "                \"type\": \"REFERENCE\",\n"
				+ "                \"location\": \"6:6\"\n"
				+ "              }\n"
				+ "            ]\n"
				+ "          },\n"
				+ "          {\n"
				+ "            \"Component\": \"Element\",\n"
				+ "            \"name\": \"scrollView\",\n"
				+ "            \"path\": \"GoodAsOldPhones/ContactViewController.swift\",\n"
				+ "            \"type\": \"VARIABLE\",\n"
				+ "            \"dependencies\": []\n"
				+ "          }\n"
				+ "        ],\n"
				+ "        \"dependencies\": []\n"
				+ "      }\n"
				+ "    ],\n"
				+ "    \"dependencies\": []\n"
				+ "  }\n"
				+ "]";
		
		Component uikit = new Module("UIKIT", ComponentType.EXTERNAL);
		Component uilabel = new Element("UILabel", ComponentType.EXTERNAL);
		Component text= new Element("UIKIT.UILabel.text", ComponentType.EXTERNAL);
		uikit.add(uilabel);
		uikit.add(text);
		components.add(uikit);
		
		Component m1 = new Module("AppDelegate.swift", ComponentType.FILE, "GoodAsOldPhones/AppDelegate.swift");
		Component m1_1 = new Module("AppDelegate", ComponentType.CLASS, "GoodAsOldPhones/AppDelegate.swift");
		Component e1_1_1 = new Element("application", ComponentType.FUNCTION, "GoodAsOldPhones/AppDelegate.swift");
		Component e1_1_2 = new Element("window", ComponentType.VARIABLE, "GoodAsOldPhones/AppDelegate.swift");
		m1_1.add(e1_1_1);
		m1_1.add(e1_1_2);
		m1.add(m1_1);
		components.add(m1);
		
		Component m2 = new Module("ContactViewController.swift", ComponentType.FILE, "GoodAsOldPhones/ContactViewController.swift");
		Component m2_1 = new Module("ContactViewController", ComponentType.CLASS, "GoodAsOldPhones/ContactViewController.swift");
		Component e2_1_1 = new Element("viewDidLoad", ComponentType.FUNCTION, "GoodAsOldPhones/ContactViewController.swift");
		e2_1_1.addDependency(new Dependency(e1_1_1, DependencyType.INVOCATION, "3:3"));
		e2_1_1.addDependency(new Dependency(uilabel, DependencyType.REFERENCE, "5:5"));
		
		Component e2_1_2 = new Element("viewDidLayoutSubviews", ComponentType.FUNCTION, "GoodAsOldPhones/ContactViewController.swift");
		e2_1_2.addDependency(new Dependency(text, DependencyType.REFERENCE, "6:6"));
		Component e2_1_3 = new Element("scrollView", ComponentType.VARIABLE, "GoodAsOldPhones/ContactViewController.swift");		
		m2_1.add(e2_1_1);
		m2_1.add(e2_1_2);	
		m2_1.add(e2_1_3);
		m2.add(m2_1);
		components.add(m2);

		final String test_dir = "test_dir/";
		if (System.getProperty("logDirectory") == null){
            System.setProperty("logDirectory", test_dir);
        }
	}
	
	@Test
	public void test() {	
		Path jsonPath = Paths.get("src/test/resources/json/JsonRepositoryTest.json");
		JsonRepository repository = new JsonRepository(jsonPath);
		repository.create(components);
		
		try {
			assertTrue(jsonString.equalsIgnoreCase(Files.readString(jsonPath, StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
