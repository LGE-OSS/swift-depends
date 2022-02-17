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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentRepository;

public class LattixRepository implements ComponentRepository {
	private Path excelFilePath;

	public LattixRepository(Path excelFilePath) {
		super();
		this.excelFilePath = excelFilePath;
	}

	@Override
	public boolean create(List<Component> components) {

		// create excel workbook
		SXSSFWorkbook workbook = new SXSSFWorkbook(-1);

		// generate atom and atom dependency list
		AtomDependencyListCreator atomDependencyListCreator = new AtomDependencyListCreator();
		components.forEach(component -> {
			component.accecpt(atomDependencyListCreator);
		});

		// create 'Definitions' sheet
		createDefinitionsSheet(workbook, atomDependencyListCreator.getAtomList());

		// create 'Dependences' sheet
		createDependencesSheet(workbook, atomDependencyListCreator.getAtomDependencyList());

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath.toString());
			workbook.write(fileOutputStream);
			workbook.close();
			fileOutputStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<Component> read() {
		throw new NotImplementedException("The read function of LattixRepository is not implemented.");
	}

	private void createDefinitionsSheet(SXSSFWorkbook workbook, Vector<Atom> atomList) {
		SXSSFSheet sheet = workbook.createSheet("Definitions");

		SXSSFRow headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Source Atom");
		headerRow.createCell(1).setCellValue("Atom Kind");
		headerRow.createCell(2).setCellValue("Property Duration");

		AtomicInteger rowCnt = new AtomicInteger(0);
		atomList.forEach(atom -> {
			SXSSFRow atomRow = sheet.createRow(rowCnt.incrementAndGet());
			atomRow.createCell(0).setCellValue(atom.getSourceAtom());
			atomRow.createCell(1).setCellValue(atom.getAtomKind());
		});
	}

	private void createDependencesSheet(SXSSFWorkbook workbook, Vector<AtomDependency> atomDependencyList) {
		SXSSFSheet sheet = workbook.createSheet("Dependencies");

		SXSSFRow headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Source Atom");
		headerRow.createCell(1).setCellValue("Target Atom");
		headerRow.createCell(2).setCellValue("Dependency Strength");
		headerRow.createCell(3).setCellValue("Dependency Kind");
		headerRow.createCell(4).setCellValue("Dependency Property Risk");

		AtomicInteger rowCnt = new AtomicInteger(0);
		atomDependencyList.forEach(atomDependency -> {
			SXSSFRow atomRow = sheet.createRow(rowCnt.incrementAndGet());
			atomRow.createCell(0).setCellValue(atomDependency.getSourceAtom());
			atomRow.createCell(1).setCellValue(atomDependency.getTargetAtom());
			atomRow.createCell(2).setCellValue(1);
		});
	}
}
