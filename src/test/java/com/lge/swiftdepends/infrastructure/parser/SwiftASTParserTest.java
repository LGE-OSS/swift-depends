/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SwiftASTParserTest {
    SwiftASTParser swiftParser;

    @Before
    public void setUp() {
        final String test_dir = "test_dir/";
        
        if (System.getProperty("logDirectory") == null){
            System.setProperty("logDirectory", test_dir);
        }

        swiftParser = new SwiftASTParser();

        File dir = new File(test_dir);

        if( !dir.isDirectory()){
            if(!dir.mkdirs()){
                System.out.println("fail to create output folder");
            }
        }
    }

    @Test
    public void testExtractJson(){
        final String outputJson = "src/test/resources/json/output.json";
        final String outputText = "src/test/resources/json/output.txt";

        List<String> extractData = swiftParser.extractJson(Paths.get(outputJson));
        List<String> expectedDataList = readFile(outputText);

        List<String> actualData = new ArrayList<>();

        for (String string : extractData) {
            string.lines().forEach( data -> {
                actualData.add(data);
            });
        }
        assertArrayEquals(actualData.toArray(new String[actualData.size()]), expectedDataList.toArray(new String[expectedDataList.size()]));
    }

    public List<String> readFile(String filePath){
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            return lines;
        } 
        catch (IOException e) {
            return null;
        }
    }
}
