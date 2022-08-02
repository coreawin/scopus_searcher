package com.diquest.scopus.searcher._2021.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kr.co.tqk.web.db.DescriptionCodeManager;
import kr.co.tqk.web.db.dao.CnProgramDao;
import kr.co.tqk.web.db.dao.export.ExportField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.scopus.util.DownloadSearchUtil;
import com.diquest.scopus.util.DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE;
import com.diquest.util.AReadFile;

/**
 * <P>
 * A. 작업방법 기술검색식을 이용하여 먼저 SCOPUS, PATENT 다운로드를 시도한다.<br>
 * 1. NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER<br>
 * 2. NEON_ECLIPSE_KISTI_PATENT_SEARCHER<br>
 * _2021.Rnd package.ScopusSearcher_2021.java <br>
 * </P>
 *
 * <P>
 * B. 정량분석 지표를 추출한다. <br>
 * pe.neon 프로젝트의 아래 클래스를 이용해 논문/특허 정량지표를 추출한다.<br>
 * pe.neon.여운동._202101.Launcher4Patent <br>
 * pe.neon.여운동._202101.Launcher4Scopus<br>
 * 결과물 : <br>
 * 1. RESULT_SCOPUS_기술명_작업일.txt <br>
 * 2. RESULT_PATENT_기술명_작업일.txt<br>
 * </P>
 *
 * <P>
 * C. pe.eclipse.neon 프로젝트를 이용하여 경제적 특성 정보를 최종 취합한다.<br>
 * pe.eclipse.neon.yeo._2021.Extract경제적특성_2021
 * </P>
 *
 * 안녕하세요 미소테크 박진현입니다. <br/>
 *
 * R&D PIE 시스템 데이터 관련 메일 드립니다.<br/>
 * 우선 정리 완료된 4개 분과(전체 16개분과 중) 논문, 특허검색식 전달 해드립니다.<br/>
 * 검색식 부분에 KISTI에서 검색시 결과 건수도 같이 표기 했습니다. 건수가 비슷하게 나오면 될듯합니다.<br/>
 * 이 부분에 대한 작년과 동일한 작업 요청 드리겠습니다.<br/>
 * (분과별 기술군별 논문/특허 결과, 10대 지표값 등)<br/>
 * 일전 말씀드린바와 같이 아래와 같은 필드 추가 요청 드립니다.<br/>
 *
 * <논문><br/>
 *
 * - 피인용수: Number of Citation<br/>
 * Check - 저자 국가 식별: Author Country<br/>
 * Check - 저자명 식별: Author ID<br/>
 * Check - 저자명: Author Name<br/>
 * Check - 저자 통합 정보: Author Info<br/>
 * Check - 기관명 국가 식별: Affiliation Country<br/>
 * Check - 기관명 식별: Affiliation IDs<br/>
 * Check - 기관명: Affiliation Name<br/>
 * Check
 *
 * <특허><br/>
 *
 * - 출원인 국적: assignee-country<br/>
 * check - 대표출원인(정제된 것?): app_gp<br/>
 * check - 출원인 국적+명: assignee<br/>
 * check - 피인용수; citation-count<br/>
 * check - 등록번호:<br/>
 * ==> KIND 항목이 Grant 인것 check
 *
 *
 * @author coreawin
 * @date 2021. 1. 9.
 */
public class ScopusSearcher_2021 {
	public static BlockingQueue<String> bQueue = new ArrayBlockingQueue<String>(1);
	static Logger logger = LoggerFactory.getLogger(ScopusSearcher_2021.class.getName());
	final String ip = "203.250.207.72";
	final int port = 5555;

	// final String downloadPath = "t:/tmp/scopusdownload/";

	static String modelpath = "t:/release/KISTI/SCOPUS_2014_WEB/models/ExportBasicFormat.xlsx";
	final DOWNLOAD_FILE_FORMAT_TYPE downloadFormat = DOWNLOAD_FILE_FORMAT_TYPE.TAB_DELIMITED;
	final String sort = "";
	final boolean isSort = false;
	final boolean isAdmin = true;
	private Set<String> selectedField = new HashSet<String>();
	final int sidx = 0;
	int eidx = Integer.MAX_VALUE;
	
	int searchCntLimit = -1; //제한없음.
	
// 웹에서 아래 필드는 다운로드 되나 해당 프로그램에서는 안됨.
//	first ASJC
//	Affiliation Citygroup
//	Affiliation AddressPart
//	Affiliation City
//	Affiliation State
//	Affiliation PostalCode
//	Affiliation Citygroup
//	Affiliation AddressPart
//	Affiliation City
//	Affiliation State
//	Affiliation PostalCode
//	OPEN_ACCESS_STATUS
//	OPEN_ACCESS_STATUS_CODE
//	OPEN_ACCESS_URL
	
	enum ERule {
		ABS, LIMIT
	}

	private void setExtentionRule(ERule type, String value) {
		switch (type) {
		case ABS:
			if (value.trim().equalsIgnoreCase("Y")) {
				selectedField.add(ExportField.ABSTRACT.name());
				logger.info("초록을 다운로드 합니다.");
			}
			break;
		case LIMIT:
			eidx = Integer.parseInt(value);
			logger.info("검색 결과 갯수 제한 {}", eidx);
			searchCntLimit = eidx;
			if (eidx > 0)
				eidx -= 1;
			break;
		default:
			break;
		}
	}

	private void setFieldInit(Set<String> extentionRule) {
		eidx = Integer.MAX_VALUE;
		boolean existABSField = false;
		for (String er : extentionRule) {
			try {
				int limit = Integer.parseInt(er);
				// 오류가 안나면 검색결과 갯수 제한.
				setExtentionRule(ERule.LIMIT, String.valueOf(limit));
			} catch (NumberFormatException nfe) {
				// 오류가 나면 초록 다운로드 여부.
				existABSField = true;
				setExtentionRule(ERule.ABS, er);
				continue;
			}
		}
		
		if(existABSField==false){
			// Default로 ABS 다운로드 기능을 뺀다. @coreawin 2022-08-02
//			selectedField.add(ExportField.ABSTRACT.name());
		}

		selectedField.add(ExportField.TITLE.name());
//		selectedField.add(ExportField.ABSTRACT.name());
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
//		 selectedField.add(ExportField.OPEN_ACCESS_STATUS.name());
	}

	/**
	 * @param searchRule
	 * @param downloadPath
	 * @param eSr
	 *            확장 룰 요소 추가 (2022.02.02)
	 * @throws Exception
	 */
	public ScopusSearcher_2021(String searchRule, String downloadPath, Set<String> eSr) throws Exception {
		setFieldInit(eSr);
		DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
				downloadFormat, selectedField, sort, isSort, isAdmin, searchCntLimit);
		su.execute();
	}

	public static void loadDescription() {
		DescriptionCodeManager instance = DescriptionCodeManager.getInstance();
		while (instance.isRunning == true) {
			logger.info("Part1 : 정보 로딩이 완료되기위해 대기중입니다.");
			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	public static void help() {
		System.out.println("기존의 결과파일과 동일한 파일명이 존재한다면 삭제하지 않고, 새롭게 검색하지 않습니다. 즉 기존의 파일데이터가 보존됩니다.");
	}

	static ConfProp cp = null;

	public static void loadConf() {
		try {
			cp = new ConfProp();
		} catch (IOException e) {
			System.out.println("설정파일이 존재하지 않습니다. ");
			System.exit(-1);
		}
	}

	public static void main(String... args) throws Exception {
		Lock l = new ReentrantLock();
		// if(true){
		// LocalDateTime now = LocalDateTime.now();
		// System.out.println(now);
		// LocalDateTime now2 = now.minusMinutes(5);
		// System.out.println(now2);
		// System.out.println(now.isBefore(now2));
		// System.out.println(now2.isBefore(now));
		// System.exit(-1);
		// }
		loadConf();
		new Thread(new Checker()).start();
		synchronized (l) {
			l.wait(2000);
		}
		String searchRuleFilePath = cp.getProperty("searchrule.path");
		String downloadPath = cp.getProperty("download.path");

		logger.info("searchRule.path : {}", searchRuleFilePath);
		logger.info("download.path : {}", downloadPath);

		if (searchRuleFilePath == null || downloadPath == null) {
			System.out.println("사용법 : conf 디렉토리에 있는 setup.conf 파일을 설정해 주세요.");
			System.out.println("searchrule.path : 검색식 파일(*.tsv)이 있는 디렉토리 ");
			System.out.println("download.path : 검색결과가 저장될 디렉토리");
			System.exit(-1);
		}
		restrict();
		loadDescription();

		new File(downloadPath).mkdirs();

		File f = new File(searchRuleFilePath);
		File[] dirs = f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.lastIndexOf(".tsv") != -1) {
					return true;
				}
				return false;
			}
		});
		int count = 0;
		for (File dir : dirs) {
			logger.info("read path {}", dir.getAbsolutePath());

			ScopusSearchRuleReader ssrr = new ScopusSearchRuleReader(dir.getAbsolutePath());
			LinkedHashMap<String, String> lhm = ssrr.getRules();

			Set<String> 다운로드파일명 = lhm.keySet();

//			for (String 파일명 : 다운로드파일명) {
//				logger.info("검색아이디 / 검색식  : {} => {}", 파일명, lhm.get(파일명));
//			}

			// if(true) System.exit(0);

			for (String 파일명 : 다운로드파일명) {
				restrict();
				logger.info("검색을 진행합니다. : {} => {}", 파일명, lhm.get(파일명));
				String dp = downloadPath + File.separator + 파일명 + (파일명.toLowerCase().lastIndexOf(".txt") == -1 ? ".txt" : "");
				String 검색식 = lhm.get(파일명).replaceAll("[\\r|\\n]", "").replaceAll("\\s{1,}", " ").trim();
				/*
				 * 경제적 특성을 추출하려면 특허의 파일명과 동일해야 한다. @since PatentSearcher_2020
				 */
				File downloadFile = new File(dp);
				downloadFile.getParentFile().mkdirs();
				if (downloadFile.isFile()) {
					if (downloadFile.length() < 15000) {

					} else {
						logger.warn("이미 만들 파일은 건너 뛴다. {}", dp);
						 continue;
					}
				}
				Set<String> extentionRule = new HashSet<String>();
				검색식 = 검색식.replaceAll("“", "\"").replaceAll("”", "\"").trim();
				StringBuffer buf = new StringBuffer();
				boolean startAt = false;
				boolean startEq = false;
				StringBuilder bb = new StringBuilder();
				for (char c : 검색식.toCharArray()) {
					if (c == ' ' && startAt) {
						startAt = false;
						startEq = false;
						extentionRule.add(bb.toString());
						bb.setLength(0);
						continue;
					}

					if (c == '#') {
						startAt = true;
						continue;
					}

					if (startAt && c == '=') {
						String eSearchRule = bb.toString();
						boolean verifyESR = false;
						if (eSearchRule.equalsIgnoreCase("limit") || eSearchRule.equalsIgnoreCase("abs")) {
							verifyESR = true;
						}
						if (verifyESR == false) {
							throw new Exception("유효하지 않은 확장 검색식이 들어왔습니다. ");
						}
						bb.setLength(0);
						startEq = true;
						continue;
					}

					if (startAt && startEq) {
						bb.append(c);
						continue;
					}

					if (startAt) {
						if (!startEq) {
							bb.append(c);
						}
						continue;
					}

					int cc = (int) c;
					if (cc >= 30 || 130 <= cc) {
						buf.append(c);
					}
				}
				if (bb.length() > 0) {
					extentionRule.add(bb.toString());
					bb.setLength(0);
					;
				}
				검색식 = buf.toString();
				logger.info("searchRule	=>	{}", 검색식.trim());
//				logger.info("extentionRule	=>	{}", extentionRule);
//				logger.info("download path	=>	{}", dp);

				if (count > 0) {
					Dermy d = new Dermy();
					synchronized (d) {
						d.wait(1000 * 10 * (new Random(System.nanoTime()).nextInt(5) + 1));
					}
				}
				try{
				new ScopusSearcher_2021(검색식.trim(), dp, extentionRule);
				}catch(Exception e){
					e.printStackTrace();
					//오류가 나더라도 다음 검색식을 던진다. 다만 검색식 오류일 확율이 높으니 1분정도 대기한다.
					synchronized (l) {
						l.wait(1000*60);
					}
				}
				count += 1;

			}
		}

	}
	
	private static boolean 검색제한없애도되는날인가(){
		LocalDate to_date = LocalDate.now();
		DayOfWeek week = to_date.getDayOfWeek();
		logger.debug("오늘 요일 (day_of_week) : " +  week);
		switch (week) {
		case SATURDAY:
		case SUNDAY:
			return true;
		default:
			return false;
		}
	}

	public static void restrict() {
		Dermy d = new Dermy();
		while (true) {
			synchronized (d) {
				try {
					// 1분마다 체크.
					if(검색제한없애도되는날인가()==false){
						LocalTime now = LocalTime.now();
						int hour = now.getHour();
						// System.out.println(now + " \t " + hour);
						if ((hour >= 19 && hour <= 23) || (hour >= 0 && hour <= 6)) {
							break;
						} else {
//							break;
							logger.warn("현재는 검색가능한 시간이 아닙니다 : 19:00~07:00 사이에만 검색 가능. (주말-토.일-에도 가능합니다.)");
							d.wait(1000 * 60 * 1);
							continue;
						}
					}else{
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static class ScopusSearchRuleReader extends AReadFile {

		public ScopusSearchRuleReader(String path) throws IOException {
			super(path);
			super.loadFile();
		}

		private LinkedHashMap<String, String> rules = new LinkedHashMap<>();

		public LinkedHashMap<String, String> getRules() {
			return this.rules;
		}

		@Override
		public void readline(String line) {
			if (line == null)
				return;
			line = line.trim();
			if (line.startsWith("@")) {
				String[] datas = line.split("\t");
				if (datas.length > 1) {
					String fileName = datas[0].substring(1).trim();
					String searchRule = datas[1].trim();
					rules.put(fileName, searchRule);
					logger.debug("put rule : " + fileName);
				}
			}
		}

	}

	public static class Checker implements Runnable {
		CnProgramDao dao = new CnProgramDao();

		@Override
		public void run() {
			while (true) {
				String ip;
				try {
					ip = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
					ip = "127.0.0.1d";
				}
				if (dao.이미실행중인프로그램인가(ip, "SCOPUS")) {
					System.out.println("이미 프로그램이 실행중입니다. 기존 프로그램을 종료해주시거나 잠시후에 다시 시도해 주세요.");
					System.exit(-1);
				} else {
					System.out.println("프로그램을 실행하고 있습니다. 잠시만 기다려 주세요.");
					dao.updateChecker(ip, "SCOPUS");
				}
				synchronized (this) {
					try {
						this.wait(1000 * 60 * 5);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				break;
			}
		}

	}

	public static class ConfProp extends Properties {
		public ConfProp() throws IOException {
			String ff = new File(new Dermy().getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
					.getParentFile().getAbsolutePath();
			System.out.println(ff);
			if (ff.contains("NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER")) {
				load(new FileInputStream(new File("./conf/setup.conf")));
			} else {
				String f = new File(new Dermy().getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
						.getParentFile().getParent();
				System.out.println("설정파일의 위치 : " + new File(f + File.separator + "/conf/setup.conf").getAbsolutePath());
				// System.out.println(new File(f + File.separator +
				// "/conf/setup.conf").isFile());
				load(new FileInputStream(new File(f + File.separator + "/conf/setup.conf")));
			}
		}
	}

}
