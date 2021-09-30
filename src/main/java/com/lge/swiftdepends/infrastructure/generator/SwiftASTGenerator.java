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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lge.swiftdepends.domain.ASTGenerator;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class SwiftASTGenerator implements ASTGenerator{

	private String configFile = "./swift-depends.config";
	private String target;
	private String sdkRoot;
	private String projectSourceRoot;
	private String xcConfigFilePath;
	private String podRoot;
	private String podConfigurationBuildDir;
	private String podxcFrameworkBuildDir;
	private String podTargetSourceRoot;
	private String bridgingHeader;
	private String objcHeaderSearchPath;
	private String otherFrameworkSearchPath;
	List<String> swiftFileList = new ArrayList<>();
	private XcConfigExtractor extractor;
	private String astLogString;
	private static final Logger logger = LogManager.getLogger(SwiftASTGenerator.class);
	
	public SwiftASTGenerator() {
		this.target = "";
		this.sdkRoot = "";
		this.projectSourceRoot = "";
		this.xcConfigFilePath = "";
		this.podRoot = "";
		this.podConfigurationBuildDir = "";
		this.podxcFrameworkBuildDir = "";
		this.podTargetSourceRoot = "";
		this.bridgingHeader = "";
		this.objcHeaderSearchPath = "";
		this.otherFrameworkSearchPath = "";
		this.astLogString = "";
	}
	
	public SwiftASTGenerator(String configFile) {
		this.configFile = configFile;
		this.target = "";
		this.sdkRoot = "";
		this.projectSourceRoot = "";
		this.xcConfigFilePath = "";
		this.podRoot = "";
		this.podConfigurationBuildDir = "";
		this.podxcFrameworkBuildDir = "";
		this.podTargetSourceRoot = "";
		this.bridgingHeader = "";
		this.objcHeaderSearchPath = "";
		this.otherFrameworkSearchPath = "";
		this.astLogString = "";
	}
	
	@Override
	public boolean generateAST(String projectName, Path inputPath, Path outputPath) {
		
		if(!this.loadSwiftDependsConfig()) {
			System.out.println("[Error] Missing \"swift-depends.config\" file");
			System.out.println("Please enter the appropriate settings in swift-depends.config");
			return false;
		}
		
		if(Files.isDirectory(inputPath)) {
			if(!this.findSwiftFile(inputPath)) 
				return false;
		}
		
		if(Files.isRegularFile(inputPath)) {
			if(!this.readSwiftFileList(inputPath)) 
				return false;
		}
		
		if(isFileDuplicated()) {
			printDuplicatedFileList();
			return false;
		}
		
		String astFilePath = outputPath.toString() + "/" + projectName + "_AST.json";
		List<String> command = createCommand(projectName);
		
		if(!executeCommand(command)) {
			return false;
		}
		System.out.println(astFilePath);
		return writeASTFile(astLogString, astFilePath);
	}

	private List<String> createCommand(String projectName) {
		List<String> command = new ArrayList<String>();
		command.add("swiftc");
		command.add("-dump-ast");	//dump AST
		command.add("-target");
		command.add(this.target);
		command.add("-sdk");
		command.add(this.sdkRoot);
		command.add("-parseable-output"); //print json format option
		command.add("-module-name");
		command.add(projectName);
		command.addAll(this.swiftFileList);
		
		if(this.xcConfigFilePath != null) {
			if(this.bridgingHeader != null) {
				command.add("-import-objc-header");
				command.add(this.bridgingHeader);
			}
			command.addAll(this.extractor.getFrameworkSearchPath());
			command.addAll(this.extractor.getLibrarySearchPath());
			command.addAll(this.extractor.getLDFlags());
			command.addAll(this.extractor.getIncludePath());
			command.addAll(this.extractor.getPodsHeaderSearchPath());
			command.addAll(this.extractor.getSourceIncludePath());
			
			this.extractor.getModuleMap().forEach( s -> {
				command.add("-Xcc");
				command.add(s);
			});

		}
		command.add("-Xcc");
		command.add("-Wno-nullability-completeness");
		command.add("-Xcc");
		command.add("-Wno-error=non-modular-include-in-framework-module");
		
		String cmd = command.stream().map(n -> n.toString()+" ").collect(Collectors.joining());
		logger.info(cmd);
		
		List<String> shellCommand = new ArrayList<String>();
		shellCommand.add("/bin/sh");
		shellCommand.add("-c");
		shellCommand.add(cmd);
		
		return shellCommand;
	}

	private boolean executeCommand(List<String> command) {
		
		StringBuilder silLog = new StringBuilder();
		boolean flag = false;
		int count = 0;
		
		ProgressBarBuilder pbb = new ProgressBarBuilder()
									.setStyle(ProgressBarStyle.ASCII)
									.setTaskName("AST Generation")
									.setUpdateIntervalMillis(1000)
									.setInitialMax(this.swiftFileList.size());
		
		try {
			Process p = new ProcessBuilder().command(command).start();
			System.out.println("\nAST Generation start...");
			
			ProgressBar pb = pbb.build();

			try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))){
				String line;
				while( (line = br.readLine()) != null) {
					silLog.append(line+"\n");
					
					if(line.contains("<unknown>:0: error:") || line.contains("\"exit-status\": 1")) {
						logger.error(silLog.toString());
						return false;
					}
					
					if(flag) {
						String filePath = line.trim().replace("\\", "").replace("\"", "").replace(",", "");
						if(filePath.endsWith(".swift")) {
							pb.step();
						}
					}
					
					if(line.contains("\"inputs\": ")) {
						flag = true;
					} else {
						flag = false;
					}
					
					if(line.contains("\"output\":") && line.contains("(source_file")) {
						int end = line.indexOf("(source_file");
						if(line.substring(0, end).contains(": error:") ) {
							String json = "{" + line.substring(0, end) + "\" }";
							JsonElement element = JsonParser.parseString(json);
							String data = element.getAsJsonObject().get("output").getAsString();
							Arrays.asList(data.lines().toArray(String[]::new)).forEach(System.out::println);
						} else {
							++count;
							
						}
					}					
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("AST Generation is completed...(" + count +"/"+ this.swiftFileList.size()+")");
		
		this.astLogString = silLog.toString();
		
		return true;
	}
	
	private boolean writeASTFile(String astLogString, String astFilePath) {
		if(!astLogString.isEmpty()) {
			Path directory = Paths.get(astFilePath).getParent();
			BufferedWriter bw = null;
			try {
				if(!Files.exists(directory)) {
					Files.createDirectories(directory);
				}
				
				if(!Files.exists(Paths.get(astFilePath))) {
					Files.createFile(Paths.get(astFilePath));
				}
				bw = Files.newBufferedWriter(Paths.get(astFilePath), Charset.forName("UTF-8"),StandardOpenOption.APPEND);
				bw.write(astLogString);
			} catch(IOException e) {
				System.out.println("[Exception] When writing AST file.");
			} finally {
				try {
					bw.close();
					return false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	private boolean loadSwiftDependsConfig() {
		if(!Files.exists(Paths.get(configFile))) {
			initalizeSwiftDependsConfig();
			return false;
		} 
		
		InputStreamReader resources = null;
		try {
			resources = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
			Properties properties = new Properties();
			properties.load(resources);
			resources.close();
			
			this.target = properties.getProperty("TARGET");
			this.sdkRoot = properties.getProperty("SDK_ROOT");
			this.projectSourceRoot = properties.getProperty("PROJECT_SOURCE_ROOT");
			this.xcConfigFilePath = properties.getProperty("XCCONFIG_FILE_PATH");
			this.podRoot = properties.getProperty("PODS_ROOT");
			this.podConfigurationBuildDir = properties.getProperty("PODS_CONFIGURATION_BUILD_DIR");
			this.podxcFrameworkBuildDir = properties.getProperty("PODS_XCFRAMEWORKS_BUILD_DIR");
			this.podTargetSourceRoot = properties.getProperty("PODS_TARGET_SRCROOT");
			this.bridgingHeader = properties.getProperty("BRIDGING_HEADER");
			this.objcHeaderSearchPath = properties.getProperty("OBJC_HEADER_SEARCH_PATH");
			this.otherFrameworkSearchPath = properties.getProperty("OTHER_FRAMEWORK_SEARCH_PATH");
			
			if(!this.validateProperties()) return false;
			this.removeWhiteSpaces();
			this.printConfigInfomation();

			
			Map<String, String> xcConfigPaths = new HashMap<String, String>();
			xcConfigPaths.put("SDK_ROOT", this.sdkRoot);
			xcConfigPaths.put("PROJECT_SOURCE_ROOT", this.projectSourceRoot);
			xcConfigPaths.put("PODS_ROOT", this.podRoot);
			xcConfigPaths.put("PODS_CONFIGURATION_BUILD_DIR", this.podConfigurationBuildDir);
			xcConfigPaths.put("PODS_XCFRAMEWORKS_BUILD_DIR", this.podxcFrameworkBuildDir);
			xcConfigPaths.put("PODS_TARGET_SRCROOT", this.podTargetSourceRoot);
			xcConfigPaths.put("OBJC_HEADER_SEARCH_PATH", this.objcHeaderSearchPath);
			xcConfigPaths.put("OTHER_FRAMEWORK_SEARCH_PATH", this.otherFrameworkSearchPath);
			
			if(this.xcConfigFilePath != null) {
				this.extractor = new XcConfigExtractor(this.xcConfigFilePath);
				extractor.setPodConfigurationBuildDir(xcConfigPaths);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				resources.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private boolean validateProperties() {

		//Validate SDK ROOT
		if(!Files.exists(Paths.get(this.sdkRoot))) {
			System.out.println("[Invalid Configuration] Please check sdk root path");
			return false;
		}
		//PROJECT_SOURCE_ROOT
		if(!Files.exists(Paths.get(this.projectSourceRoot))) {
			System.out.println("[Invalid Configuration] Please check PROJECT_SOURCE_ROOT path");
			return false;
		}
		//XCCONFIG_FILE_PATH
		if(this.xcConfigFilePath!= null && !Files.exists(Paths.get(this.xcConfigFilePath))) {
			System.out.println("[Invalid Configuration] Please check XCCONFIG_FILE_PATH path");
			return false;
		}
		//PODS_CONFIGURATION_BUILD_DIR
		if(this.podConfigurationBuildDir!= null && !Files.exists(Paths.get(this.podConfigurationBuildDir))) {
			System.out.println("[Invalid Configuration] Please check PODS_CONFIGURATION_BUILD_DIR path");
			return false;
		}
		//PODS_XCFRAMEWORKS_BUILD_DIR
		if(this.podxcFrameworkBuildDir!= null && !Files.exists(Paths.get(this.podxcFrameworkBuildDir))) {
			System.out.println("[Invalid Configuration] Please check PODS_XCFRAMEWORKS_BUILD_DIR path");
			return false;
		}
		//PODS_TARGET_SRCROOT
		if(this.podTargetSourceRoot!= null && !Files.exists(Paths.get(this.podTargetSourceRoot))) {
			System.out.println("[Invalid Configuration] Please check PODS_TARGET_SRCROOT path");
			return false;
		}
		//BRIDGING_HEADER
		if(this.bridgingHeader!= null && !Files.exists(Paths.get(this.bridgingHeader))) {
			System.out.println("[Invalid Configuration] Please check BRIDGING_HEADER path");
			return false;
		}
		//OBJC_HEADER_SEARCH_PATH
		if(this.objcHeaderSearchPath!= null && !Files.exists(Paths.get(this.objcHeaderSearchPath))) {
			System.out.println("[Invalid Configuration] Please check OBJC_HEADER_SEARCH_PATH path");
			return false;
		}
		//OTHER_FRAMEWORK_SEARCH_PATH
		if(this.otherFrameworkSearchPath!= null && !Files.exists(Paths.get(this.otherFrameworkSearchPath))) {
			System.out.println("[Invalid Configuration] Please check OTHER_FRAMEWORK_SEARCH_PATH path");
			return false;
		}
		
		return true;
	}

	private void initalizeSwiftDependsConfig() {
		BufferedWriter writer = null;
		
		try {
			writer = Files.newBufferedWriter(Paths.get("./swift-depends.config"), Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW);
			writer.write("TARGET=\n");
			writer.write("SDK_ROOT=\n");
			writer.write("PROJECT_SOURCE_ROOT=\n");
			writer.write("XCCONFIG_FILE_PATH=\n");
			writer.write("PODS_ROOT=\n");
			writer.write("PODS_CONFIGURATION_BUILD_DIR=\n");
			writer.write("PODS_XCFRAMEWORKS_BUILD_DIR=\n");
			writer.write("PODS_TARGET_SRCROOT=\n");
			writer.write("BRIDGING_HEADER=\n");
			writer.write("OBJC_HEADER_SEARCH_PATH=\n");
			writer.write("OTHER_FRAMEWORK_SEARCH_PATH=\n");
			
		} catch (IOException e) {
			System.out.println("[Exception] When creating \"swift-depends.config\" file.");
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean findSwiftFile(Path path) {
		List<Path> files;
		
		try {
			files = Files.find(path,Integer.MAX_VALUE,
					(f, attr) -> !attr.isDirectory() && f.toFile().getName().toString().endsWith(".swift")).distinct().collect(Collectors.toList());
		} catch (IOException e) {
			System.out.println("[Exception] when finding swift file in " + path.toString() + ". Please check input source path.");
			return false;
		}
		
		List<String> fileNameList = files.stream().map(f -> f.getFileName().toString()).distinct().collect(Collectors.toList());
		
		for(Path p : files) {
			String name = p.getFileName().toString();
			if(fileNameList.contains(name)) {
				fileNameList.remove(name);
				this.swiftFileList.add("\"" + p.toString().replace("\\", "")+ "\"");
			}	
		}		
		
		return true;
	}
	
 	private boolean readSwiftFileList(Path inputPath) {
		
		try {
			for(String line : Files.readAllLines(inputPath)) {
				if(!Files.isRegularFile(Paths.get(line))) {
					System.out.println(line + " file is not exist");
					System.out.println("Please check: " + inputPath.toString() + " file");
					return false;
				}
				this.swiftFileList.add(line.replace(" ", "\\ "));
			}
		} catch (IOException e) {
			System.out.println("[Error] Reading Swift file list...");
		}
		return true;
	}
	
 	private boolean isFileDuplicated() {
 		
 		return this.swiftFileList.stream()
 				.map(p -> Paths.get(p).getFileName().toString())
 				.distinct()
 				.count() != this.swiftFileList.size();
	}

	private void printDuplicatedFileList() {
		List<String> fileNameList = this.swiftFileList.stream()
 										.map(p -> Paths.get(p).getFileName().toString())
										.collect(Collectors.toList());
 		
 		Map<String, Integer> fileMap = new HashMap<>();
 		
 		fileNameList.forEach( name -> {
 			fileMap.put(name, fileMap.getOrDefault(name, 0)+1);
 		});
 		
 		fileMap.forEach((key,value) -> {
 			if(value > 1) {
 				System.out.println("\n***** Duplicated file name: "+ key + "*****");
 				
 				this.swiftFileList.stream().forEach( path -> {
 		 			String name = Paths.get(path).getFileName().toString();
 		 			if(name.equals(key)) {
 		 				System.out.println(path);
 		 			}
 		 		});
 			}
 		});
	}
 	
	private void removeWhiteSpaces() {
		if(this.target != null) this.target = target.replace(" ", "\\ ");
		if(this.sdkRoot != null) this.sdkRoot = sdkRoot.replace(" ", "\\ ");
		if(this.podRoot != null) this.podRoot = podRoot.replace(" ", "\\ ");
		if(this.podConfigurationBuildDir != null) this.podConfigurationBuildDir = podConfigurationBuildDir.replace(" ", "\\ ");
		if(this.podxcFrameworkBuildDir != null) this.podxcFrameworkBuildDir = podxcFrameworkBuildDir.replace(" ", "\\ ");
		if(this.podTargetSourceRoot != null) this.podTargetSourceRoot = podTargetSourceRoot.replace(" ", "\\ ");
		if(this.bridgingHeader != null) this.bridgingHeader = bridgingHeader.replace(" ", "\\ ");
	}
	
	private void printConfigInfomation() {
		System.out.println("==================================================================================================================");
		System.out.println("Configuration File : " + this.configFile +"\n");
		System.out.println("TARGET=" + this.target);
		System.out.println("SDK_ROOT=" + this.sdkRoot);
		System.out.println("PROJECT_SOURCE_ROOT=" + this.projectSourceRoot);
		System.out.println("XCCONFIG_FILE_PATH=" + this.xcConfigFilePath);
		System.out.println("PODS_ROOT=" + this.podRoot);
		System.out.println("PODS_CONFIGURATION_BUILD_DIR=" + this.podConfigurationBuildDir);
		System.out.println("PODS_XCFRAMEWORKS_BUILD_DIR=" + this.podxcFrameworkBuildDir);
		System.out.println("PODS_TARGET_SRCROOT=" + this.podTargetSourceRoot);
		System.out.println("BRIDGING_HEADER=" + this.bridgingHeader);
		System.out.println("OBJC_HEADER_SEARCH_PATH=" + this.objcHeaderSearchPath);
		System.out.println("OTHER_FRAMEWORK_SEARCH_PATH=" + this.otherFrameworkSearchPath);
		System.out.println("==================================================================================================================");
	}
}