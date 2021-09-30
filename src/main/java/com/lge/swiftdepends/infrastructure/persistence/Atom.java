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

public class Atom {
	private String sourceAtom; 
	private String atomKind;		

	public Atom(String sourceAtom, String atomKind) {		
		this.sourceAtom = sourceAtom;
		this.atomKind = atomKind;
	}

	public String getSourceAtom() {
		return sourceAtom;
	}

	public void setSourceAtom(String sourceAtom) {
		this.sourceAtom = sourceAtom;
	}

	public String getAtomKind() {
		return atomKind;
	}

	public void setAtomKind(String atomKind) {
		this.atomKind = atomKind;
	}

	@Override
	public String toString() {
		return "Atom [sourceAtom=" + sourceAtom + ", atomKind=" + atomKind + "]";
	}
}
