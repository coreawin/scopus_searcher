package com.diquest.scopus.searcher._2020.RnD;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kr.co.tqk.web.db.DescriptionCodeManager;
import kr.co.tqk.web.db.dao.export.ExportField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.patent.searcher._2020.RnD.ExcelUtilCommon;
import com.diquest.scopus.util.DownloadSearchUtil;
import com.diquest.scopus.util.DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE;

/**
 * 2020.RnD_PIE_특허_논문_정량적_분석_지표에 대한 Excel 검색식 추출 및 결과 만들기.<br>
 * 특허 데이터를 받는다.
 * 
 * @author coreawin
 * @date 2020. 1. 22.
 */
public class ScopusSearcher_2020 {
	public static BlockingQueue<String> bQueue = new ArrayBlockingQueue<String>(1);
	static Logger logger = LoggerFactory.getLogger(ScopusSearcher_2020.class.getName());
	final String ip = "203.250.207.72";
	final int port = 5555;

	// final String downloadPath = "t:/tmp/scopusdownload/";
	static String downloadPath = "d:\\data\\이창환\\20190801\\";

	static String modelpath = "t:/release/KISTI/SCOPUS_2014_WEB/models/ExportBasicFormat.xlsx";
	final DOWNLOAD_FILE_FORMAT_TYPE downloadFormat = DOWNLOAD_FILE_FORMAT_TYPE.TAB_DELIMITED;
	final String sort = "";
	final boolean isSort = false;
	final boolean isAdmin = true;
	private Set<String> selectedField = new HashSet<String>();
	final int sidx = 0;
	final int eidx = Integer.MAX_VALUE;

	private void setFieldInit() {

		selectedField.add(ExportField.TITLE.name());
		// selectedField.add(ExportField.ABSTRACT.name());
		selectedField.add(ExportField.YEAR.name());
		selectedField.add(ExportField.DOI.name());
		selectedField.add(ExportField.KEYWORD.name());
		selectedField.add(ExportField.INDEX_KEYWORD.name());

		selectedField.add(ExportField.ASJC.name());
		selectedField.add(ExportField.NUMBER_CITATION.name());
		// selectedField.add(ExportField.CITATION.name());
		// selectedField.add(ExportField.NUMBER_REFERENCE.name());
		// selectedField.add(ExportField.REFERENCE.name());
		// selectedField.add(ExportField.CITATION_TYPE.name());

		// selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
		// selectedField.add(ExportField.AUTHOR_ID.name());
		// selectedField.add(ExportField.AUTHOR_NAME.name());
		// selectedField.add(ExportField.AUTHOR_EMAIL.name());
		// selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
		// selectedField.add(ExportField.AFFILIATION_ID.name());
		// selectedField.add(ExportField.AFFILIATION_NAME.name());
		selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
		// selectedField.add(ExportField.DELEGATE_AFFILIATION.name());

		// selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
		// selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
		selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
		selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());

		// selectedField.add(ExportField.SOURCE_ID.name());
		selectedField.add(ExportField.SOURCE_SOURCETITLE.name());
		// selectedField.add(ExportField.SOURCE_VOLUMN.name());
		// selectedField.add(ExportField.SOURCE_ISSUE.name());
		// selectedField.add(ExportField.SOURCE_PAGE.name());
		// selectedField.add(ExportField.SOURCE_TYPE.name());
		// selectedField.add(ExportField.SOURCE_PUBLICSHERNAME.name());
		// selectedField.add(ExportField.SOURCE_COUNTRY.name());
		// selectedField.add(ExportField.SOURCE_PISSN.name());
		// selectedField.add(ExportField.SOURCE_EISSN.name()); //
		// selectedField.add(ExportField.CORR_AUTHORNAME.name());
		// selectedField.add(ExportField.CORR_COUNTRYCODE.name());
		// selectedField.add(ExportField.CORR_EMAIL.name());
		// selectedField.add(ExportField.CORR_AFFILIATION.name());

		// selectedField.add(ExportField.GRANT_AGENCY.name());
		// selectedField.add(ExportField.GRANT_AGENCY_ACRONYM.name());
		// selectedField.add(ExportField.GRANT_AGENCY_ID.name());
		// selectedField.add(ExportField.GRANT_ID.name());
		// selectedField.add(ExportField.OPEN_ACCESS_STATUS.name());
	}

	public ScopusSearcher_2020(String searchRule) throws Exception {
		setFieldInit();
		DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
				downloadFormat, selectedField, sort, isSort, isAdmin);
		su.execute();
	}
	
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	
	public static void main(String... args) throws Exception {
		String _분과명 = "신재생에너지";
		// 분과명 = "소부장";
		// 분과명 = "혁신성장";
		_분과명 = "테스트";

		String[] 분과명정보 = new String[] { "혁신성장", "신재생에너지"

		};
		
		분과명정보 = new String[] { "0213"

		};

		for (String 분과명 : 분과명정보) {
			String searchRuleFilePath = "D:/eclipse_workspace/workspace_luna/public/NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER/2020/0212/"
					+ 분과명 + File.separator;

			File f = new File(searchRuleFilePath);
			File[] dirs = f.listFiles();
			for (File dir : dirs) {
				logger.info("read path {}", dir.getAbsolutePath());
				// 검색식을 엑셀파일로부터 읽는다.
				Map<String, Map<Integer, LinkedList<Map<Integer, String>>>> readData = new ExcelUtilCommon().readExcel(dir
						.getAbsolutePath());

				Set<String> excelFileNameSet = readData.keySet();
				for (String excelFileName : excelFileNameSet) {
					logger.info("excel data {}", excelFileName);
					Map<Integer, LinkedList<Map<Integer, String>>> sheetData = readData.get(excelFileName);
					// 여기서는 0번 시트만 사용한다.

					LinkedList<Map<Integer, String>> rowData = sheetData.get(0);

					for (Map<Integer, String> row : rowData) {
						String techNo = row.get(0);
						String techNo2 = techNo;
						String techName = row.get(1).replaceAll("[\\r|\\n|\\<]", "").replaceAll("\\s{1,}", " ")
								.replaceAll("/", "-").replaceAll(" ", "_").trim();
						String searchRule = row.get(2).replaceAll("[\\r|\\n]", "").replaceAll("\\s{1,}", " ").trim();
//						if ("소부장".equals(분과명) && row.size() == 4) {
						logger.info("row.size() : {}" ,row.size());
						if (row.size() == 4) {
							techNo = row.get(0);
							techNo2 = row.get(1);
							techName = row.get(2).replaceAll("[\\r|\\n\\|\\<]", "").replaceAll("\\s{1,}", " ")
									.replaceAll("/", "-").replaceAll(" ", "_").replaceAll("/", "-").trim();
							searchRule = row.get(3).replaceAll("[\\r|\\n]", "").replaceAll("\\s{1,}", " ").trim();
							downloadPath = String.format("d:\\data\\2020\\yeo\\" + dateFormat.format(new Date())
									+ "\\%s\\%s\\scopus\\%s.txt", 분과명, dir.getName().replaceAll("\\s{1,}", ""), techName
									.replaceAll("\\s{1,}", "").replaceAll("[-_\\.\\<]", "").replaceAll("\\.txt", "").trim()
									+ "_" + techNo2);
						} else {
							downloadPath = String.format("d:\\data\\2020\\yeo\\" + dateFormat.format(new Date())
									+ "\\%s\\%s\\scopus\\%s.txt", 분과명, dir.getName().replaceAll("\\s{1,}", ""), techName
									.replaceAll("\\s{1,}", "").replaceAll("[-_\\.\\<]", "").replaceAll("\\.txt", "").trim()
									+ techNo);
						}
						if (techName == null)
							continue;
						if ("".equalsIgnoreCase(techName.trim()))
							continue;

						/*
						 * 경제적 특성을 추출하려면 특허의 파일명과 동일해야 한다. @since
						 * PatentSearcher_2020
						 */
						File downloadFile = new File(downloadPath);
						downloadFile.getParentFile().mkdirs();
						if(downloadFile.isFile()){
							if(downloadFile.length() < 15000 ){
								
							}else{
								logger.info("이미 만들 파일은 건너 뛴다. {}", downloadPath);
								continue;
							}
						}

						searchRule = searchRule.replaceAll("“", "\"").replaceAll("”", "\"").trim();
						StringBuffer buf = new StringBuffer();
//						searchRule = "1234567890azAZ_+=-\"()?&*!#";
						for (char c : searchRule.toCharArray()) {
							int cc = (int)c;
							if(cc>=30 || 130<=cc){
//								logger.info("{}", cc);
								buf.append(c);
							}
						}
						searchRule = buf.toString();
						logger.debug("searchRule {}", searchRule.trim());
						logger.debug("download path {}", downloadPath);
						logger.debug("techName {} ", techName);
						new ScopusSearcher_2020(searchRule.trim());
					}
				}
			}
		}

	}

}
