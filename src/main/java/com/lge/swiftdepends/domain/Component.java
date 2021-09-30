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

import java.util.List;

public interface Component{
	public String getName();
	public String getPath();
	public ComponentType getType();
	public Component getParent();
	public void setParent(Component comp);
	public void add(Component comp);
	public void addDependency(Dependency dep);
	public List<Component> getComponents();
	public List<Dependency> getDependencies();
	public int getDependencyStrengh();
	public void accecpt(Visitor visitor);
	public void setPath(String path);
	public void setComponents(List<Component> components);
}
