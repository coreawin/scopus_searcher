package com.diquest.scopus.util;

import java.io.File;
import java.util.LinkedHashMap;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadExcel {
	Logger logger = LoggerFactory.getLogger(getClass());

	public LinkedHashMap<String, ExcelSheetBean> readExcelFile(File f, int startSheet) throws Exception {
		System.out.println("AAAAAA");
		LinkedHashMap<String, ExcelSheetBean> excelData = new LinkedHashMap<String, ExcelSheetBean>();
		

		String name = f.getName();
		Workbook book = null;
		if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
			book = WorkbookFactory.create(f);
		} else {
			return excelData;
		}
		for (int i = startSheet; i < book.getNumberOfSheets(); i++) {
			String sheetName = book.getSheetName(i);
			logger.info("sheetName : {} ", sheetName);
			System.out.println("AAAAA");
			Sheet sheet = book.getSheetAt(i);
			ExcelSheetBean eBean = new ExcelSheetBean(sheetName);
			int rowCnt =sheet.getPhysicalNumberOfRows();
			for (int rn = 1; rn < rowCnt; rn++) {
				Row dR = sheet.getRow(rn);
				if (dR == null) {
					continue;
				}
				int columnCnt = dR.getLastCellNum();
				columnCnt = 23;
				String[] columnData = new String[columnCnt+1];
//				logger.info("rows : {} / {} , {}", rn, columnCnt);
//				System.out.println("rows : " + rn +" / " + rowCnt);
				int fColor = 64;
				for (int cn = 0; cn < columnCnt; cn++) {
					try{
					Cell dC = dR.getCell(cn);
					String cellValue = " ";
					if (dC != null) {
						if(cn==0){
							fColor = dC.getCellStyle().getFillForegroundColor();
						}
						dC.setCellType(Cell.CELL_TYPE_STRING);
						cellValue = UtilString.nullCkeck(dC.getStringCellValue());
					}
					columnData[cn] = cellValue;
//					logger.info("columnDatas {} : {} ", cn, cellValue);
					}catch(Exception e){
						columnData[cn] = "";
					}
				}
				columnData[columnCnt] = String.valueOf(fColor); 
//				System.out.println(columnCnt +"\t" + columnData[columnCnt]);
				eBean.putRow(columnData);
//				break;
			}
//			System.out.println(eBean.getSheetData().size());
//			if(true) System.exit(01);
			excelData.put(sheetName, eBean);
			break;
		}
		return excelData;
	}
}
