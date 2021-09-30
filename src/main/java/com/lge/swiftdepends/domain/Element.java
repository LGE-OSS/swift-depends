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

import java.util.ArrayList;
import java.util.List;

public class Element implements Component{

	private String name;
	private String path;
	private ComponentType type;
	private transient Component parent;
	private List<Dependency> dependencies = new ArrayList<Dependency>();
	
	public Element(String name, ComponentType type) {
		this.name = name;
		this.type = type;
		this.path = "";
		this.parent = null;
	}
	
	public Element(String name, ComponentType type, String path) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.parent = null;
	}
	
	public Element(String name, ComponentType type, String path, Component parent) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.parent = parent;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getPath() {
		return this.path;
	}
	
	@Override
	public ComponentType getType() {
		return this.type;
	}
	
	@Override
	public Component getParent() {
		if(this.parent != null)	{
			return this.parent;
		}
		else {
			throw new ComponentException(this.toString() + "has no parent");
		}
	}
	
	public void setParent(Component comp) {
		this.parent = comp;
	}
	
	@Override
	public void add(Component comp) {
		throw new UnsupportedOperationException();	
	}

	@Override
	public void addDependency(Dependency dep) {
		this.dependencies.add(dep);
	}

	public List<Component> getComponents() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<Dependency> getDependencies() {
		return this.dependencies;
	}

	@Override
	public int getDependencyStrengh() {
		return this.dependencies.size();

	}
	
	public void accecpt(Visitor visitor) {
		visitor.visit(this);			
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Element [name=" + name + ", path=" + path + ", type=" + type + ", parent=" + parent + ", dependencies="
				+ dependencies + "]";
	}

	@Override
	public void setComponents(List<Component> components) {
		throw new UnsupportedOperationException();	
	}
}
