/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.presentation;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.lge.swiftdepends.application.ISwiftDependsApplicationService;

public class SwiftDependsCLI {
	private Options options;
	private CommandLineParser parser;
	private CommandLine cmd;
	private String curTime;
	private ISwiftDependsApplicationService service;
		
	public SwiftDependsCLI(ISwiftDependsApplicationService service) {
		this.service = service;
		options = new Options();
		parser = new DefaultParser();
		options.addOption("n", "name", true, "Enter project name")
			   .addOption("i", "input", true, "Enter a target sources(*.swift) path to analyze")
			   .addOption("o", "output", true, "Enter the path to save the results \n If you don't enter the path, analysis result will save at ./result/$TIMESTAMP")
		   	   .addOption("h", "help", false, "Give this help list");	
		parser = new DefaultParser();
		curTime = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
	}
	
	public void start(String[] args) throws ParseException{
		cmd = parser.parse(options, args);
		
		if(cmd.hasOption("n") && cmd.hasOption("i") && cmd.hasOption("o") && cmd.getOptions().length == 3) {
			String projectName = cmd.getOptionValue("n");
			String inputPath = cmd.getOptionValue("i");
			String outputPath = cmd.getOptionValue("o")+"/"+curTime;
			this.service.analyzeDependency(projectName, Paths.get(inputPath), Paths.get(outputPath));
		}
		if(cmd.hasOption("n") && cmd.hasOption("i") && !cmd.hasOption("o") && cmd.getOptions().length == 2) {
			String projectName = cmd.getOptionValue("n");
			String inputPath = cmd.getOptionValue("i");
			String outputPath = "./result/"+curTime;
			this.service.analyzeDependency(projectName, Paths.get(inputPath), Paths.get(outputPath));
		}
		
		else if(cmd.hasOption("h") || cmd.getOptions().length < 2) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("swift-depends", options);
		}
		
	}	
}
