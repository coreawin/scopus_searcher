package com.diquest.scopus.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import kr.co.diquest.search.SearchQueryHelper;
import kr.co.diquest.search.rule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import kr.co.diquest.search.rule.QueryConverterWoS;
import kr.co.tqk.web.db.DescriptionCodeManager;
import kr.co.tqk.web.db.dao.CnProgramDao;
import kr.co.tqk.web.db.dao.export.ExportCluster;
import kr.co.tqk.web.db.dao.export.ExportDocumentSax;
import kr.co.tqk.web.db.dao.export.ExportExcelJXL;
import kr.co.tqk.web.db.dao.export.ExportTabDelimited;
import kr.co.tqk.web.db.dao.export.ExportText;
import kr.co.tqk.web.util.UtilDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.ext.body.common.ErrorMessage;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.GroupBySet;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;

/**
 * 
 * 
 * 검색되어 나온 결과를 다운로드 받을 수 있게 서버에 파일로 생성한다. <br>
 * 
 * 
 * @참조 : GPass 검색 서비스 PatentDownloadFileService.java
 * 
 * @fileName : ExportSearchUtil.java
 * @author : 이관재
 * @date : 2014. 5. 13.
 * @Version : 1.0
 */
public class DownloadSearchUtil {

	Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * 
	 * 다운로드 포맷 타입
	 * 
	 * @fileName : ExportSearchUtil.java
	 * @author : 이관재
	 * @date : 2014. 5. 13.
	 * @Version : 1.0
	 */
	public enum DOWNLOAD_FILE_FORMAT_TYPE {
		CLUSETER, EXCEL, TEXT, TAB_DELIMITED
	}

	/**
	 * 
	 * Expot를 위한 Select Set 정보
	 * */
	private static final SelectSet[] DOWNLOAD_SELECT_SET = new SelectSet[] {
			new SelectSet(SCOPUS_MARINER_FIELD.EID.getValue(), Protocol.SelectSet.NONE, 300),
			new SelectSet(SCOPUS_MARINER_FIELD.DAFFIL.getValue(), Protocol.SelectSet.NONE),
			new SelectSet(SCOPUS_MARINER_FIELD.XML.getValue(), Protocol.SelectSet.NONE, 300),
			new SelectSet(SCOPUS_MARINER_FIELD.CITEID.getValue(), Protocol.SelectSet.NONE),
			new SelectSet(SCOPUS_MARINER_FIELD.CITCOUNT.getValue(), Protocol.SelectSet.NONE),
			new SelectSet("weight", Protocol.SelectSet.NONE) };

	private static final int DIVISION_COUNT = 1000;

	private String searchRule;
	private String documentIDList;
	private String ip;
	private int port;
	private int sidx;
	private int eidx;
	private DOWNLOAD_FILE_FORMAT_TYPE downloadFormat = DOWNLOAD_FILE_FORMAT_TYPE.TEXT;
	private String downloadPath;
	private Set<String> selectedField;
	private String modelPath;
	private String downloadFileName;
	private boolean isSort;
	private String sort;
	private boolean isAdmin;
	private int limit = -1;

	NumberFormat nf = NumberFormat.getInstance();

	/**
	 * @param session
	 *            다운로드시 경과정보를 보관하는 session
	 * @param ip
	 *            검색엔진 ip
	 * @param port
	 *            검색엔진 포트
	 * @param sidx
	 *            시작 인덱스
	 * @param eidx
	 *            마지막 인덱스
	 * @param searchRule
	 *            {@link Query} 객체에 대한 Json 문자열
	 * @param downloadFormat
	 *            Export를 위한 포맷정보
	 * @param downloadPath
	 *            실제 파일이 작성되는 디렉토리 경로
	 * @param modelpath
	 *            Excel파일로 Export를 수행할때 템플릿 파일이 저장되어 있는 디렉토리 경로
	 * @param sessionID
	 *            사용자의 세션ID
	 */
	public DownloadSearchUtil(String ip, int port, int sidx, int eidx, String searchRule, String downloadPath,
			String modelpath, DOWNLOAD_FILE_FORMAT_TYPE downloadFormat) {
		this(ip, port, sidx, eidx, searchRule, downloadPath, modelpath, downloadFormat, new HashSet<String>(), "", true, false);
	}

	/**
	 * @param session
	 *            다운로드시 경과정보를 보관하는 session
	 * @param ip
	 *            검색엔진 ip
	 * @param port
	 *            검색엔진 포트
	 * @param sidx
	 *            시작 인덱스
	 * @param eidx
	 *            마지막 인덱스
	 * @param searchRule
	 *            {@link Query} 객체에 대한 Json 문자열
	 * @param downloadPath
	 *            실제 파일이 작성되는 디렉토리 경로
	 * @param modelpath
	 *            Excel파일로 Export를 수행할때 템플릿 파일이 저장되어 있는 디렉토리 경로
	 * @param sessionID
	 *            사용자의 세션ID
	 * @param downloadFormat
	 *            Export를 위한 포맷정보
	 * @param selectedField
	 *            사용자가 선택한 필드 정보셋
	 */
	public DownloadSearchUtil(String ip, int port, int sidx, int eidx, String searchRule, String downloadPath,
			String modelpath, DOWNLOAD_FILE_FORMAT_TYPE downloadFormat, Set<String> selectedField, String sort, boolean isSort,
			boolean isAdmin) {
		this.ip = ip;
		this.port = port;
		this.sidx = sidx;
		this.eidx = eidx;
		this.searchRule = searchRule;
		this.downloadPath = downloadPath;
		this.modelPath = modelpath;
		this.selectedField = selectedField;
		this.downloadFormat = downloadFormat;
		this.sort = sort;
		this.isSort = isSort;
		this.isAdmin = isAdmin;
	}
	
	public DownloadSearchUtil(String ip, int port, int sidx, int eidx, String searchRule, String downloadPath,
			String modelpath, DOWNLOAD_FILE_FORMAT_TYPE downloadFormat, Set<String> selectedField, String sort, boolean isSort,
			boolean isAdmin, int limit) {
		this.ip = ip;
		this.port = port;
		this.sidx = sidx;
		this.eidx = eidx;
		this.searchRule = searchRule;
		this.downloadPath = downloadPath;
		this.modelPath = modelpath;
		this.selectedField = selectedField;
		this.downloadFormat = downloadFormat;
		this.sort = sort;
		this.isSort = isSort;
		this.isAdmin = isAdmin;
		this.limit = limit;
	}

	/**
	 * @param session
	 *            다운로드시 경과정보를 보관하는 session
	 * @param ip
	 *            검색엔진 ip
	 * @param port
	 *            검색엔진 포트
	 * @param documentIDList
	 *            문서 ID 정보들
	 * @param downloadPath
	 *            실제 파일이 작성되는 디렉토리 경로
	 * @param modelpath
	 *            Excel파일로 Export를 수행할때 템플릿 파일이 저장되어 있는 디렉토리 경로
	 * @param downloadFormat
	 *            Export를 위한 포맷정보
	 */
	public DownloadSearchUtil(String ip, int port, String documentIDList, String downloadPath, String modelPath,
			DOWNLOAD_FILE_FORMAT_TYPE downloadFormat, boolean isAdmin) {
		this.ip = ip;
		this.port = port;
		this.isAdmin = isAdmin;
		this.sidx = 1;
		this.eidx = documentIDList.trim().split("(\\s)").length - 1;
		this.documentIDList = documentIDList;
		this.downloadPath = downloadPath;
		this.modelPath = modelPath;
		this.downloadFormat = downloadFormat;
	}
	/**
	 * 
	 * Export를 수행 처리 한다.
	 * 
	 * @author 이관재
	 * @throws Exception
	 * @date 2014. 5. 13.
	 */
	public boolean execute() throws Exception {
		try {
			while(DescriptionCodeManager.getInstance().isRunning==true){
				logger.info("Part2 : 정보 로딩이 완료되기위해 대기중입니다.");
				Thread.sleep(1000 * 2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean isSuccess = false;
		String info = "";
		ExportDocumentSax exportDocument = createExportDocument();
		int totalSearchCnt = 0;
		try {
			CommandSearchRequest cmd = createCommandSearchRequest();
			Query query = getDownloadQuery();

			Result result = null;

			if (sidx > 0) {
				sidx = sidx - 1;
				eidx = eidx - 1;
			}

			int totalCnt = eidx - sidx + 1;

			if (sidx > 0) {
				// sidx = sidx - 1;
			}

			boolean isContinues = true;
			// 세션 중복 방지
			int progress = 0;
			char[] brokerPagingInfo = null;
			int 보정값 = 0;
			while (true) {
				int last = eidx;
				if ((eidx - sidx) > DIVISION_COUNT - 1) {
					last = sidx + DIVISION_COUNT - 1;
				} else {
					isContinues = false;
					last = eidx;
				}

				if (progress == 0) {
					if (sidx > 0) {
						sidx = sidx - 1;
					}
				}
				progress += DIVISION_COUNT;

				if (isContinues) {
//					logger.info("download documents {}/{}", nf.format(progress), nf.format(totalCnt));
				} else {
					logger.info("download for ready");
				}
				
				if(limit > -1){
					if(last > limit){
						last = limit-1;
						isContinues = false;
						보정값 +=1;
					}
				}
//				logger.info("limit {} / last {} " , limit-1, last);
				query.setResult(sidx, last);
				query.setValue("searchType", "DOCUMENT-DOWNLOAD-BULK");
				query.setBrokerPagingInfo(brokerPagingInfo);

				ResultSet rs = getResultSet(cmd, query);
//				logger.info("rs {} " , rs);
				if(rs==null) break;
				brokerPagingInfo = writeFileInfo(rs, exportDocument);
				if (result == null) {
					result = rs.getResult(0);
					totalSearchCnt = result.getTotalSize();
					eidx = totalSearchCnt;
					logger.debug("total search count {}", totalSearchCnt);
					totalCnt = totalSearchCnt;
					
					if(totalCnt < limit){
						limit = totalCnt;
					}
					
					if (totalSearchCnt <= last) {
						break;
					} else {
						// eidx = totalSearchCnt;
					}
				}
				if (isContinues) {
					logger.info("download documents  {}", nf.format(progress) + "/" + nf.format(limit==-1?totalCnt:limit));
				} else if (isContinues) {
					logger.info("download documents  {}", nf.format(last) + "/" + nf.format(limit==-1?totalCnt:limit));
				}

				sidx = last + 1;

				if (!isContinues) {
					break;
				} else {
					exportDocument.flushRow(progress);
				}
			}
			logger.info("download Complete!!!!");
			logger.info("download Count Info : {}", limit==-1?totalCnt:limit);
			logger.info("download RESULT COUNT Info : {}", limit==-1?totalCnt:limit);
			
			//아래 프로그램에서만 사용하는 코드... 웹 배포시에는 제거되어야 한다.
			new CnProgramDao().insertHistory(InetAddress.getLocalHost().getHostAddress(), searchRule, totalSearchCnt, "SCOPUS-DOWNLOAD-PROGRAM");
			isSuccess = true;
			return isSuccess;
		} catch (Exception e) {
			logger.info("Total Search Count : {}", totalSearchCnt);
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			if (exportDocument != null) {
				exportDocument.close();
			}
		}
	}

	private char[] writeFileInfo(ResultSet rs, ExportDocumentSax exportDocument) throws Exception {
		if (rs == null) {
			throw new Exception("Export시 오류가 발생되었습니다. Result Set 객체가 존재하지 않습니다.");
		}

		Result[] rArr = rs.getResultList();
		char[] brokerPagingInfo = null;
		for (int k = 0; rArr != null && k < rArr.length; k++) {
			Result r = rArr[k];

			if (brokerPagingInfo == null) {
				brokerPagingInfo = r.getBrokerPagingInfo();
			}

			// int errorCode = r.getErrorCode();
			if (r.getRealSize() != 0) {
				Map<String, String> xmlMap = new LinkedHashMap<String, String>();
				Map<String, String[]> xmlCitEIDListMap = new LinkedHashMap<String, String[]>();
				Map<String, String> xmlCitCountMap = new LinkedHashMap<String, String>();
				Map<String, String[]> xmlDAffilMap = new LinkedHashMap<String, String[]>();
				for (int i = 0; i < r.getRealSize(); i++) {
					String eid = new String(r.getResult(i, 0));
					eid = eid.trim();
					String daffil = new String(r.getResult(i, 1));
					String[] daffilArr = new String[0];
					if (daffil.length() > 0) {
						daffilArr = daffil.split("(\r\n|\r|\n)");
					}

					String xml = new String(r.getResult(i, 2));
					xml = xml.trim();
					if (xml.length() < 1) {
						logger.error("NONE EXIST XML DOCUMENT ID INFO : " + eid);
						continue;
					}
					xml = xml.replaceAll("(\r\n|\r|\n)", "");
					// BufferedWriter writer = new BufferedWriter(new
					// OutputStreamWriter(new
					// FileOutputStream("C:\\test.xml")));
					// writer.write(xml);
					// writer.flush();
					// writer.close();

					String citEidList = new String(r.getResult(i, 3));
					citEidList = citEidList.trim();
					String[] citEidArr = new String[0];
					if (citEidList.length() > 0) {
						citEidArr = citEidList.split("(\\s)");
					}
					String citCount = new String(r.getResult(i, 4));
					citCount = citCount.trim();
					// logger.debug("XML : {}", xml);
					xmlMap.put(eid, xml);
					xmlCitEIDListMap.put(eid, citEidArr);
					xmlCitCountMap.put(eid, citCount);
					xmlDAffilMap.put(eid, daffilArr);
				}
				exportDocument.exportData(xmlMap, xmlCitEIDListMap, xmlCitCountMap, xmlDAffilMap);
			} else {
				logger.error("검색된 결과 정보가 존재하지 않습니다.");
				break;
			}
		}

		return brokerPagingInfo;
	}

	/*
	 * 특정 경로내의 파일들이 48시간이 경과된 파일이면 지운다.
	 */
	private void clearFiles(String dirPath) {
		// 파일을 삭제하는 코드
		if (true)
			return;
		File path = new File(dirPath);
		File[] files = path.listFiles(new CustomFileFilter(-2));
		for (File f : files) {
			try {
				logger.info("delete file : " + f.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			f.delete();
		}
	}

	/**
	 * 현재 날짜로 부터 입력된 날짜 수 이전의 파일들만 필터링한다.O
	 * 
	 * @author neon
	 * @date 2013. 7. 1.
	 * @Version 1.0
	 */
	private static class CustomFileFilter implements FileFilter {
		Calendar c = new GregorianCalendar();

		/**
		 * @param date
		 *            이전 날짜 수.
		 */
		protected CustomFileFilter(int date) {
			c.add(Calendar.DATE, date);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File pathname) {
			return c.getTime().after(new Date(pathname.lastModified()));
		}

	}

	/**
	 * 
	 * Export와 관련된 인스턴스를 생성한다.
	 * 
	 * @author 이관재
	 * @return
	 * @date 2014. 5. 13.
	 */
	private ExportDocumentSax createExportDocument() {
		String id = "bulk";
		String fileName = id + "-"
				+ UtilDate.getTimestampFormat(new Timestamp(System.currentTimeMillis()), "yyyyMMdd-HHmmss-SSSSS-")
				+ UUID.randomUUID();
		downloadFileName = "";
		ExportDocumentSax export = null;
		switch (downloadFormat) {
		case CLUSETER:
			downloadFileName = fileName + ".cluster.txt";
			downloadFileName = ""; /*외부에서 입력받은 파일명을 그대로 쓴다.*/
			export = new ExportCluster(downloadPath);
//			export = new ExportCluster(downloadPath+ downloadFileName);
			break;
		case TAB_DELIMITED:
			downloadFileName = fileName + ".txt";
			downloadFileName = ""; /*외부에서 입력받은 파일명을 그대로 쓴다.*/
			export = new ExportTabDelimited(downloadPath + downloadFileName, selectedField);
			break;
		case EXCEL:
			downloadFileName = fileName + ".xlsx";
			downloadFileName = ""; /*외부에서 입력받은 파일명을 그대로 쓴다.*/
			export = new ExportExcelJXL(modelPath + "ExportBasicFormat.xlsx", downloadPath + downloadFileName, selectedField,
					isAdmin);
			break;
		case TEXT:
		default:
			downloadFileName = fileName + ".mini.txt";
			downloadFileName = ""; /*외부에서 입력받은 파일명을 그대로 쓴다.*/
			export = new ExportText(downloadPath + downloadFileName, selectedField);
			break;
		}
		clearFiles(this.downloadPath);
		return export;
	}

	/**
	 * 
	 * 
	 * 다운로드에 필요한 쿼리 객체르 생성한다.
	 * 
	 * 
	 * @author 이관재
	 * @date 2014. 5. 14.
	 * @return
	 * @throws Exception
	 */
	private Query getDownloadQuery() throws Exception {
		Query query = null;
		if (this.searchRule != null) {
			QueryConverterWoS qc = new QueryConverterWoS(this.searchRule);
			query = new Query();
			if (qc.getWhereSet().length > 0) {
				query.setWhere(qc.getWhereSet());
			}

			if (qc.getFilterSet().length > 0) {
				query.setFilter(qc.getFilterSet());
			}

			if (isSort) {
				OrderBySet[] orderBySet = SearchQueryHelper.getOrderBySet(sort);
				query.setOrderby(orderBySet);
			}

		} else {
			query = new Query();
			query.setWhere(getWhereSet());
		}
		query.setSelect(DOWNLOAD_SELECT_SET);
		// query.setOrderby(new OrderBySet[] {
		// new OrderBySet(true, SCOPUS_MARINER_FIELD.PUBYEAR.getSortField(),
		// Protocol.OrderBySet.OP_NONE),
		// new OrderBySet(true, "weight", Protocol.OrderBySet.OP_NONE) });
		query.setFrom("SCOPUS_2020");
		query.setBrokerPagingInfo("");
		query.setBrokerPrevious(false);
		query.setLoggable(true);
		query.setIgnoreBrokerTimeout(false);
		query.setValue("searchType", "export-result");
		query.setSearchOption(Protocol.SearchOption.CACHE | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.BANNED | Protocol.SearchOption.PHRASEEXACT);
		query.setThesaurusOption(Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM);
//		query.setSearchOption(Protocol.SearchOption.PHRASEEXACT | Protocol.SearchOption.CACHE);
		query.setGroupBy(new GroupBySet[0]);
//		logger.debug("{}", query.toString());
		logger.debug("search request range : " + sidx + ", " + eidx);
		return query;
	}

	private ResultSet getResultSet(CommandSearchRequest cmd, Query query) throws Exception {
		QuerySet querySet = new QuerySet(1);
		querySet.addQuery(query);
		try {
			int resultCode = cmd.request(querySet);
			if (resultCode >= 0) {
				return cmd.getResultSet();
			}
			logger.error("mariner errorCode : {}", resultCode);
			printErrorMessage(cmd);
			return null;
		} catch (IRException e) {
			printErrorMessage(cmd);
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private void printErrorMessage(CommandSearchRequest cmd) {
		ErrorMessage message = cmd.getException();
		if (message != null) {
			logger.error("Mariner ERROR MESSAGE : {}", message.getErrorMessage());
		}
	}

	/**
	 * 
	 * 마리너 검색엔진에 요청하는 객체를 생성한다.
	 * 
	 * @author 이관재
	 * @date 2014. 5. 13.
	 * @return
	 */
	private CommandSearchRequest createCommandSearchRequest() {
		SearchQueryHelper.setCommandSearchRequestProps(ip, port);
		return new CommandSearchRequest(ip, port);
	}

	private WhereSet[] getWhereSet() {
		return new WhereSet[] { new WhereSet(SCOPUS_MARINER_FIELD.EID.getIndexField(), Protocol.WhereSet.OP_HASANY,
				this.documentIDList, 150) };
	}
}
