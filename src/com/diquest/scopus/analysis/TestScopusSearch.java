/**
 * 
 */
package com.diquest.scopus.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.ExecutorIBSSearch;

/**
 * IBS 엑셀 파일을 읽어서 상위 1% 저널의 정보를 취합한다.< br>
 * 이 클래스를 실행하기 전에 @see ExecutorIBSSearch.java 를 우선 구동해야 한다.
 * @author coreawin
 * @date 2014. 6. 17.
 * @Version 1.0
 */
public class TestScopusSearch {
	static Logger logger = LoggerFactory.getLogger("com.diquest.scopus.analysis.TestScopusSearch");

	/**
	 * 2012년 상위 1% 저널의 issn 번호
	 */
	static Set<String> journalISSNSet = new HashSet<String>();
	static Set<String> journalIDSet = new HashSet<String>();
	static Map<String, String> journalIDAscjMap = Collections.emptyMap();
	static {
		try {
			SrcIDReader sidr = new SrcIDReader("/com/diquest/scopus/analysis/top1JournalIdList.txt");
			journalIDSet = sidr.readId();
			sidr = new SrcIDReader("/com/diquest/scopus/analysis/topJournalASJCInfo.txt");
			journalIDAscjMap = sidr.readSrcFirstASJC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String[] titles = new String[] { "", "84880800169", "84880733249", "84867765569", "79960515455", "79952613531", "67749106310",
			"61649128006", "", "34248545460", "", };

	static BufferedReader br = null;

	private LinkedList<LinkedList<String>> init() {
		LinkedList<LinkedList<String>> gdatalist = new LinkedList<LinkedList<String>>();
		try {

			br = new BufferedReader(new FileReader(new File("f:\\Documents\\Private\\2015\\IBS\\eid.txt")));
			String line = null;
			LinkedList<String> datalist = new LinkedList<String>();
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("==")) {
					gdatalist.add(new LinkedList<String>(datalist));
					datalist = new LinkedList<String>();
					continue;
				}
				datalist.add(line.trim());
			}
			if (datalist.size() > 0) {
				gdatalist.add(new LinkedList<String>(datalist));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return new LinkedList<LinkedList<String>> ();
		return gdatalist;
	}

	public LinkedList<String> searchTitle() {
		return searchTitle(init());
	}

	public LinkedList<String> searchTitle(LinkedList<LinkedList<String>> gdatalist) {
		LinkedList<String> datalist = new LinkedList<String>();
		// LinkedHashMap<String, DOC> datas = new LinkedHashMap<String, DOC>();
		String adminIP = ExecutorIBSSearch.marinerIP; // Admin 서버 IP
		int adminPORT = ExecutorIBSSearch.marinerPort; // Admin 서버 PORT
		String collectionName = ExecutorIBSSearch.marinerCollection; // 컬렉션명

		// 전송하기 위한 Query를 설정합니다.

		Query query = new Query();
		query.setSearch(true); // 검색 여부 설정
		query.setResultCutOffSize(2);
		query.setFaultless(true);
		query.setPrintQuery(true);
		query.setIgnoreBrokerTimeout(true);
		SelectSet[] selectSet = new SelectSet[] { new SelectSet("EID", (byte) (Protocol.SelectSet.NONE), 300),
				new SelectSet("AU_ID", (byte) (Protocol.SelectSet.NONE), 300), new SelectSet("CIT_COUNT", (byte) (Protocol.SelectSet.NONE), 300),
				new SelectSet("ISSN", (byte) (Protocol.SelectSet.NONE), 300), new SelectSet("SRCTITLE", Protocol.SelectSet.NONE, 300),
				new SelectSet("TITLE", Protocol.SelectSet.NONE, 300), new SelectSet("AUTHOR_NAME", Protocol.SelectSet.NONE, 300),
				new SelectSet("CR_NAME", Protocol.SelectSet.NONE, 300), new SelectSet("CR_EMAIL", Protocol.SelectSet.NONE, 300),
				new SelectSet("SORTYEAR", Protocol.SelectSet.NONE, 300), new SelectSet("SRCID", Protocol.SelectSet.NONE, 300),
				new SelectSet("ASJC", Protocol.SelectSet.NONE, 300), new SelectSet("DOCTYPE", Protocol.SelectSet.NONE, 300), new SelectSet("XML", Protocol.SelectSet.NONE, 300), };
		query.setSelect(selectSet);
		query.setSearchOption(Protocol.SearchOption.PHRASEEXACT | Protocol.SearchOption.CACHE);
		query.setFrom(collectionName);
		query.setResult(0, 10);

		for (LinkedList<String> dl : gdatalist) {
			LinkedList<DOC> datas = new LinkedList<DOC>();
			for (String title : dl) {
				// logger.info("searching... " + title);
				boolean checkT = false;
				query.setWhere(null);
				title = title.replaceAll("-", " ");
				// logger.info(title);
				WhereSet[] whereSet = new WhereSet[] { new WhereSet("IDX_TITLE_E", Protocol.WhereSet.OP_HASALL, title, 150),
						new WhereSet(Protocol.WhereSet.OP_OR), new WhereSet("IDX_EID", Protocol.WhereSet.OP_HASALL, title, 150), };
				query.setWhere(whereSet);
				QuerySet querySet = new QuerySet(1);
				querySet.addQuery(query);
				Result[] resultlist = null;
				CommandSearchRequest command = new CommandSearchRequest(adminIP, adminPORT);
				int returnCode;
				try {
					returnCode = command.request(querySet);
					if (returnCode >= 0) {
						ResultSet results = command.getResultSet();
						resultlist = results.getResultList();
					} else {
						resultlist = new Result[1];
						resultlist[0] = new Result();
					}
					Result result = null;
					for (int k = 0; resultlist != null && k < resultlist.length; k++) {
						result = resultlist[k];
						// 검색 결과 출력
						if (result.getRealSize() != 0) {
							if (result.getRealSize() > 1) {
								logger.info(result.getRealSize() + " ========== mariner search title WARN : " + title);
								checkT = true;
							}
							DOC d = null;
							boolean checkMatch = false;
							for (int i = 0; i < result.getRealSize(); i++) {
								DOC doc = new DOC();
								int columnIdx = 0;
								doc.eid = new String(result.getResult(i, columnIdx++));
								doc.auid = new String(result.getResult(i, columnIdx++));
								doc.cit_count = new String(result.getResult(i, columnIdx++));
								doc.issn = new String(result.getResult(i, columnIdx++)).replaceAll("-", "");
								doc.srctitle = new String(result.getResult(i, columnIdx++));
								String ti = new String(result.getResult(i, columnIdx++));
								doc.authorname = new String(result.getResult(i, columnIdx++)).replaceAll("\n", "; ").replaceAll("\t", " ");
								doc.crname = new String(result.getResult(i, columnIdx++));
								
//								logger.info("eid : " + doc.eid +"\t" + doc.authorname);
								
								doc.cremail = new String(result.getResult(i, columnIdx++));
								doc.sortyear = new String(result.getResult(i, columnIdx++));
								doc.srcid = new String(result.getResult(i, columnIdx++)).trim();
								doc.asjc = new String(result.getResult(i, columnIdx++)).trim().replaceAll("\n", " ");
								doc.doctype = new String(result.getResult(i, columnIdx++)).trim();
								String xml = new String(result.getResult(i, columnIdx++)).trim();
								if(doc.crname==null || "".equals(doc.crname)){
									try{
									String emailInfo = DomParser.parseCorrespond(xml);
									if(emailInfo!=null){
										String[] _einfo = emailInfo.split("\\|");
										doc.crname = _einfo[0];
										doc.cremail = _einfo[1];
//										logger.info("== 교신저자 ==>> " + doc.crname +", " + doc.cremail);
									}
									}catch(Exception e){
										System.err.println(e.getMessage());
										logger.error(xml);
									}
								}
								doc.title = ti;
								doc.auid_cnt = String.valueOf(doc.auid.split("\n").length);
								doc.isTopJ = journalIDSet.contains(doc.srcid) ? "TRUE" : "FALSE";
								doc.srcFirstAsjc = journalIDAscjMap.get(doc.srcid) != null ? journalIDAscjMap.get(doc.srcid) : "";
								String bigAsjc = doc.srcFirstAsjc;
								if (bigAsjc.length() > 2) {
									bigAsjc = bigAsjc.substring(0, 2);
								}

								if (bigAsjc.length() > 0) {
									String[] asjcCode = doc.asjc.split(" ");
									for (String asjc : asjcCode) {
										if (asjc.startsWith(bigAsjc)) {
											doc.jornalAjsc += asjc + " ";
										}
									}
								}
								doc.jornalAjsc = doc.jornalAjsc.trim();

								// logger.info("asjc : " + doc.asjc);
								// logger.info("doctype : " +
								// doc.doctype);
								// doc.isTopJ =
								// journalISSNSet.contains(doc.issn) ? "TRUE" :
								// "FALSE";
								if (result.getRealSize() > 1) {
									if (ti.replaceAll("[,\\.\\-]", " ").equals(title.replaceAll("[,\\.\\-]", " "))) {
										// logger.info("FIND title
										// equals");
										try {
											// logger.info("PUT E \t" +
											// title);
											HCPCheck.checkHCP(doc);
											datas.add(doc);
											// datas.put(doc.eid, doc);
											checkMatch = true;
											d = null;
											break;
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
									d = doc;
								} else {
									try {
										// logger.info("PUT \t" + title);
										HCPCheck.checkHCP(doc);
										HCPCheck.checkASJC(doc);

										datas.add(doc);
										// datas.put(doc.eid, doc);
										break;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							if (result.getRealSize() > 1 && checkMatch == false) {
								// logger.info("PUT M\t" + title);
								if (d != null) {
									d.title = "M_ " + d.title;
								}
								datas.add(d != null ? d : new DOC());
								// datas.put("Multi Titles " +
								// System.nanoTime(),
								// d!=null?d:new DOC());
							}
						} else {
							// logger.info("PUT N\t" + title);
							datas.add(new DOC());
							// datas.put("No Titles " + System.nanoTime(), new
							// DOC());ㅁ
						}
					}
				} catch (IRException e) {
					e.printStackTrace();
				}
			}

			// logger.info("TITLE\tEID\tTC\tHCP\tHCP
			// %\t저자수\tissn\t상위저널\t저널명");
			// logger.info("EID\t저자순위\t저자들\t교신저자이름\t교신저자이메일\tTC\tissn\tPY\t저널1%\t저널명\tHCP%\t저자수");
			logger.info("JCR IF Top 1%\tASJC\t상위1%ASJC\teid\tissn\tcorr-author");
			for (DOC d : datas) {

				// DOC d = datas.get(k);
				// logger.info(d.title + "\t" + d.eid + "\t" +
				// d.cit_count + "\t" + d.isHCP + "\t" + d.ranking + "\t"
				// + d.auid_cnt + "\t" + d.issn + "\t" + d.isTopJ + "\t" +
				// d.srctitle);
				String data = d.asjc + " \t" + d.eid + " \t" + d.issn + " \t" + d.crname + " \t" + d.cit_count + " \t" + d.srctitle + " \t"
						+ d.srcFirstAsjc + " \t" + d.jornalAjsc + " \t" + d.isTopJ + " \t" + d.hcpFirstAsjc + " \t" + d.hcpSubAsjc + " \t"
						+ d.isFirstHCP + " \t" + ((d.hcpFirstAsjc.trim().length() > 0 || d.hcpSubAsjc.trim().length() > 0) ? "TRUE" : "FALSE");
				datalist.add(data);
				// logger.info(data);
				// logger.info(d.eid + "\t \t" + d./*authorname + "\t" +
				// d.crname + "\t" + d.cremail + "\t" + d.cit_count
				// + "\t" + d.issn + "\t" + d.sortyear + "\t" + d.isTopJ + "\t"
				// + d.srctitle + "\t" + d.ranking + "\t"
				// + d.auid_cnt + "\t" + d.title);*/
			}
			// logger.info(datas.size());
		}
		
		
		return datalist;
	}

	/**
	 * @author coreawin
	 * @date 2014. 6. 17.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TestScopusSearch searcher = new TestScopusSearch();
		ExcelUtil u = new ExcelUtil();

		String path = "h:\\d\\coreawin\\Documents\\Project\\2018\\KISTI\\10.IBS\\180607(5월)\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\180710_6월 입사자 1% 여부\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\180809(7월)\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\180911(8월)\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181011-9월\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181108-10월\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181203-11월\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181221-12월\\result\\";
		path = "C:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\10.IBS\\20181221-12월\\result\\";
		path = "f:\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\1-4월\\result\\";
		path = "d:\\data\\ibs\\1-4월\\result\\";
		path = "f:\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\6월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\9월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2019.KISTI\\10.IBS\\11월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\12월\\result\\";
		
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\2020년도 4월까지 신규임용자 CV(논문 리스트)\\4월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\5-6월\\result\\";
		
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\7-8월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2020\\11월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\1-5월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\5-6월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\7-8월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2021\\9-10월\\result\\";
		path = "f:\\Users\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\21.KISTI\\10.IBS\\2022\\6월\\result\\";
		File dir = new File(path);
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xlsx")) {
					return true;
				}
				return false;
			}
		});
		searchTOP1Journal(searcher, u, files);

//		dir = new File("E:\\프로젝트\\2017\\KISTI\\ibs\\분석\\20170530\\본원 및 캠퍼스 연구단");
//		files = dir.listFiles(new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String name) {
//				if (name.endsWith(".xlsx")) {
//					return true;
//				}
//				return false;
//			}
//		});
//		searchTOP1Journal(searcher, u, files);

		// dir = new File("E:\\IBS_검색결과\\외부연구단\\");
		// files = dir.listFiles(new FilenameFilter() {
		//
		// @Override
		// public boolean accept(File dir, String name) {
		// if (name.endsWith(".xlsx")) {
		// return true;
		// }
		// return false;
		// }
		// });
		// searchTOP1Journal(searcher, u, files);

		// new TestScopusSearch().searchTitle();
	}

	private static void searchTOP1Journal(TestScopusSearch searcher, ExcelUtil u, File[] files) throws IOException {
		for (File f : files) {
			Map<Integer, LinkedList<String>> mapData = u.readExcel(f);
			Set<Integer> sets = mapData.keySet();
			for (Integer i : sets) {
				LinkedList<LinkedList<String>> param = new LinkedList<LinkedList<String>>();
				param.add(mapData.get(i));
//				logger.info("=====================>>>>>>>>>>>>>>> " + mapData.get(i));
				LinkedList<String> list = searcher.searchTitle(param);
				mapData.put(i, list);
			}
//			logger.info(mapData);
			u.writeExcel(f, mapData);
		}
	}

	/**
	 * top1projasc : 상위 1%에 포함된 문서라면, 해당 분류의 쓰레숄드를 기준으로 문헌이 다른 분류로 등록되어 있다면, 해당
	 * 분류로도 쓰레숄드로 HCP를 비교한다. 해당 쓰레숄드 값이 다른 분류의 상위 1% 쓰레숄드보다 크다면 이곳에 등록
	 * 2015-06-15
	 * 
	 * @author pc
	 * @date 2015. 6. 15.
	 */
	public static class DOC {
		public String eid = "", auid = " ", cit_count = "0", issn = " ", srctitle = " ", auid_cnt = " ", title = " ", isHCP = " ", ranking = " ",
				isTopJ = " ", authorname = " ", crname = " ", cremail = " ", sortyear = " ", srcid = " ", asjc = " ", hcpasjc = " ", doctype = " ",
				srcFirstAsjc = " ", jornalAjsc = " ", hcpEtcAsjc = " ", hcpFirstAsjc = " ", isFirstHCP = " ", hcpSubAsjc = " ", 입사여부="";

	}
}
