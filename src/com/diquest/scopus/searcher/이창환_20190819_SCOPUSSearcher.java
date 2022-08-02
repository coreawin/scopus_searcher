package com.diquest.scopus.searcher;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kr.co.tqk.web.db.dao.export.ExportField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.kisti.DownloadScopusData;
import test.kisti.DownloadSearchUtil;
import test.kisti.DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE;

/**
 * 안녕하세요 <br>
 * <br>
 * 2018년도에 2018년 8월 이전까지의 데이터 분에 대해서 압축 파일로 송부해 주신 것으로 알고 있습니다. <br>
 * <br>
 * 올해에도 작년과 동일하게 2018년 이후부터의 자료를 요청드려야 하지만 부득이하게 메타데이터 구분이 안되면 전체를 송부해 주시면
 * 좋겠습니다. <br>
 * <br>
 * 범위는 국내 연구자 연구 성과로 작년에 송부해 주셨던 부분과 동일합니다. <br>
 * <br>
 * 전화상으로는 100만원 정도 소요가 되는 것으로 말씀해 주셨는데요 <br>
 * <br>
 * 첨부드린 자료는 작년에 어느 컬럼을 송부해 주신 포멧과 데이터 샘플입니다. <br>
 * <br>
 * 올해도 동일한 작업이 필요한데요 첨부드린 포멧 파일의 컬럼을 보면 SCOPUS 스키마 대부분의 테이블에 있는 컬럼 목록임을 확인했습니다. <br>
 * <br>
 * 따라서 데이터 산출해서 주실 때 어떻게 쿼리를 날려서 데이터를 압축해서 보내주셨는지 문의드립니다. <br>
 * <br>
 * <br>
 * 문의 1. 2018년 8월 이후 국내 연구자 SCOPUS 데이터를 압축파일로 송부 요청드립니다. <br>
 * <br>
 * 문의 2. 2018년 8월 이전 데이터를 산출하여 다운로드 하실 때 어떤 쿼리를 수행하여 결과를 뽑으셨는지 추가로 문의드립니다. <br>
 * <br>
 * <br>
 * <br>
 * 검토 부탁드립니다. <br>
 * <br>
 * 수고하세요 <br>
 * <br>
 * 최원준 드림 <br>
 * 
 * 
 * @author coreawin
 * @date 2019. 8. 19.
 */
public class 이창환_20190819_SCOPUSSearcher {
	public static BlockingQueue<String> bQueue = new ArrayBlockingQueue<String>(1);
	static Logger logger = LoggerFactory.getLogger("SCOPUSSearcher.class");
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
		 selectedField.add(ExportField.ABSTRACT.name());
		 selectedField.add(ExportField.YEAR.name());
		 selectedField.add(ExportField.DOI.name());
		 selectedField.add(ExportField.KEYWORD.name());
		 selectedField.add(ExportField.INDEX_KEYWORD.name());
		
		 selectedField.add(ExportField.ASJC.name());
		 selectedField.add(ExportField.NUMBER_CITATION.name());
		 selectedField.add(ExportField.CITATION.name());
		 selectedField.add(ExportField.NUMBER_REFERENCE.name());
		 selectedField.add(ExportField.REFERENCE.name());
		 selectedField.add(ExportField.CITATION_TYPE.name());
		 
		 selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
		 selectedField.add(ExportField.AUTHOR_ID.name());
		 selectedField.add(ExportField.AUTHOR_NAME.name());
		 selectedField.add(ExportField.AUTHOR_EMAIL.name());
		 selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
		 selectedField.add(ExportField.AFFILIATION_ID.name());
		 selectedField.add(ExportField.AFFILIATION_NAME.name());
		 selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
		 selectedField.add(ExportField.DELEGATE_AFFILIATION.name());
		 
		 selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
		 selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
		 selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
		 selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());
		 
		 selectedField.add(ExportField.SOURCE_ID.name());
		 selectedField.add(ExportField.SOURCE_SOURCETITLE.name());
		 selectedField.add(ExportField.SOURCE_VOLUMN.name());
		 selectedField.add(ExportField.SOURCE_ISSUE.name());
		 selectedField.add(ExportField.SOURCE_PAGE.name());
		 selectedField.add(ExportField.SOURCE_TYPE.name());
		 selectedField.add(ExportField.SOURCE_PUBLICSHERNAME.name());
		 selectedField.add(ExportField.SOURCE_COUNTRY.name());
		 selectedField.add(ExportField.SOURCE_PISSN.name());
		 selectedField.add(ExportField.SOURCE_EISSN.name()); //
		 selectedField.add(ExportField.CORR_AUTHORNAME.name());
		 selectedField.add(ExportField.CORR_COUNTRYCODE.name());
		 selectedField.add(ExportField.CORR_EMAIL.name());
		 selectedField.add(ExportField.CORR_AFFILIATION.name());
		 
		 selectedField.add(ExportField.GRANT_AGENCY.name());
		 selectedField.add(ExportField.GRANT_AGENCY_ACRONYM.name());
		 selectedField.add(ExportField.GRANT_AGENCY_ID.name());
		 selectedField.add(ExportField.GRANT_ID.name());
		 selectedField.add(ExportField.OPEN_ACCESS_STATUS.name());
	}
	
	public 이창환_20190819_SCOPUSSearcher(){
		
	}

	public 이창환_20190819_SCOPUSSearcher(String searchRule) throws Exception {
		System.out.println("1 " + downloadPath);
		setFieldInit();
		System.out.println("2 " + downloadPath);
		DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
				downloadFormat, selectedField, sort, isSort, isAdmin);
		System.out.println("3 " + downloadPath);
		su.execute();
		System.out.println("4 " + downloadPath);
	}

	public static void main(String... args) throws Exception {
		String searchRule = "AU=(\"Pollard, Jeffrey W.\")";
		/** SAMPLE */
		/*
		searchRule = " PY=(2003-2018) CU=(KOR)";
		searchRule = " EID=(85044590526 85020118974)";
		args = new String[] { "d:\\data\\scopus\\김정환박사\\201809\\1sample_tsv_", };
		if (args != null) {
			try {
				downloadPath = args[0];
				new File(downloadPath).getParentFile().mkdirs();
				logger.info("download path : {} ", downloadPath);
			} catch (Exception e) {
			}
			try {
				modelpath = args[1];
			} catch (Exception e) {
			}
		}
		*/
		// new DownloadScopusData(searchRule);
		// searchRule = "PY=(1999-2013)";
		for (int idx = 1997; idx <= 2019; idx++) {
			downloadPath = String.format("d:/data/이창환/20190819/최원준/SCOPUS_KOR_DOCUMENT_%s.tsv", idx);
			new File(downloadPath).getParentFile().mkdirs();
			searchRule = String.format("PY=(%s) CU=(KOR)", idx);
			logger.debug("searchRule {}", searchRule);
			logger.debug("download path {}", downloadPath);
			System.out.println(searchRule);
			System.out.println(downloadPath);
			new DownloadScopusData(searchRule);
		}

	}
	
}
