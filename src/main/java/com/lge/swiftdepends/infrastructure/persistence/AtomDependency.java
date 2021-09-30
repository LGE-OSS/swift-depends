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

public class AtomDependency {	
	private String sourceAtom; 
	private String targetAtom;		

	public AtomDependency(String sourceAtom, String targetAtom) {
		this.sourceAtom = sourceAtom;
		this.targetAtom = targetAtom;
	}

	public String getSourceAtom() {
		return sourceAtom;
	}

	public void setSourceAtom(String sourceAtom) {
		this.sourceAtom = sourceAtom;
	}

	public String getTargetAtom() {
		return targetAtom;
	}

	public void setTargetAtom(String targetAtom) {
		this.targetAtom = targetAtom;
	}

	@Override
	public String toString() {
		return "AtomDependency [sourceAtom=" + sourceAtom + ", targetAtom=" + targetAtom + "]";
	}
}
