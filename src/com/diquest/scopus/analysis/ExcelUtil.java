package com.diquest.scopus.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelUtil {

	FileOutputStream fs = null;
	FileInputStream inputStream = null;
	SXSSFWorkbook workbook;
	SXSSFSheet sheet = null;
	
	Logger logger = LoggerFactory.getLogger(getClass());

	CellStyle style = null;
	CellStyle dataStyle = null;
	
	private CreationHelper helper;
	
	
	public static void main(String... args) throws IOException {
		ExcelUtil u = new ExcelUtil();
		u.readExcel(new File("f:\\Documents\\Private\\2015\\IBS\\1~6월 71명+\\test.xlsx"));
	}

	private void createHeaderStyle(XSSFWorkbook workbook) {
		style = workbook.createCellStyle();
		dataStyle = workbook.createCellStyle();
		dataStyle.setWrapText(false);
		
		style.setWrapText(true);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	}

	public LinkedHashMap<Integer, LinkedList<LinkedList<LinkedList<String>>>> generalReadExcel(File readFile)
			throws IOException {
		LinkedHashMap<Integer, LinkedList<LinkedList<LinkedList<String>>>> totalDataMap = new LinkedHashMap<Integer, LinkedList<LinkedList<LinkedList<String>>>>();

		LinkedList<LinkedList<LinkedList<String>>> rowDataMap = new LinkedList<LinkedList<LinkedList<String>>>();
		XSSFRow row;
		XSSFCell cell;
		XSSFWorkbook workbook = null;
		try {
			inputStream = new FileInputStream(readFile);
			logger.info("read File " + readFile.getName());
			workbook = new XSSFWorkbook(inputStream);
			// sheet수 취득
			int sheetCn = workbook.getNumberOfSheets();
//			logger.debug("sheet수 : " + sheetCn);
			for (int sheetIndex = 0; sheetIndex < sheetCn; sheetIndex++) {
				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				// 취득된 sheet에서 rows수 취득
				int rows = sheet.getPhysicalNumberOfRows();
//				logger.debug(workbook.getSheetName(sheetIndex) + " sheet의 row수 : " + rows);
				// 취득된 row에서 취득대상 cell수 취득
				rowDataMap = new LinkedList<LinkedList<LinkedList<String>>>();
				for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
					row = sheet.getRow(rowIndex); // row 가져오기
					LinkedList<LinkedList<String>> rowData = new LinkedList<LinkedList<String>>();
					if (row != null) {
						int toatalColumnIndex = row.getLastCellNum();
						LinkedList<String> columnData = new LinkedList<String>();
						for (int columnIdx = 0; columnIdx <= toatalColumnIndex; columnIdx++) {
							cell = row.getCell(columnIdx);
							if (cell != null) {
								String value = null;
								switch (cell.getCellType()) {
								case XSSFCell.CELL_TYPE_FORMULA:
									value = cell.getCellFormula();
									break;
								case XSSFCell.CELL_TYPE_NUMERIC:
									value = "" + cell.getNumericCellValue();
									break;
								case XSSFCell.CELL_TYPE_STRING:
									value = "" + cell.getStringCellValue();
									break;
								case XSSFCell.CELL_TYPE_BLANK:
									value = "[null 아닌 공백]";
									break;
								case XSSFCell.CELL_TYPE_ERROR:
									value = "" + cell.getErrorCellValue();
									break;
								default:
								}
								if (value != null) {
									columnData.add(columnIdx, value.trim());
								}else{
								}
							}
						}
						rowData.add(columnData);
					}
					rowDataMap.add(rowData);
				}
				totalDataMap.put(sheetIndex, rowDataMap);
			}
			// logger.info(eidMap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return totalDataMap;
	}

	public Map<Integer, LinkedList<String>> readExcel(File readFile) throws IOException {
		Map<Integer, LinkedList<String>> eidMap = new HashMap<Integer, LinkedList<String>>();
		XSSFRow row;
		XSSFCell cell;
		int eidColumnIndex = 4;
		XSSFWorkbook workbook = null;
		try {
			inputStream = new FileInputStream(readFile);
			logger.info("read File " + readFile.getName());
			workbook = new XSSFWorkbook(inputStream);
			// sheet수 취득
			int sheetCn = workbook.getNumberOfSheets();
			logger.info("sheet수 : " + sheetCn);
			StringBuilder buf = new StringBuilder();
			for (int sheetIndex = 0; sheetIndex < sheetCn; sheetIndex++) {
				String sheetName = workbook.getSheetName(sheetIndex);
				if ("ASJC Code".equalsIgnoreCase(sheetName)) {
					break;
				}
				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				// 취득된 sheet에서 rows수 취득
				int rows = sheet.getPhysicalNumberOfRows();

//				logger.info(workbook.getSheetName(sheetIndex) + " sheet의 row수 : " + rows);

				// 취득된 row에서 취득대상 cell수 취득
				LinkedList<String> list = new LinkedList<String>();
				int idx = 0;
				for (int rowIndex = 2; rowIndex < rows; rowIndex++) {
					row = sheet.getRow(rowIndex); // row 가져오기
					if (row != null) {
						cell = row.getCell(eidColumnIndex);
						if (cell != null) {
							String value = null;
							switch (cell.getCellType()) {
							case XSSFCell.CELL_TYPE_FORMULA:
								value = cell.getCellFormula();
								break;
							case XSSFCell.CELL_TYPE_NUMERIC:
								value = "" + cell.getNumericCellValue();
								break;
							case XSSFCell.CELL_TYPE_STRING:
								value = "" + cell.getStringCellValue();
								break;
							case XSSFCell.CELL_TYPE_BLANK:
								value = "[null 아닌 공백]";
								break;
							case XSSFCell.CELL_TYPE_ERROR:
								value = "" + cell.getErrorCellValue();
								break;
							default:
							}
							if (value != null) {
//								logger.info(value.trim());
								list.add(idx++, value.trim());
//								list.add(idx++, value.trim().replaceAll("#", "").replaceAll("\\s{1,}", " "));
							}
						}
					}
					eidMap.put(sheetIndex, list);
				}
			}
			// logger.info(eidMap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return eidMap;
	}

	public void writeExcel(File writeFile, Map<Integer, LinkedList<String>> mapData) throws IOException {
		XSSFRow row;
		XSSFCell cell;
		int startCreateCellIndex = 7;
		XSSFWorkbook workbook = null;
		try {
			inputStream = new FileInputStream(writeFile);
			workbook = new XSSFWorkbook(inputStream);
			// sheet수 취득
			int sheetCn = workbook.getNumberOfSheets();
			createHeaderStyle(workbook);
			helper = workbook.getCreationHelper();
			for (int sheetIndex = 0; sheetIndex < sheetCn; sheetIndex++) {
				String sheetName = workbook.getSheetName(sheetIndex);
				if ("ASJC Code".equalsIgnoreCase(sheetName)) {
					break;
				}
				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				int rows = sheet.getPhysicalNumberOfRows();
				int idx = 0;
				row = sheet.getRow(1); // row 가져오기
				write(sheet, row, startCreateCellIndex, "논문 ASJC", true, false);
				write(sheet, row, startCreateCellIndex + 1, "eid", true, false);
				write(sheet, row, startCreateCellIndex + 2, "issn", true, false);
				write(sheet, row, startCreateCellIndex + 3, "corr-author", true, false);
				sheet.setColumnWidth(startCreateCellIndex + 4, 3500);
				write(sheet, row, startCreateCellIndex + 4, "TC (피인용 수)", true, false);
				sheet.setColumnWidth(startCreateCellIndex + 5, 4000);
				write(sheet, row, startCreateCellIndex + 5, "저널명(Source)", true, false);
				sheet.setColumnWidth(startCreateCellIndex + 6, 3500);
				write(sheet, row, startCreateCellIndex + 6, "저널 대표\nasjc 1%", true, true);
				sheet.setColumnWidth(startCreateCellIndex + 7, 4000);
				write(sheet, row, startCreateCellIndex + 7, "저널 대표\nasjc 대분야 1%", true, true);
				sheet.setColumnWidth(startCreateCellIndex + 8, 4000);
				write(sheet, row, startCreateCellIndex + 8, "저널 1% 여부", true, false);
				sheet.setColumnWidth(startCreateCellIndex + 9, 4000);
				write(sheet, row, startCreateCellIndex + 9, "HCP 대표\nasjc 1%", true, true);
				sheet.setColumnWidth(startCreateCellIndex + 10, 4000);
				write(sheet, row, startCreateCellIndex + 10, "HCP 기타\nasjc 1%", true, true);
				sheet.setColumnWidth(startCreateCellIndex + 11, 4000);
				write(sheet, row, startCreateCellIndex + 11, "HCP 대표\nhcp 1% 여부", true, true);
				sheet.setColumnWidth(startCreateCellIndex + 12, 4000);
				write(sheet, row, startCreateCellIndex + 12, "HCP 대표+기타\nhcp 1% 여부", true, true);
				LinkedList<String> datas = mapData.get(sheetIndex);
				for (int rowIndex = 2; rowIndex < rows; rowIndex++) {
					row = sheet.getRow(rowIndex); // row 가져오기
					if (row != null && (datas.size() > idx)) {
						String data = datas.get(idx++);
//						logger.debug(data);
						boolean highlight = false;
						if(data.contains("TRUE")){
							highlight = true;
						}
						String[] ds = data.split("\t");
						int l = startCreateCellIndex + ds.length;
						int dsIndex = 0;
						if(startCreateCellIndex==7 && highlight){
							int _idx = startCreateCellIndex;
							while(_idx==0){
								Cell _cell = row.getCell(_idx);
								dataStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
								dataStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
								dataStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
								dataStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN); 
								dataStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
								dataStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
							    _cell.setCellStyle(dataStyle);
							    row.setRowStyle(dataStyle);
								_idx -= 1;
							}
							
						}
						
						for (int dataIndex = startCreateCellIndex; dataIndex < l; dataIndex++) {
							write(sheet, row, dataIndex, ds[dsIndex++], false, false, highlight);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		FileOutputStream outstream = null;
		try {
			outstream = new FileOutputStream(writeFile);
			workbook.write(outstream);
			outstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outstream != null) {
				outstream.flush();
				outstream.close();
			}
		}
		logger.info("write File " + writeFile.getName());
	}
	
	private void write(XSSFSheet sheet, Row row, int cellIdx, String contents, boolean header, boolean isWarp) {
		write(sheet, row, cellIdx, contents, header, isWarp, false);
		
	}

	private void write(XSSFSheet sheet, Row row, int cellIdx, String contents, boolean header, boolean isWarp, boolean highlight) {
		Cell cell = row.createCell(cellIdx);
		style.setWrapText(true);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
//		https://huskdoll.tistory.com/795
		if(highlight && header == false){
			dataStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			dataStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			dataStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
			dataStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN); 
			dataStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
			dataStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		    cell.setCellStyle(dataStyle);
		}else{
//			style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
//		    cell.setCellStyle(style);
		}
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		if (header) {
			ClientAnchor anchor = null;
			Comment comment = null;
			RichTextString str = null;
			Drawing drawing = sheet.createDrawingPatriarch();
			switch (cellIdx) {
			case 7:
				str = helper.createRichTextString("문헌의 ASJC 전체 분류 코드\n2017.05.08.");
				break;
			case 8:
				str = helper.createRichTextString("논문의 EID\n2017.05.08.");
				break;
			case 9:
				str = helper.createRichTextString("논문이 게재된 저널의 ISSN 번호\n2017.05.08.");
				break;
			case 10:
				str = helper.createRichTextString("교신저자 명칭\n2017.05.08.");
				break;
			case 11:
				str = helper.createRichTextString("논문의 총 피인용 수\n2017.05.08.");
				break;
			case 12:
				str = helper.createRichTextString("논문이 게재된 저널명\n2017.05.08.");
				break;
			case 13:
				str = helper.createRichTextString("논문이 게제된 TOP 1% 저널의 대표 ASJC 코드 정보\n2017.05.08.");
				break;
			case 14:
				str = helper.createRichTextString("논문이 게재된 TOP 1% 저널의 대표 ASJC 분류코드의 대분야가 매칭된 논문 ASJC 코드\n2017.05.08.");
				break;
			case 15:
				str = helper.createRichTextString("논문이 게재된 저널이 Impact Factor가 TOP 1%에 속하는지 여부\n2017.05.08.");
				break;
			case 16:
				str = helper.createRichTextString("HCP 1%에 포함되는 논문의 대표 ASJC 분류코드 정보\n2017.05.08.");
				break;
			case 17:
				str = helper.createRichTextString("HCP 1%에 포함되는 논문의 대표 ASJC 분류코드 정보를 제외한 나머지 ASJC 분류코드 정보\n2017.05.08.");
				break;
			case 18:
				str = helper.createRichTextString("논문의 대표 ASJC 분류코드가 HCP 1%에 속하는지 여부\n2017.05.08.");
				break;
			case 19:
				str = helper.createRichTextString("논문의 대표 + 기타 ASJC 분류코드가 HCP 1%에 속하는지 여부\n2017.05.08.");
				break;
			}
			if (str != null) {
				anchor = helper.createClientAnchor();
				anchor.setCol1(cellIdx);
				anchor.setCol2(cellIdx + 3);
				anchor.setRow1(row.getRowNum());
				anchor.setRow2(row.getRowNum() + 2);
				comment = drawing.createCellComment(anchor);
				comment.setString(str);
				cell.setCellComment(comment);
			}
			if (isWarp) {
				cell.setCellStyle(style);
			}
		}
//		logger.info("contents : {} / {}", cellIdx, contents);

		cell.setCellValue(contents);
	}

	public void flush() {
		try {
			workbook.write(fs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public void close() {
		if (fs != null)
			try {
				flush();
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
