package com.diquest.scopus;

import com.diquest.scopus.searcher.IBSSearchAllTextFile;

public class ExecutorIBSSearch {
	
	public final static String marinerIP = "203.250.207.72";
	public final static String marinerCollection = "SCOPUS_2020";
	public final static int marinerPort = 5555;
	
	
	public static void main(String[] args) throws Exception {
//		System.setProperty("EJIANA_HOME", "D:\\Project_WorkSpace\\2014\\SCOPUS_IBS_SEARCH");
		String path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\180809(7월)";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\180911(8월)";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181011-9월";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181108-10월";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181203-11월";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181221-12월";
		path = "c:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181221-12월";
		path = "f:\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\1-4월";
		path = "f:\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\6월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\9월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\11월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\12월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\12월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\2020년도 4월까지 신규임용자 CV(논문 리스트)\\4월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\5-6월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\7-8월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\11월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\1-5월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\5-6월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\7-8월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\9-10월";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2022\\6월";
		
		
		args = new String[]{
//			"-srf:/coreawin/Documents/Project/2018/KISTI/10.IBS/20180423/180423_1-3월신규입사자/titles",
//			"-tgf:/coreawin/Documents/Project/2018/KISTI/10.IBS/20180423/180423_1-3월신규입사자/result",
			"-sr"+path+"\\titles",
			"-tg"+path+"\\result",
		};
		InputParameter param = new InputParameter(args);
//		IBSSearch ibs = new IBSSearch(param);
//		ibs.search();
//		IBSSearchTextFile ibs2 = new IBSSearchTextFile(param);
//		ibs2.search();
		System.out.println("===> " + param.target);
		IBSSearchAllTextFile ibs3 = new IBSSearchAllTextFile(param);
		ibs3.search();
	}

	public static class InputParameter {
		public String source;
		public String target;

		public InputParameter(String[] params) throws Exception {
			for (String s : params) {
				parseParameter(s);
			}
		}

		private void parseParameter(String s) throws Exception {
			String param = s.substring(3);
			if (s.startsWith("-sr")) {
				source = param;
			} else if (s.startsWith("-tg")) {
				target = param;
			}
		}
	}
}
