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

import java.util.Vector;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.domain.Visitor;

public class AtomDependencyListCreator implements Visitor {
	private Vector<Atom> atomList = null;
	private Vector<AtomDependency> atomDependencyList = null;

	public AtomDependencyListCreator() {
		atomList = new Vector<Atom>();	
		atomDependencyList = new Vector<AtomDependency>(); 
	}	
	
	public Vector<Atom> getAtomList() {
		return atomList;
	}	

	public Vector<AtomDependency> getAtomDependencyList() {
		return atomDependencyList;
	}
	
	private String createPathForLattixDSMView(Component component) {
		if (component.getType() == ComponentType.EXTERNAL)
			return "/" + component.getName().replace(".", "/");
		else
			return "/<" + component.getType().getIcon() + ">" + component.getName();
	}
	
	private String getAtomPath(Component component) {
		try {			
			return getAtomPath(component.getParent()) + createPathForLattixDSMView(component);			
		}catch (Exception e) {
			if (!component.getPath().equals(""))
				return component.getPath();
			else
				return createPathForLattixDSMView(component);
		}		
	}	

	@Override
	public void visit(Element element) {
		// External components are not added to the source atom list.
		if (element.getType() == ComponentType.EXTERNAL)
			return;
		
		// add atom
		atomList.add(new Atom(getAtomPath(element), element.getType().toString()));
		
		// add atom dependency
		element.getDependencies().forEach(dependency->{
			atomDependencyList.add(new AtomDependency(getAtomPath(element), getAtomPath(dependency.getDependsOnComponent())));
		});		
	}

	@Override
	public void visit(Module module) {	
		// External components are not added to the source atom list.
		if (module.getType() == ComponentType.EXTERNAL)
			return;
		
		// add atom
		atomList.add(new Atom(getAtomPath(module), module.getType().toString()));
		
		// add atom dependency
		module.getDependencies().forEach(dependency->{
			atomDependencyList.add(new AtomDependency(getAtomPath(module), getAtomPath(dependency.getDependsOnComponent())));
		});
		
		module.getComponents().forEach(component->{component.accecpt(this);});		
	}
}
