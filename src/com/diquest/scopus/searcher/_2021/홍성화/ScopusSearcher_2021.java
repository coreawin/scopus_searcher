package com.diquest.scopus.searcher._2021.홍성화;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kr.co.tqk.web.db.DescriptionCodeManager;
import kr.co.tqk.web.db.dao.export.ExportField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.scopus.searcher._2021.검색식파서;
import com.diquest.scopus.util.DownloadSearchUtil;
import com.diquest.scopus.util.DownloadSearchUtil.DOWNLOAD_FILE_FORMAT_TYPE;

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

		selectedField.add(ExportField.AUTHOR_AUTHORINFO.name());
		selectedField.add(ExportField.AUTHOR_ID.name());
		selectedField.add(ExportField.AUTHOR_NAME.name());
		// selectedField.add(ExportField.AUTHOR_EMAIL.name());
		selectedField.add(ExportField.AUTHOR_COUNTRYCODE.name());
		selectedField.add(ExportField.AFFILIATION_ID.name());
		selectedField.add(ExportField.AFFILIATION_NAME.name());
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

	public ScopusSearcher_2021(String searchRule, String downloadPath) throws Exception {
		setFieldInit();
		DownloadSearchUtil su = new DownloadSearchUtil(ip, port, sidx, eidx, searchRule, downloadPath, modelpath,
				downloadFormat, selectedField, sort, isSort, isAdmin);
		su.execute();
	}

	public static void loadDescription() {
		DescriptionCodeManager instance = DescriptionCodeManager.getInstance();
		while (instance.isRunning == true) {
			logger.info("Part1 : 정보 로딩이 완료되기위해 대기중입니다.");
			try {
				Thread.sleep(1000 * 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	public static void main(String... args) throws Exception {
		loadDescription();
		String downloadPath = "d:/data/2021/홍성화/download";
		String downloadDateFormat = "20210415";
		// String[] 분과명정보 = new String[] {
		// "고기능무인기", "수소에너지", "스마트팜", "연료전지", "친환경차", "태양광"
		// };
		// 다운로드 폴더 미리 생성.
		new File(downloadPath).mkdirs();

		String searchRuleFilePath = "D:/eclipse_workspace/workspace_luna/public/NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER/홍성화/2021/"+ File.separator;

		File f = new File(searchRuleFilePath);
		File[] dirs = f.listFiles();
		String 분과명 ="";
		for (File dir : dirs) {
			logger.info("read path {}", dir.getAbsolutePath());
			검색식파서 searchParser = new 검색식파서(dir.getAbsolutePath());
			Map<String, String> searchParsers = searchParser.keys;

			Set<String> 검색식아이디정보 = searchParsers.keySet();

			for (String 검색식아이디 : 검색식아이디정보) {
				String dp = downloadPath;
				// logger.info("검색식아이디 : {} => {}", 검색식아이디,
				// searchParsers.get(검색식아이디));
				String[] 검색식아이디분절 = 검색식아이디.split(":");
				String techID = 검색식아이디분절[0];

				String techName = 검색식아이디분절[1].replaceAll("[\\r|\\n|\\<]", "").replaceAll("\\s{1,}", " ").replaceAll("/", "-")
						.replaceAll(" ", "_").trim();
				String 검색식 = searchParsers.get(검색식아이디).replaceAll("[\\r|\\n]", "").replaceAll("\\s{1,}", " ").trim();

				dp = String.format(dp + File.separator + downloadDateFormat + "\\%s\\scopus\\%s.txt", 분과명, techID + "_"
						+ techName.replaceAll("\\s{1,}", "").replaceAll("[-_\\.\\<]", "").replaceAll("\\.txt", "").trim());

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

				검색식 = 검색식.replaceAll("“", "\"").replaceAll("”", "\"").trim();
				StringBuffer buf = new StringBuffer();
				for (char c : 검색식.toCharArray()) {
					int cc = (int) c;
					if (cc >= 30 || 130 <= cc) {
						// logger.info("{}", cc);
						buf.append(c);
					}
				}
				검색식 = buf.toString();
				logger.info("searchRule	=>	{}", 검색식.trim());
				logger.info("download path	=>	{}", dp);
				logger.info("techName	=>	{} ", techName);
				logger.info("techID	=>	{} ", techID);
				new ScopusSearcher_2021(검색식.trim(), dp);
			}
		}

	}

}
