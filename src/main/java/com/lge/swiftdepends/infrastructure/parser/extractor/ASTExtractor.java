/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser.extractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASTExtractor {
	private final String fileSyntax = ".(file)";
	private static final Logger logger = LogManager.getLogger(ASTExtractor.class);

    public String extractQuote(String quote, String line){
		Pattern pattern = Pattern.compile(quote + "(.*?)" + quote);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()){
            return matcher.group(1).toString();
        }
		return null;
	}
	
	public String extractSourceFileName(String line){
		String[] sourceFilePath;
		try {
			sourceFilePath = line.split(" ");
		} catch (Exception e) {
			logger.error(line);
			return null;
		}

		File file = new File(sourceFilePath[sourceFilePath.length -1].replace("\"", ""));
		try{
			return file.getName().substring(0, file.getName().lastIndexOf("."));
		}
		catch (Exception e){
			logger.error(line);
			return null;
		}
	}

	public String extractLocation(String line){
		if(line.contains(".swift")){
			Pattern pattern = Pattern.compile(".swift:(.*?)\\s");
			Matcher matcher = pattern.matcher(line);

			if(matcher.find()){
				return matcher.group(1).toString();
			}
		}
		return "";
	}

	public String extractFileSyntax(String line){
		final String fileSyntax = "(file)";

		try {
			if (line.contains(fileSyntax)){
				for (String str : line.split(" ")) {
					if (str.contains(fileSyntax)){
						return str.split("=")[1];
					}	
				}
			}
		} catch (Exception e) {
			logger.error(line);
			return "";
		}
		
		return "";
	}

	public String extractEqualKeyword(String keyword, String line){
		Pattern pattern = Pattern.compile(keyword + "=(.*?)\\s");
		Matcher matcher = pattern.matcher(line);

		String result = "";
		
		if (matcher.find()){
			result =  matcher.group(1).toString();
		}

		if(result == null || result.equals("") ){
			Pattern innerPattern = Pattern.compile(keyword + "=(.*?)\\s");
			Matcher innerMatcher = innerPattern.matcher(line + " ");

			if (innerMatcher.find()){
				return innerMatcher.group(1).toString();
			}
		}

		return result;
	}

	public String extractEqualKeywordWithQuote(String keyword, String line){
		Pattern pattern = Pattern.compile(keyword + "='(.*?)'\\s");
		Matcher matcher = pattern.matcher(line);

		String result = null;
		
		if (matcher.find()){
			result =  matcher.group(1).toString();
		}

		if(result == null){
			Pattern innerPattern = Pattern.compile(keyword + "='(.*?)'\\s");
			Matcher innerMatcher = innerPattern.matcher(line + " ");

			if (innerMatcher.find()){
				return innerMatcher.group(1).toString();
			}else{
				return "";
			}
		}

		return result;
	}
	

	public String[] extractAfterLastColon(String line){
		try {
			String lastConol = line.substring(line.lastIndexOf(":")+1, line.length()).replace(" ", "");
			
			return lastConol.split(",");	
		} catch (Exception e) {
			logger.error(line);
			return null;
		}
	}

	public String extractDecl(String line){
		try {
			for (String data : line.split(" ")) {
				if(data.contains("decl=")){
					return data.split("=")[1].replace(fileSyntax, "").split("@")[0];
				}
			}
		} catch (Exception e) {
			logger.error(line);
			return null;
		}
		return null;
	}

	public String extractBindFromComponent(String line){
		String result = "";
		
		try {
			String[] bindSplitStr = line.split("=");
			result = bindSplitStr[bindSplitStr.length-1].replace(fileSyntax, "").replace(")", "").split("@")[0];
			result = result.split(" ")[0];
			
			if (result.contains("(")){
				result = result.concat(")");
			}
		} catch (Exception e) {
			logger.error(line);
			return null;
		}

		return result;
	}

	public List<String> extractGenericAndDict(String typeData){
        var typeList = new ArrayList<String>();

		try {
			String[] splitData = typeData.split("[:<,]+");
        
			for (String string : splitData) {
				typeList.add(string.replaceAll("[^a-zA-Z0-9._]", ""));
			}
		} catch (Exception e) {
			logger.error(typeData);
			return null;
		}

        return typeList;
    }
	
	public String extracFunctionProperty(String line) {
		String result = "";
		String strInterfaceType = "interface type=";
		
		if( line.contains(strInterfaceType) == false) {
			return "";
		}
		
		try {
			result = line.substring(line.lastIndexOf("\" ") + 2, line.indexOf(strInterfaceType) -1);
		}
		catch(StringIndexOutOfBoundsException e) {
			try {
				result = line.substring(line.lastIndexOf("\" ") + 1, line.indexOf(strInterfaceType) -1);	
			} catch (Exception ee) {
				logger.error(line);
				return null;
			}
		}
		
		return result;
	}
}
