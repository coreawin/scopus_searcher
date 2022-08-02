package com.diquest.scopus.searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
 * @author coreawin
 * @date 2020. 8. 18.
 */
public class 이창환_20200814_SCOPUSSearcher {
	public static BlockingQueue<String> bQueue = new ArrayBlockingQueue<String>(5);
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
		selectedField.add(ExportField.NUMBER_REFERENCE.name());
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

	public 이창환_20200814_SCOPUSSearcher() {

	}

	public static class WorkerSearch implements Runnable {

		List<String> list = new LinkedList<String>();
		String delim = "";

		public WorkerSearch(List<String> list, String delim) {
			this.list = list;
			this.delim = delim;
		}
		
		@Override
		public void run() {
			String path = String.format("d:/data/2020/lee/20200814/%s/", this.delim);
			new File(path).mkdirs();
			for (String line : list) {
				String[] ds = line.split("\t");
				String searchRule = String.format("TI=(%s) ", ds[1].replaceAll("=|,|-|\\(|\\)|[α-ω]"," ").replaceAll("( and )|( or )|( not )", " ").replaceAll("\\s{1,}"," ").trim());
				System.out.println(this.delim +"\t" + ds[0] +"\t" + searchRule);
				DownloadSearchUtil.CURRENTSEARCHID = ds[0];
				downloadPath = String.format(path + "%s.tsv", ds[0]);
				if(new File(downloadPath).length() > 0){
					System.out.println("이미 작업 결과물이 있는 데이터입니다. " + ds[0]);
				}else{
					try {
						new DownloadScopusData(downloadPath, searchRule);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}

		}

	}

	public 이창환_20200814_SCOPUSSearcher(String searchRule) throws Exception {
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
		
//		for(int i=0; i<5; i++){
//			System.out.println(System.nanoTime());
//		}
		
//		String a= "Biotic and abiotic elicitors induce biosynthesis and accumulation of resveratrol with antitumoral activity in the long-term Vitis vinifera L. callus cultures";
//		System.out.println(a.replaceAll("( and )|( or )|( not )", ""));
//		if(true){
//			System.exit(1);
//		}
//		
//
		BufferedReader reader = null;

		try {
////			String divideFileName = "523319081506500.txt";
////			String divideFileName = "523319207330000.txt";
////			String divideFileName = "523319331421200.txt";
////			String divideFileName = "523319453376200.txt";
////			String divideFileName = "523319564365000.txt";
////			String divideFileName = "523319674483400.txt";
////			String divideFileName = "523319799357800.txt";
////			String divideFileName = "523319922468400.txt";
////			String divideFileName = "523320045729800.txt";
////			String divideFileName = "523320155663700.txt";
//
////			String divideFileName = "595440655393300.txt";
////			String divideFileName = "595440655453200.txt";
////			String divideFileName = "595440655467700.txt";
////			String divideFileName = "595440655479700.txt";
////			String divideFileName = "595440655490700.txt";
//			
////			String divideFileName = "1148384870650900.txt";
////			String divideFileName = "1148384870713600.txt";
//			String divideFileName = "1148384870735400.txt";
//		String divideFileName = "604180173897100.txt";
		String divideFileName = "604180173957100.txt";
		divideFileName = "604180173972300.txt";
		divideFileName = "604180173984600.txt";
		divideFileName = "604180173996300.txt";
//
//		
//		
//		
//		
//		

		
////			
////			
////						
//			
			String dir = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\20.이창환\\20200814\\divide\\";
			String fileName = dir + divideFileName;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
			String line = null;

			LinkedList<String> list = new LinkedList<String>();
			boolean isSearch = true;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				
				if(isSearch){
					list.add(line);
				}
			}
			new WorkerSearch(list, divideFileName.replaceAll(".txt", "")).run();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
