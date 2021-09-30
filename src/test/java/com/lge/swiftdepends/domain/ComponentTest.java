/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.domain;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ComponentTest {
	
	private List<Component> components = new ArrayList<Component>();
	
	@Before
	public void setUp() throws Exception {
		//Create Component(Module, Element) and add components
		Component m1 = new Module("Product", ComponentType.CLASS);
		Component e1 = new Element("name", ComponentType.VARIABLE);
		Component e2 = new Element("getName", ComponentType.METHOD);
		m1.add(e1);
		m1.add(e2);

		Component m2 = new Module("App", ComponentType.CLASS);
		Component m3 = new Module("Main", ComponentType.CLASS);
		Component f1 = new Element("main", ComponentType.METHOD);
		f1.addDependency(new Dependency(e1, "3:3"));
		f1.addDependency(new Dependency(e2, "4:6"));
		
		Component f2 = new Element("print", ComponentType.FUNCTION);
		f2.addDependency(new Dependency(e2, "4:6"));
		
		m3.add(f1);
		m3.add(f2);
		m2.add(m3);
		
		components.add(m1);
		components.add(m2);
	}

	@Test
	public void testgetName() {
		assertTrue(components.get(0).getName() == "Product");
		assertTrue(components.get(1).getName() == "App");
	}
	
	@Test
	public void testgetComponents() {
		assertTrue(components.get(0).getComponents().size() == 2);
		assertTrue(components.get(1).getComponents().size() == 1);
	}	
	
	@Test	
	public void testgetDependencies() {
		assertTrue(components.get(0).getDependencies().size() == 0);
		assertTrue(components.get(1).getDependencies().size() == 0);
	}
	
	
	@Test
	public void testgetDependencyStrength() {
		assertTrue(components.get(0).getDependencyStrengh() == 0);
		assertTrue(components.get(1).getDependencies().size() == 0);
	}
}
