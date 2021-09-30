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

import java.util.HashMap;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.domain.Visitor;

public class ComponentHashMapCreator implements Visitor {
	private HashMap<String, Component> components = null; 

	public ComponentHashMapCreator() {
		components = new HashMap<String, Component>();
	}
	
	private String makeKeyValue(Component component) {
		return component.getName() + "@" + component.getType().toString() + "@" + component.getPath();
	}
	
	public Component findComponent(Component component) {
		Component find = null;
		
		if (components.containsKey(makeKeyValue(component))) {
			find = (Component)components.get(makeKeyValue(component));
		} 
		
		return find;
	}

	@Override
	public void visit(Element element) {
		if (components.containsKey(makeKeyValue(element))){
			System.out.println("The same value ​​exist: " + makeKeyValue(element));
		} else {
			components.put(makeKeyValue(element), element);
		}
	}

	@Override
	public void visit(Module module) {
		if (components.containsKey(makeKeyValue(module))){
			System.out.println("The same value ​​exist: " + makeKeyValue(module));
		} else {
			components.put(makeKeyValue(module), module);
		}
		module.getComponents().forEach(component->{component.accecpt(this);});	
	}
}
