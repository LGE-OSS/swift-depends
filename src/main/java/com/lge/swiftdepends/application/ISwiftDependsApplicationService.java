/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.application;

import java.nio.file.Path;

public interface ISwiftDependsApplicationService {
	public boolean analyzeDependency(String projectName, Path inputPath, Path outputPath);
}
