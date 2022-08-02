package com.diquest.scopus.searcher._2021;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.scopus.util.AReadFile;

public class 검색식파서_OLD extends AReadFile {
	
//	Map 순서대로 키값 정의 FileName, SheetName, ColumnIdx
	
	static Logger logger = LoggerFactory.getLogger(검색식파서.class.getName());

	LinkedList<Map<String, String>> dataSet = new LinkedList<Map<String, String>>();
	public Map<String, String> keys = new LinkedHashMap<String, String>();
	/**
	 * 기술별검색건수 저장. 
	 */
	public Map<String, Integer> scount = new LinkedHashMap<String, Integer>();
	 
	
	public 검색식파서_OLD(String path) throws IOException {
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
	String[] 식별자구분 = null;
	
	@Override
	public void readline(String line) {
		line = line.trim();
		String[] _식별자구분 = line.split("\t");
		String _검색아이디 = _식별자구분[0].trim();

		if(start검색식별자){
			검색식 = line;
			start검색식별자 = false;
			keys.put(검색아이디+":"+세부기술명, line);
			scount.put(검색아이디+":"+세부기술명, Integer.parseInt(검색건수));
			return;
		}
		
		if(_검색아이디.startsWith("T")  && _검색아이디.length() == 3){
//			logger.info("AAA {} / {}", _검색아이디, _식별자구분.length+" / " +   line.indexOf("\t")+"");
			//신규 검색 아이디 시작
			식별자구분 = _식별자구분;
			for(int i  = 1; i<식별자구분.length; i++){
				String  a = 식별자구분[i].trim();
				System.out.println(a);
				if("".equalsIgnoreCase(a)){
					continue;
				}else{
					세부기술명 = a.replaceAll("\\s{1,}",  "");
				}
			}
			String[] 기술정보 = 세부기술명.split(":");
			세부기술명 = 기술정보[0]; // 명칭만 뽑아낸다. 건수는 제외한다.
			검색건수 = 기술정보[1].replaceAll("[^0-9]", "").replaceAll("\\s{1,}",  "");
			검색아이디 = _검색아이디;
			logger.info("세부기술명  : {} ",  세부기술명);
			start검색식별자 = true;
		}else{
			start검색식별자 = false;
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		검색식파서_OLD searchParser = new 검색식파서_OLD("D:/eclipse_workspace/workspace_luna/public/NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER/2021/0109/자율주행차/(논문검색식)-자율주행차-최붕기-20210226.txt");
		searchParser.loadFile();
	}
	

}
