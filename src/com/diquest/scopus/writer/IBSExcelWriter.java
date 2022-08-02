package com.diquest.scopus.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class IBSExcelWriter {

	private String path;
	private File f;
	SXSSFWorkbook workbook = null;

	public IBSExcelWriter(File f) {
		this.f = f;
		init();
	}

	public void init() {
		workbook = new SXSSFWorkbook();
	}

	public void writeWook(LinkedList<LinkedHashMap<String, String>> result, String sheetName) {
		SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);
		Row r = sheet.createRow(0);
		Cell c = r.createCell(5);
		c.setCellValue("Author Role");
		c = r.createCell(7);
		c.setCellValue("Subject");
		r = sheet.createRow(1);
		int colCellCnt = 0;
		c = r.createCell(colCellCnt++);
		c.setCellValue("Author");
		c = r.createCell(colCellCnt++);
		c.setCellValue("Title");
		c = r.createCell(colCellCnt++);
		c.setCellValue("Document Type");
		c = r.createCell(colCellCnt++);
		c.setCellValue("Year");
		c = r.createCell(colCellCnt++);
		c.setCellValue("EID");
		c = r.createCell(colCellCnt++);
		c.setCellValue("Co-Author");
		c = r.createCell(colCellCnt++);
		c.setCellValue("First-Correspondence author");
		c = r.createCell(colCellCnt++);
		c.setCellValue("논문 ASJC");
		c = r.createCell(colCellCnt++);
		c.setCellValue("eid");
		c = r.createCell(colCellCnt++);
		c.setCellValue("issn");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Author");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Title (scopus.kisti)");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Title (extract)");1ㅈ	ㅂ
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Eq (titles equals)");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Year");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Document Type");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("EID");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("Co-Author");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("First-Correspondence author");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("doi");
//		c = r.createCell(colCellCnt++);
//		c.setCellValue("논문 ASJC");

		int row = 2;
		for (int i = 0; i < result.size(); i++) {
			LinkedHashMap<String, String> data = result.get(i);
			Row dR = sheet.createRow(row);
			int cell = 0;
			Set<String> ks = data.keySet();
			for (String k : ks) {
				Cell dC = dR.createCell(cell);
				dC.setCellType(Cell.CELL_TYPE_STRING);
				dC.setCellValue(data.get(k));
				cell++;
			}
			row++;
		}
	}

	public void write() {
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			workbook.write(os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.flush();
				} catch (IOException e) {
				}
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}

	}
}
