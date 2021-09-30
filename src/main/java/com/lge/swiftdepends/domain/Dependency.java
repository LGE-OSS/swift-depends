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

public class Dependency {
	private Component toComponent;
	private DependencyType type;
	private String location;
	
	public Dependency(Component comp, String location) {
		this.toComponent = comp;
		this.location = location;
	}
	
	public Dependency(Component comp, DependencyType type, String location) {
		this.toComponent = comp;
		this.type = type;
		this.location = location;
	}
	
	public Component getDependsOnComponent() {
		return this.toComponent;
	}
	
	public void setDependsOnComponent(Component comp) {
		this.toComponent = comp;
	}
	
	public DependencyType getType() {
		return this.type;
	}
	
	public String getLocation() {
		return this.location;
	}
	
	@Override
	public String toString() {
		return "Dependency [toComponent=" + toComponent + ", location=" + location + "]";
	}	
}
