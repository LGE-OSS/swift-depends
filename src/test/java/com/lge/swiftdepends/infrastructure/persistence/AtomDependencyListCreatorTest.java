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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;

public class AtomDependencyListCreatorTest{
	
private List<Component> components = new ArrayList<Component>();
	
	@Before
	public void setUp() throws Exception {
		Component uikit = new Module("UIKIT", ComponentType.EXTERNAL);
		Component uilabel = new Element("UILabel", ComponentType.EXTERNAL);
		Component text= new Element("UIKIT.UILabel.text", ComponentType.EXTERNAL);
		uikit.add(uilabel);
		uikit.add(text);
		components.add(uikit);
		
		Component m1 = new Module("Png.swift", ComponentType.FILE, "Png/Png.swift");
		m1.addDependency(new Dependency(uikit, "2:2"));
		Component m1_1 = new Module("Image", ComponentType.PROTOCOL, "Png/Png.swift");
		Component e1_1_1 = new Element("filename", ComponentType.VARIABLE, "Png/Png.swift");
		Component e1_1_2 = new Element("filesize", ComponentType.VARIABLE, "Png/Png.swift");
		Component e1_1_3 = new Element("save", ComponentType.FUNCTION, "Png/Png.swift");
		m1_1.add(e1_1_1);
		m1_1.add(e1_1_2);
		m1_1.add(e1_1_3);
		m1.add(m1_1);
		
		Component m1_2 = new Module("Png", ComponentType.CLASS, "Png/Png.swift");
		m1_2.addDependency(new Dependency(m1_1, "6:6"));
		Component e1_2_1 = new Element("filename", ComponentType.VARIABLE, "Png/Png.swift");
		Component e1_2_2 = new Element("filesize", ComponentType.VARIABLE, "Png/Png.swift");
		Component e1_2_3 = new Element("init", ComponentType.FUNCTION, "Png/Png.swift");
		Component e1_2_4 = new Element("save", ComponentType.FUNCTION, "Png/Png.swift");
		m1_2.add(e1_2_1);
		m1_2.add(e1_2_2);
		m1_2.add(e1_2_3);
		m1_2.add(e1_2_4);
		m1.add(m1_2);
		
		components.add(m1);
	}

	@Test
	public void test() {							
		AtomDependencyListCreator creator = new AtomDependencyListCreator();		
		components.forEach(component->{
			component.accecpt(creator);
		});
		
		creator.getAtomList().forEach(atom->{System.out.println(atom);});					
		assertTrue(creator.getAtomList().size() == 10);		
		
		creator.getAtomDependencyList().forEach(atom->{System.out.println(atom);});				
		assertTrue(creator.getAtomDependencyList().size() == 2);			
	}
}
