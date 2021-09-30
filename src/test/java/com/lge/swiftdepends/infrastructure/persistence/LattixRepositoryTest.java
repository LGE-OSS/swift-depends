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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;

public class LattixRepositoryTest {
	
	private List<Component> components = new ArrayList<Component>();
	
	@Before
	public void setUp() throws Exception {
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
		e2_1_1.addDependency(new Dependency(e1_1_1, "3:3"));
		e2_1_1.addDependency(new Dependency(uilabel, "5:5"));
		Component e2_1_2 = new Element("viewDidLayoutSubviews", ComponentType.FUNCTION, "GoodAsOldPhones/ContactViewController.swift");
		e2_1_2.addDependency(new Dependency(text, "6:6"));
		Component e2_1_3 = new Element("scrollView", ComponentType.VARIABLE, "GoodAsOldPhones/ContactViewController.swift");		
		m2_1.add(e2_1_1);
		m2_1.add(e2_1_2);	
		m2_1.add(e2_1_3);
		m2.add(m2_1);
		components.add(m2);		
	}
	
	@Test
	public void test() {	
		Path path = Paths.get("src/test/java/com/lge/swiftdepends/infrastructure/persistence/lattixRepository.xlsx");
				
		LattixRepository repository = new LattixRepository(path);
		repository.create(components);
	}
}
