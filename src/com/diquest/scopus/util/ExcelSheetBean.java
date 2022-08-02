package com.diquest.scopus.util;

import java.util.LinkedList;

public class ExcelSheetBean {
	 String sheetName = null;
	public int sheetIdx = -1;
	LinkedList<String[]> rowdatas = new LinkedList<String[]>();
	
	public ExcelSheetBean(String sheetName){
		this.sheetName = sheetName;
	}

	public void putRow(String[] columns) {
		rowdatas.add(columns);
	}

	public LinkedList<String[]> getSheetData() {
		return this.rowdatas;
	}
	
	public String getSheetName(){
		return this.sheetName;
	}

}
