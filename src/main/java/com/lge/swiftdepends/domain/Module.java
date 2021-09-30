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

public class Module implements Component{

	private String name;
	private String path;
	private ComponentType type;
	private transient Component parent;
	private List<Component> components = new ArrayList<Component>();
	private List<Dependency> dependencies = new ArrayList<Dependency>();
	
	public Module(String name, ComponentType type) {
		this.name = name;
		this.type = type;
		this.path = "";
		this.parent = null;
	}
	
	public Module(String name, ComponentType type, String path) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.parent = null;
	}
	
	public Module(String name, ComponentType type, String path, Component parent) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.parent = parent;
	}
	
	@Override
	public String getPath() {
		return this.path;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public ComponentType getType() {
		return this.type;
	}
	
	@Override
	public Component getParent() {
		if(this.type != ComponentType.FILE && this.type != null) {
			return this.parent;
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public void setParent(Component comp) {
		if(this.type != ComponentType.FILE) {
			this.parent = comp;
		}
	}
	
	@Override
	public void add(Component comp) {
		this.components.add(comp);
		comp.setParent(this);
	}

	@Override
	public void addDependency(Dependency dep) {
		this.dependencies.add(dep);
	}
	
	@Override
	public List<Component> getComponents() {
		return this.components;
	}
	
	@Override
	public List<Dependency> getDependencies() {		
		return this.dependencies;
	}

	@Override
	public int getDependencyStrengh() {
		return this.dependencies.size();
	}

	@Override
	public void accecpt(Visitor visitor) {
		visitor.visit(this);		
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Module [name=" + name + ", path=" + path + ", type=" + type + ", parent=" + parent 
				+ ", components=" + components.size() + ", dependencies=" + dependencies + "]";
	}

	@Override
	public void setComponents(List<Component> components) {
		this.components = components;
	}
}
