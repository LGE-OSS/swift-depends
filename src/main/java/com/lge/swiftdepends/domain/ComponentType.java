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

public enum ComponentType {
	FILE(""),
	PROTOCOL("P"),
	CLASS("C"),
	STRUCT("S"),
	ENUM("E"),
	METHOD("M"),
	FUNCTION("F"),
	VARIABLE("V"),
	EXTERNAL("");
	
	private final String icon;
	
	ComponentType(String icon) { this.icon = icon; }
	
	public String getIcon() { return this.icon; }
}
