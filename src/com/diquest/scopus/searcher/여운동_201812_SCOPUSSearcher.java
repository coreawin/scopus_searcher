package com.diquest.scopus.searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kr.co.tqk.web.db.DescriptionCodeManager;
import kr.co.tqk.web.db.dao.export.ExportField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.kisti.DownloadSearchUtil;
import test.kisti.DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE;

/**
 * 
 * @author 정승한
 * @date 2018. 12. 7.
 */
public class 여운동_201812_SCOPUSSearcher {
	public static BlockingQueue<String> bQueue = new ArrayBlockingQueue<String>(1);
	static Logger logger = LoggerFactory.getLogger("DownloadScopusData.class");
	final String ip = "203.250.207.72";
	final int port = 5555;
	// final String downloadPath = "t:/tmp/scopusdownload/";
	static String downloadPath = "d:/test/";
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
		selectedField.add(ExportField.FIRST_ASJC.name());
		selectedField.add(ExportField.ASJC.name());
		selectedField.add(ExportField.NUMBER_CITATION.name());
		selectedField.add(ExportField.CITATION.name());
		selectedField.add(ExportField.NUMBER_REFERENCE.name());
		selectedField.add(ExportField.REFERENCE.name());
		selectedField.add(ExportField.CITATION_TYPE.name());
		selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
		selectedField.add(ExportField.AUTHOR_ID.name());
		selectedField.add(ExportField.AUTHOR_NAME.name());
		selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
		selectedField.add(ExportField.AUTHOR_EMAIL.name());
		selectedField.add(ExportField.AFFILIATION_ID.name());
		selectedField.add(ExportField.AFFILIATION_NAME.name());
		selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
		selectedField.add(ExportField.DELEGATE_AFFILIATION.name());
		selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
		selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
		selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
		selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());
		selectedField.add(ExportField.CORR_AUTHORNAME.name());
		selectedField.add(ExportField.CORR_COUNTRYCODE.name());
		selectedField.add(ExportField.CORR_EMAIL.name());
		selectedField.add(ExportField.CORR_AFFILIATION.name());

		/*
		 * selectedField.add(ExportField.TITLE.name());
		 * selectedField.add(ExportField.ABSTRACT.name());
		 * selectedField.add(ExportField.YEAR.name());
		 * selectedField.add(ExportField.DOI.name());
		 * selectedField.add(ExportField.KEYWORD.name());
		 * selectedField.add(ExportField.INDEX_KEYWORD.name());
		 */
		// selectedField.add(ExportField.FIRST_ASJC.name());
		/*
		 * selectedField.add(ExportField.ASJC.name());
		 * selectedField.add(ExportField.NUMBER_CITATION.name());
		 * selectedField.add(ExportField.CITATION.name());
		 * selectedField.add(ExportField.NUMBER_REFERENCE.name());
		 * selectedField.add(ExportField.REFERENCE.name());
		 * selectedField.add(ExportField.CITATION_TYPE.name());
		 * 
		 * selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
		 * selectedField.add(ExportField.AUTHOR_ID.name());
		 * selectedField.add(ExportField.AUTHOR_NAME.name());
		 * selectedField.add(ExportField.AUTHOR_EMAIL.name());
		 * selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
		 * selectedField.add(ExportField.AFFILIATION_ID.name());
		 * selectedField.add(ExportField.AFFILIATION_NAME.name());
		 * selectedField.add(ExportField.AFFILIATION_COUNTRY.name());
		 * selectedField.add(ExportField.DELEGATE_AFFILIATION.name());
		 * 
		 * selectedField.add(ExportField.FIRST_AUTHOR_NAME.name());
		 * selectedField.add(ExportField.FIRST_AUTHOR_EMAIL.name());
		 * selectedField.add(ExportField.FIRST_AUTHOR_COUNTRYCODE.name());
		 * selectedField.add(ExportField.FIRST_AFFILIATION_NAME.name());
		 * 
		 * selectedField.add(ExportField.SOURCE_ID.name());
		 * selectedField.add(ExportField.SOURCE_SOURCETITLE.name());
		 * selectedField.add(ExportField.SOURCE_VOLUMN.name());
		 * selectedField.add(ExportField.SOURCE_ISSUE.name());
		 * selectedField.add(ExportField.SOURCE_PAGE.name());
		 * selectedField.add(ExportField.SOURCE_TYPE.name());
		 * selectedField.add(ExportField.SOURCE_PUBLICSHERNAME.name());
		 * selectedField.add(ExportField.SOURCE_COUNTRY.name());
		 * selectedField.add(ExportField.SOURCE_PISSN.name());
		 * selectedField.add(ExportField.SOURCE_EISSN.name()); //
		 * selectedField.add(ExportField.CORR_AUTHORNAME.name());
		 * selectedField.add(ExportField.CORR_COUNTRYCODE.name());
		 * selectedField.add(ExportField.CORR_EMAIL.name());
		 * selectedField.add(ExportField.CORR_AFFILIATION.name());
		 * 
		 * selectedField.add(ExportField.GRANT_AGENCY.name());
		 * selectedField.add(ExportField.GRANT_AGENCY_ACRONYM.name());
		 * selectedField.add(ExportField.GRANT_AGENCY_ID.name());
		 * selectedField.add(ExportField.GRANT_ID.name());
		 * selectedField.add(ExportField.OPEN_ACCESS_STATUS.name());
		 */
	}
	
	public 여운동_201812_SCOPUSSearcher(){
		
	}

	public 여운동_201812_SCOPUSSearcher(String searchRule) throws Exception {
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
		System.out.println("SDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//		if(true) System.exit(0);
		String readfile = "./여운동/201903/scopus.searchrule.20190312";
		System.out.println("================> " + readfile);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(readfile)), "utf-8"));
		String line = null;

		Map<String, LinkedList<String>> datas = new HashMap<String, LinkedList<String>>();
		try {
			LinkedList<String> list = null;
			String k = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				System.out.println(line);
				if ("".equals(line)) {
					continue;
				}

				if (line.startsWith("@")) {
					
					if (list != null) {
						datas.put(k, list);
					}
					list = datas.get(line);
					k = line;
					if (list == null) {
						list = new LinkedList<String>();
					}
				} else {
					list.add(line);
				}
			}

			if (list != null) {
				datas.put(k, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				br.close();
		}
		Set<String> key = datas.keySet();
		
		BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("./여운동/201903/err.txt"))));
		int cnt = 0;
		for (String k : key) {
			List<String> l = datas.get(k);
			System.out.println(k + "\t" + l.size());
			cnt++;
			for (String tl : l) {

				String[] tls = tl.split("\t");
				System.out.println(tls[0] + "\t|\t" + tls[1]);
				
				if(tls[0].startsWith("#")) continue;
				
				String searchRule = tls[1];
				
//				downloadPath = String.format("e:/kisti/download/yeo/scopus/20181213/%s_%s.txt", k.replaceAll("@", "") , tls[0]);
				downloadPath = String.format("d:/data/yeo/scopus/%s_%s.txt", k.replaceAll("@", "") , tls[0].replaceAll("/", "_"));
				new File(downloadPath).getParentFile().mkdirs();
				try{
//					if(cnt==1){
//						new 여운동_201812_SCOPUSSearcher(searchRule + " PY=(2013-2018)" );	
//						continue;
//					}
					
					searchRule +=  " PY=(2013-2018)" ;
					logger.debug("searchRule {}", searchRule);
					logger.debug("download path {}", downloadPath);
//					bQueue.put("abc");
					new 여운동_201812_SCOPUSSearcher(searchRule);
//					new Thread(new Worker(searchRule)).start();
//				new 여운동_201812_SCOPUSSearcher(searchRule);
				}catch(Exception e){
					e.printStackTrace();
					bwr.write(downloadPath);
					System.err.println(">>>>>>> " + downloadPath);
				}
				}
		}

//		String searchRule = "AU=(\"Pollard, Jeffrey W.\")";
//		searchRule = " PY=(2003-2018) CU=(KOR)";
//		searchRule = " EID=(85044590526 85020118974)";
//		args = new String[] { "d:\\data\\scopus\\김정환박사\\201809\\1sample_tsv_", };
//		if (args != null) {
//			try {
//				downloadPath = args[0];
//				new File(downloadPath).getParentFile().mkdirs();
//				logger.info("download path : {} ", downloadPath);
//			} catch (Exception e) {
//			}
//			try {
//				modelpath = args[1];
//			} catch (Exception e) {
//			}
//		}
//		// new DownloadScopusData(searchRule);
//		// searchRule = "PY=(1999-2013)";
//		for (int idx = 1996; idx <= 2018; idx++) {
//			downloadPath = String.format("d://data/scopus/김정환박사/20181007/SCOPUS_KOR_DOCUMENT_%s.tsv", idx);
//			new File(downloadPath).getParentFile().mkdirs();
//			searchRule = String.format("PY=(%s) CU=(KOR)", idx);
//			logger.debug("searchRule {}", searchRule);
//			logger.debug("download path {}", downloadPath);
//			System.out.println(searchRule);
//			System.out.println(downloadPath);
//			new DownloadScopusData(searchRule);
//		}

	}
	
}
