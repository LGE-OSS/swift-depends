/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.generator;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XcConfigExtractor {
	private String xcConfigFilePath = "";
	private String PROJECT_SOURCE_ROOT = "";
	private String PODS_CONFIGURATION_BUILD_DIR = "";
	private String PODS_ROOT = "";
	private String PODS_XCFRAMEWORKS_BUILD_DIR = "";
	private String PODS_TARGET_SRCROOT = "";
	private String SDK_ROOT = "";
	private String OBJC_HEADER_SEARCH_PATH = "";
	private String FRAMEWORK_SEARCH_PATHS = "";
	private String OTHER_SWIFT_FLAGS = "";
	private String LIBRARY_SEARCH_PATHS = "";
	private String SWIFT_INCLUDE_PATHS = "";
	private String HEADER_SEARCH_PATHS = "";
	private String OTHER_LDFLAGS = "";
	private String OTHER_FRAMEWORK_SEARCH_PATH = "";
	
	public XcConfigExtractor(String path) {
		this.xcConfigFilePath = path;
		LoadXcConfigFile();
	}
	
	public void setPodConfigurationBuildDir(Map<String, String> configPaths) {
		if(configPaths.containsKey("PROJECT_SOURCE_ROOT") && 
			configPaths.containsKey("PODS_CONFIGURATION_BUILD_DIR") &&
			configPaths.containsKey("PODS_ROOT") &&
			configPaths.containsKey("PODS_XCFRAMEWORKS_BUILD_DIR") && 
			configPaths.containsKey("PODS_TARGET_SRCROOT") && 
			configPaths.containsKey("SDK_ROOT") &&
			configPaths.containsKey("OBJC_HEADER_SEARCH_PATH") && 
			configPaths.containsKey("OTHER_FRAMEWORK_SEARCH_PATH")) {
			
			PROJECT_SOURCE_ROOT = configPaths.get("PROJECT_SOURCE_ROOT");
			PODS_CONFIGURATION_BUILD_DIR = configPaths.get("PODS_CONFIGURATION_BUILD_DIR");
			PODS_ROOT = configPaths.get("PODS_ROOT");
			PODS_XCFRAMEWORKS_BUILD_DIR = configPaths.get("PODS_XCFRAMEWORKS_BUILD_DIR");
			PODS_TARGET_SRCROOT = configPaths.get("PODS_TARGET_SRCROOT");
			SDK_ROOT = configPaths.get("SDK_ROOT");
			OBJC_HEADER_SEARCH_PATH = configPaths.get("OBJC_HEADER_SEARCH_PATH");
			OTHER_FRAMEWORK_SEARCH_PATH = configPaths.get("OTHER_FRAMEWORK_SEARCH_PATH");
		}
	}
	
	private void LoadXcConfigFile() {
		
		if(this.xcConfigFilePath.length() != 0) {
			
			InputStreamReader resources = null;
			
			try {
				resources = new InputStreamReader(new FileInputStream(this.xcConfigFilePath), StandardCharsets.UTF_8);
				Properties properties = new Properties();
				properties.load(resources);
				
				this.FRAMEWORK_SEARCH_PATHS = properties.getProperty("FRAMEWORK_SEARCH_PATHS");
				this.OTHER_SWIFT_FLAGS = properties.getProperty("OTHER_SWIFT_FLAGS");
				this.LIBRARY_SEARCH_PATHS = properties.getProperty("LIBRARY_SEARCH_PATHS");
				this.SWIFT_INCLUDE_PATHS = properties.getProperty("SWIFT_INCLUDE_PATHS");
				this.HEADER_SEARCH_PATHS = properties.getProperty("HEADER_SEARCH_PATHS");
				this.OTHER_LDFLAGS = properties.getProperty("OTHER_LDFLAGS");
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[Error] Could not open file: " + xcConfigFilePath);
				
			} finally {
				try {
					resources.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public List<String> getFrameworkSearchPath() {
		
		if(this.FRAMEWORK_SEARCH_PATHS == null) {
			return new ArrayList<String>();
		}
		
		List<String> frameworkSearchPath = Arrays.asList(FRAMEWORK_SEARCH_PATHS.split(" "))
									.stream().filter(s -> !s.contains("inherited"))
									.map(s -> s.replace("${PODS_CONFIGURATION_BUILD_DIR}", PODS_CONFIGURATION_BUILD_DIR))
									.map(s -> s.replace("${PODS_XCFRAMEWORKS_BUILD_DIR}", PODS_XCFRAMEWORKS_BUILD_DIR))
									.map(s -> s.replace("${PODS_ROOT}", PODS_ROOT))
									.map(s -> s = "-F " + s )
									.collect(Collectors.toList());
		
		if(this.OTHER_FRAMEWORK_SEARCH_PATH == null) {
			return frameworkSearchPath;
		}
		
		List<String> otherPaths = findFrameworkPath(OTHER_FRAMEWORK_SEARCH_PATH);
		for(String path : otherPaths) {
			frameworkSearchPath.add("-F \"" + path + "\"");
		}
		return frameworkSearchPath;
	}
	
	public List<String> getModuleMap() {
		
		if(this.OTHER_SWIFT_FLAGS == null) {
			return new ArrayList<String>();
		}
		
		List<String> moduleMapList = Arrays.asList(OTHER_SWIFT_FLAGS.split(" "))
							  .stream().filter(s -> s.contains("-fmodule-map-file"))
							  .map(s -> s.replace("${PODS_CONFIGURATION_BUILD_DIR}", PODS_CONFIGURATION_BUILD_DIR))
							  .map(s -> s.replace("${PODS_ROOT}", PODS_ROOT))
							  .collect(Collectors.toList());
		
		return moduleMapList;
	}
	
	public List<String> getLibrarySearchPath() {
		
		if(this.LIBRARY_SEARCH_PATHS == null) {
			return new ArrayList<String>();
		}
		
		List<String> librarySearchPath = Arrays.asList(LIBRARY_SEARCH_PATHS.split(" "))
								  .stream().filter(s -> !s.contains("inherited"))
								  .map(s -> s.replace("${PODS_CONFIGURATION_BUILD_DIR}", PODS_CONFIGURATION_BUILD_DIR))
								  .map(s -> s.replace("${PODS_XCFRAMEWORKS_BUILD_DIR}", PODS_XCFRAMEWORKS_BUILD_DIR))
								  .map(s -> s.replace("${PODS_ROOT}", PODS_ROOT))
								  .map(s -> s = "-L " + s)
								  .collect(Collectors.toList());
		return librarySearchPath;
	}
	
	public List<String> getLDFlags() {
		
		if(this.OTHER_LDFLAGS == null) {
			return new ArrayList<String>();
		}
		
		List<String> ldflags = Arrays.asList(OTHER_LDFLAGS.split(" "))
						.stream().filter(s -> s.startsWith("-l"))
						.collect(Collectors.toList());
		return ldflags;
	}
	
	public List<String> getIncludePath() {
		
		if(this.SWIFT_INCLUDE_PATHS == null) {
			return new ArrayList<String>();
		}
		
		List<String> includes = Arrays.asList(SWIFT_INCLUDE_PATHS.split(" "))
						.stream().filter(s -> !s.contains("inherited"))
					    .map(s -> s.replace("${PODS_CONFIGURATION_BUILD_DIR}", PODS_CONFIGURATION_BUILD_DIR))
					    .map(s -> s.replace("${PODS_ROOT}", PODS_ROOT))
					    .map(s -> s = "-I" + s)
					    .collect(Collectors.toList());
		includes.add("-I\""+PODS_CONFIGURATION_BUILD_DIR+"/include\"");
		return includes;
	}
	
	public List<String> getPodsHeaderSearchPath() {
		
		if(this.HEADER_SEARCH_PATHS == null) {
			return new ArrayList<String>();
		}
		
		List<String> headerSearchPaths = Arrays.asList(HEADER_SEARCH_PATHS.split(" "))
				  				  .stream().filter(s -> !s.contains("inherited"))
				  				  .map(s -> s.replace("${PODS_CONFIGURATION_BUILD_DIR}", PODS_CONFIGURATION_BUILD_DIR))
				  				  .map(s -> s.replace("${PODS_XCFRAMEWORKS_BUILD_DIR}", PODS_XCFRAMEWORKS_BUILD_DIR))
				  				  .map(s -> s.replace("${PODS_ROOT}", PODS_ROOT))
				  				  .map(s -> s.replace("${PODS_TARGET_SRCROOT}", PODS_TARGET_SRCROOT))
				  				  .map(s -> s.replace("$(SDKROOT)", SDK_ROOT))
				  				  .map(s -> s = "-I" + s)
				  				  .collect(Collectors.toList());
		return headerSearchPaths;
	}
	
	public List<String> getSourceIncludePath() {
		
		List<String> paths = new ArrayList<String>();
		
		if(this.PROJECT_SOURCE_ROOT != null) {
			paths.addAll(searchHeaderFilePath(PROJECT_SOURCE_ROOT));
		}
		
		if(this.OBJC_HEADER_SEARCH_PATH != null) {
			paths.addAll(searchHeaderFilePath(OBJC_HEADER_SEARCH_PATH));
		}
				
		List<String> sourceIncludePaths = paths.stream().distinct()
				.filter(s -> !s.contains(PODS_ROOT))
				.filter(s -> !s.contains(PODS_TARGET_SRCROOT))
				.map(s -> "-I"+"\""+s.toString()+"\"")
				.collect(Collectors.toList());
		
		return sourceIncludePaths;
	}

	private List<String> searchHeaderFilePath(String path) {
		List<String> paths = new ArrayList<String>();
		
		try {
			Stream<Path> entries = Files.find(Paths.get(path), Integer.MAX_VALUE, (s, attr) -> s.toString().endsWith(".h"));
			
			entries.forEach( p -> {
				paths.add(p.getParent().toString());
			});	
			entries.close();
		} catch (IOException e) {
			System.out.println("[Exception] when finding path in getSourceIncludePath()");
		}
		return paths;
	}
	
	private List<String> findFrameworkPath(String path) {
		List<String> paths = new ArrayList<String>();
		
		try {
			Stream<Path> entries = Files.find(Paths.get(path), 6, (s, attr) -> s.toString().endsWith(".framework"));
			entries.forEach( p -> {
				paths.add(p.getParent().toString());
			});	
			entries.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return paths.stream().distinct().collect(Collectors.toList());
	}
}
