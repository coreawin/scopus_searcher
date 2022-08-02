package com.diquest.scopus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractTitles {

	LinkedList<String> lineList = new LinkedList<String>();

	public ExtractTitles(String path) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "UTF-8"));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			 regex(line);
//			delimLine(line);
		}
		for(String s : this.lineList){
			System.out.println(s.trim());
		}
		br.close();
	}

	StringBuilder buf = new StringBuilder();

	public LinkedList<String> delimLine(String line) {
		String firstLinePattern = ""; // 라인의 시작 조건
		firstLinePattern = "^([0-9]{1,2}\\)\\s)"; // 1) 로 시작하는 패턴인 경우
		firstLinePattern = "^([0-9]{1,2}\\.\\s)"; // 1. 로 시작하는 패턴인 경우
//		firstLinePattern = "^(\\[[0-9]{1,2}\\]\\s)"; //[1]. 로 시작하는 패턴인 경우

		String endLine = ""; // 라인의 끝 조건
		endLine = ".";// .이 나타나면 라인이 끝남.
//		endLine = "~"; // 라인 끝나는 조건이 시작 조건과 같다면

		Matcher lineStartMatcher = Pattern.compile(firstLinePattern).matcher(line);

		boolean match = false;
		while (lineStartMatcher.find()) {
			// line의 시작임을 뜻함.
			match = true;
//			System.out.println("start " + line);
			String find = lineStartMatcher.group().trim();
			line = line.substring(line.indexOf(find)+find.length()+1, line.length());
//			System.out.println(buf.length() +"," + line);
			if (line.indexOf(endLine) != -1) {
				this.lineList.add(buf.toString() + line.substring(0, line.indexOf(endLine)));
				buf.setLength(0);
			} else {
				if("~".equals(endLine) && buf.length()>0){
					this.lineList.add(buf.toString());
					buf.setLength(0);
					buf.append(line.replaceAll("\r\n", " "));
				}else{
					buf.append(" ");
					buf.append(line.replaceAll("\r\n", " "));
				}
			}
			match = true;
		}
		if(match == false && buf.length() > 0){
//			System.out.println(line);
			if (line.indexOf(endLine) != -1) {
				buf.append(" ");
				this.lineList.add(buf.toString() + line.substring(0, line.indexOf(endLine)));
				buf.setLength(0);
			} else {
				buf.append(" ");
				buf.append(line.replaceAll("\r\n", " "));
			}
		}
		
		if("~".equals(endLine) && buf.length()>0 && match==false){
			this.lineList.add(buf.toString());
			buf.setLength(0);
		}
		return this.lineList;
	}

	/**
	 * 정규식으로 제목 추출
	 * 
	 * @author 정승한
	 * @date 2018. 4. 24.
	 * @param line
	 * @throws Exception
	 */
	public void regex(String line) throws Exception {
		String pattern = "(?<=“).+(?=”)";
		pattern = "(?<=“).+(?=”)";
		pattern = "(?<=\").+?(?=\")";
		pattern = "(?<=\\.).+";
//		pattern = "(?<=:).+(?=\\..)";
//		pattern = "(?<=(\\([0-9]{4}\\))).+(?=\\..)";
//		pattern = "(?<=([0-9]{4}\\.\\s)).+?(?=\\..)";
//		 pattern = "^([0-9]{1,2}).+";

		Pattern p = Pattern.compile(pattern);
		line = line.trim();
		if ("".equals(line))
			return;
		Matcher m = p.matcher(line);
		
		while (m.find()) {
			String find = m.group().trim();
			System.out.println(find.replaceAll("\\s{1,}", " "));
			// if(find.indexOf(",")!=-1){
			// System.out.println(find.substring(find.indexOf(" ") +1,
			// find.indexOf(",")));
			// }else{
			// System.out.println(find.substring(find.indexOf(" ") +1));
			// }
		}
	}

	public static void main(String[] args) throws Exception {
//		new ExtractTitles("F:/coreawin/Documents/GitHub/Research_SUPPORT/SCOPUS_IBS_SEARCH/pattern/titles");
		String file = "c:/Users/정승한/OneDrive - hansung.ac.kr/2018.KISTI/10.IBS/20181203-11월/raw_title.txt";
		new ExtractTitles(file);
	}

}
