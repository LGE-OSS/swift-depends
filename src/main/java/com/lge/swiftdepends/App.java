/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends;

import org.apache.commons.cli.ParseException;

import com.lge.swiftdepends.application.SwiftDependsApplicationService;
import com.lge.swiftdepends.presentation.SwiftDependsCLI;

public class App {

	public static void main(String[] args) {
		
		SwiftDependsCLI cmdUI = new SwiftDependsCLI(new SwiftDependsApplicationService());
		try {
			cmdUI.start(args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}