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

import java.nio.file.Path;
import java.util.List;

public class ComponentFactory {
	public List<Component> createComponents(ASTParser repository, String projectName, Path path) {
		return repository.read(projectName, path);
	}
}
