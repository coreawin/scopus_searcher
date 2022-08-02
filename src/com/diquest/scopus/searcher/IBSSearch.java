package com.diquest.scopus.searcher;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.ExecutorIBSSearch.InputParameter;
import com.diquest.scopus.searchrule.QueryConverterWoS_OLD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;
import com.diquest.scopus.util.UtilString;
import com.diquest.scopus.writer.IBSExcelWriter;

public class IBSSearch {
	Logger logger = LoggerFactory.getLogger(getClass());
	private InputParameter param;
	SearchService ss = new SearchService();

	public IBSSearch(InputParameter param) {
		this.param = param;
	}

	public void search() throws Exception {
		String source = param.source;
		File f = new File(source);
		if (f.isDirectory() && f.exists()) {
			findDir(f, param.target + File.separator + f.getName());
		} else if (f.isFile() && f.exists()) {
			findFile(f, param.target);
		} else {
			throw new Exception("해당 경로가 존재하지 않습니다.");
		}
	}

	private void findFile(File f, String target) throws Exception {
		String name = f.getName();
		Workbook book = null;
		if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
			book = WorkbookFactory.create(f);
		} else {
			return;
		}

		File targetF = new File(target, name);
		IBSExcelWriter writer = new IBSExcelWriter(targetF);
		LinkedList<LinkedHashMap<String, String>> data = null;
		List<String> headerList = null;
		SelectSet[] ss = SearchQueryHelper.getSelectSet();
		for (int i = 0; i < book.getNumberOfSheets(); i++) {
			String sheetName = book.getSheetName(i);
			Sheet sheet = book.getSheetAt(i);
			if (data == null) {
				data = new LinkedList<LinkedHashMap<String, String>>();
			} else {
				data.clear();
			}
			if (headerList == null) {
				headerList = new ArrayList<String>();
			} else {
				headerList.clear();
			}
			for (int rn = 0; rn < sheet.getPhysicalNumberOfRows(); rn++) {
				Row dR = sheet.getRow(rn);
				if (dR == null) {
					continue;
				}
				String searchRule = "";
				if (rn == 0) {
//					System.out.println(dR.getPhysicalNumberOfCells());
					for (int cn = 0; cn < dR.getPhysicalNumberOfCells(); cn++) {
						Cell dC = dR.getCell(cn);
						if (dC == null) {
							headerList.add("");
						}
//						System.out.println(dC);
						if(dC!=null){
							dC.setCellType(Cell.CELL_TYPE_STRING);
							headerList.add(UtilString.nullCkeck(dC.getStringCellValue()));
						}
					}
				} else {
					for (int cn = 0; cn < dR.getPhysicalNumberOfCells(); cn++) {
						Cell dC = dR.getCell(cn);
						if (dC != null) {
							dC.setCellType(Cell.CELL_TYPE_STRING);
							String cellValue = UtilString.nullCkeck(dC.getStringCellValue());
							if (cellValue.length() < 1) {
								continue;
							}
							try {
								String header = headerList.get(cn);
								if (header.length() < 1) {
									continue;
								} else {
									if (searchRule.length() > 0) {
										searchRule = searchRule + " OR ";
									}
									if ("TITLE".equals(header) || "제목".equals(header)) {
										searchRule = searchRule + "TI=(" + cellValue + ")";
									} else if ("DOI".equals(header)) {
										searchRule = searchRule + "DOI=(" + cellValue + ")";
									} else if ("EID".equals(header)) {
										searchRule = searchRule + "EID=(" + cellValue + ")";
									}
								}
							} catch (Exception e) {

							}
						}
					}

					if (searchRule.endsWith(" OR ")) {
						searchRule = searchRule.substring(0, searchRule.length() - 3).trim();
					}
					logger.info("SHEET NAME : {}, ROW NUMBER : " + (rn + 1) + " ,SEARCH RULE : {}", sheetName, searchRule);
					if (searchRule.trim().length() < 1) {
						continue;
					}

					QueryConverterWoS_OLD qc = new QueryConverterWoS_OLD(searchRule);
					System.out.println("SHEET NAME : {}, ROW NUMBER : " + (rn + 1) + " ,SEARCH RULE : {}" + sheetName + searchRule);
					WhereSet[] ws = qc.getWhereSet();
					QuerySet qs = new QuerySet(1);
					Query query = new Query();
					query.setSearchOption(Protocol.SearchOption.CACHE | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.BANNED
							| Protocol.SearchOption.PHRASEEXACT);
					query.setThesaurusOption(Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM);
					query.setSelect(ss);
					query.setWhere(ws);
					query.setFrom("SCOPUS_2016");
					query.setSearch(true);
					query.setDebug(true);
					query.setFaultless(true);
					query.setPrintQuery(true);
					query.setLoggable(true);
					query.setResult(0, 3);
					qs.addQuery(query);
					ResultSet rs = this.ss.requestSearch("203.250.207.72", 5555, qs);

					Result[] rArr = rs.getResultList();
					for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
						Result rInfo = rArr[resIndex];
						int totalSize = rInfo.getTotalSize();
						int realSize = rInfo.getRealSize();
						if (totalSize > realSize) {
							logger.info("SHEET NAME : {}, ROW NUMBER : " + (rn + 1) + " ,SEARCH RULE : {}", sheetName, searchRule);
							continue;
						}
						if (totalSize == 1) {
							for (int rr = 0; rr < realSize; rr++) {
								LinkedHashMap<String, String> dd = new LinkedHashMap<String, String>();
								String author = new String(rInfo.getResult(rr, 0));
								String title = new String(rInfo.getResult(rr, 1));
								String year = new String(rInfo.getResult(rr, 2));
								String docType = new String(rInfo.getResult(rr, 3));
								String eid = new String(rInfo.getResult(rr, 4));
								String firstAu = author.split(";", -1)[0];
								String corrAuthor = new String(rInfo.getResult(rr, 5));

								dd.put("AU", author);
								dd.put("title", title);
								dd.put("year", year);
								dd.put("docType", docType);
								dd.put("eid", eid);
								dd.put("11", "");
								dd.put("22", "");
								dd.put("firstAu", firstAu);
								dd.put("corrAuthor", corrAuthor);
								data.add(dd);
							}
						} else {
							logger.info("SHEET NAME : {}, ROW NUMBER : " + (rn + 1) + " ,SEARCH RULE : {}", sheetName, searchRule);
						}
					}
				}
			}
			writer.writeWook(data, sheetName);
		}
		writer.write();
	}

	private void findDir(File f, String target) throws Exception {
		File targetF = new File(target);
		if (!targetF.exists()) {
			targetF.mkdirs();
		}
		File[] subFile = f.listFiles();
		if (subFile == null) {
			return;
		}
		for (File subF : subFile) {
			if (subF.isDirectory() && subF.exists()) {
				findDir(subF, target + File.separator + subF.getName());
			} else if (subF.isFile() && subF.exists()) {
				findFile(subF, target);
			}
		}
	}

}
