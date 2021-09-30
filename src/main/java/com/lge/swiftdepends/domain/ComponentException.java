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

public class ComponentException extends UnsupportedOperationException{

	private static final long serialVersionUID = 1L;
	private String message = "";
	
	public ComponentException(String message) {
		super(message);
		this.message = message;
	}
	
	public String toString() {
		return ("[Exception Occurred] " + this.message);
		
	}
}
