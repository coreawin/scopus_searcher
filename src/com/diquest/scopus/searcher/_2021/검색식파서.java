package com.diquest.scopus.searcher._2021;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.util.AReadFile;

public class 검색식파서 extends AReadFile {

	// Map 순서대로 키값 정의 FileName, SheetName, ColumnIdx

	static Logger logger = LoggerFactory.getLogger(검색식파서.class.getName());

	LinkedList<Map<String, String>> dataSet = new LinkedList<Map<String, String>>();
	public Map<String, String> keys = new LinkedHashMap<String, String>();
	/**
	 * 기술별검색건수 저장.
	 */
	public Map<String, Integer> scount = new LinkedHashMap<String, Integer>();

	public 검색식파서(String path) throws IOException {
		super(path);
		keys = new LinkedHashMap<String, String>();
		super.loadFile();
	}

	boolean start검색식별자 = false;
	boolean start검색식 = false;
	String 검색식 = "";
	String 세부기술명 = "";
	String 검색아이디 = "";
	String 검색건수 = "";
	String titleLine = "";
	String[] 식별자구분 = null;

	public String get검색아이디(String src) {
		Pattern p = Pattern.compile("T[0-9]{1,2}");
		Matcher m = p.matcher(src);
		while (m.find()) {
			return m.group().trim();
		}
		return null;
	}
	
	public String get검색갯수(String src) {
		Pattern p = Pattern.compile("[\\d,]*(\\.?\\d*)\\s{0,}[건|개]");
		Matcher m = p.matcher(src);
		while (m.find()) {
			return m.group().trim();
		}
		return null;
	}

	@Override
	public void readline(String line) {
		String[] _식별자구분 = line.split("\t");
		if (_식별자구분.length < 1)
			return;
		// System.out.println(_식별자구분.length);
		String _검색아이디 = _식별자구분[0].trim();
		
		// logger.info("_검색아이디 {}", _검색아이디);
		if (start검색식별자) {
			검색식 = line;
			start검색식별자 = false;
			// logger.info("AAA {}", line);
			keys.put(검색아이디 + ":" + 세부기술명, line);
			scount.put(검색아이디 + ":" + 세부기술명, Integer.parseInt(검색건수));
			return;
		}

		if (_검색아이디.startsWith("T")) {
			int startNu = 1;
			if (_검색아이디.length() != 3) {
				startNu = 0;
				_검색아이디 = get검색아이디(_검색아이디);
				titleLine= line.replaceAll("\t", " ").replaceAll(_검색아이디, "");
				_식별자구분 = titleLine.split("@");
			}
			logger.debug("_검색아이디 1  {}", _검색아이디);
			logger.debug("_titleLine 2  {}", titleLine);

			// 신규 검색 아이디 시작
			식별자구분 = _식별자구분;
			for (int i = startNu; i < 식별자구분.length; i++) {
				String a = 식별자구분[i].trim();
				logger.debug("_식별자구분 {} /   {}", a, i);
				if ("".equalsIgnoreCase(a)) {
					continue;
				} else {
					세부기술명 = a.replaceAll("\\s{1,}", "").replaceAll("/", "_");
				}
			}
				logger.debug("세부기술명 {} /   {}", 세부기술명);
			String[] 기술정보 = 세부기술명.split(":");
			세부기술명 = 기술정보[0]; // 명칭만 뽑아낸다. 건수는 제외한다.
			if(세부기술명.indexOf(":")!=-1){
				검색건수 = 기술정보[1].replaceAll("[^0-9]", "").replaceAll("\\s{1,}", "");
			}else{
				String 갯수 = get검색갯수(line).replaceAll("건|개|,|\\s", "").trim();
				if("".equals(갯수)){
					검색건수 = "0";
				}else{
					검색건수 = Integer.parseInt(갯수)+"";
				}
				logger.debug("검색갯수 : " + 검색건수);
			}
			
			검색아이디 = _검색아이디;
//			logger.info("세부기술명  : {} ", 세부기술명);
			start검색식별자 = true;
		} else {
			start검색식별자 = false;
		}
//		System.out.println("B " + start검색식별자);
	}
}
