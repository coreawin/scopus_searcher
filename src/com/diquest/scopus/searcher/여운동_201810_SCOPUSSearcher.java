package com.diquest.scopus.searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kr.co.diquest.search.rule.QueryConverterWoS;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.analysis.ExcelUtil;
import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;
import com.google.gson.Gson;

/**
 * 대분류가 총 16개+알파개(별도중분류가 몇개 더 있음)인데 2개 중분류에 대해 샘플송부드립니다. <br>
 * 다운로드는 제이슨포맷으로 해 주시면 됩니다.<br>
 * 
 * 중분류별 다운로드는 아래 기준을 따라 주시기 바랍니다.<br>
 * 1. 2003년이후에 대해서만 다운로드 합니다. 단, 검색결과가 1,000건이 넘지 않을 경우 2003년 이전도 다운로드 합니다.<br>
 * 2. 다운로든 최대 1,000건까지 합니다. 1,000건이하는 모두 다운합니다.<br>
 * 3. 필드는 아래와 같습니다.<br>
 * 
 * -스커퍼스ID<br>
 * -제목<br>
 * -초록<br>
 * -저자키워드<br>
 * -색인자키워드 (여러종류의 키워드 모두다 해 주세요)<br>
 * -스커퍼스분류<br>
 * -출판연도<br>
 * -저자소속기관<br>
 * 
 * (지난번에 말씀드린 검색식에 포함된 단어 제거는 하지 말아 주세요.)<br>
 * 
 * 파일은 중분류별로 하나씩 만들어 주시면 되고, 파일 제목은 "대분류명_중분류명"으로 해주세요.<br>
 * 
 * 가운데 점과 "/"이 파일제목으로 안되면 그냥 빼주시면 됩니다.<br>
 * 
 * [2018.11.05] 추가 <br>
 * 1. 한국인 저자가 포함된 논문은 제외해 주세요. <br>
 * -나중에 한국인 저자가 포함된 논문은 따로 받아서 분류할 것이라서 한국인 저자가 포함되면 안됩니다. <br>
 * 2. 제목 및 초록의 토큰수(공백으로 나눈 글자수)의 합이 40개 이상인 것만 다운받아 주세요. 글자수가 많아야 학습이 잘 되기
 * 때문입니다. <br>
 * 3. 다운로드시 연도는 디폴트(2001년부터)로 하면 됩니다. <br>
 * 4. 만약 앞의 조건식으로 2001년부터 논문수가 1000개가 안될시 시작기간을 2000년부터 해 주시면 됩니다. 또 안되면 그
 * 이전..또..또 <br>
 * 
 * @author 정승한
 * @date 2018. 8. 5.
 */
public class 여운동_201810_SCOPUSSearcher {
	SearchService searchService = new SearchService();

	public static SelectSet[] getSelectSet() {
		SelectSet[] selectSet = new SelectSet[] {
				new SelectSet(SCOPUS_MARINER_FIELD.EID.name(), (byte) Protocol.SelectSet.NONE),
				new SelectSet("TITLE", (byte) Protocol.SelectSet.NONE), new SelectSet("ABS", (byte) Protocol.SelectSet.NONE),
				new SelectSet("AUTHKEY", (byte) Protocol.SelectSet.NONE),
				new SelectSet("INDEXTERMS", (byte) Protocol.SelectSet.NONE),
				new SelectSet("ASJC", (byte) Protocol.SelectSet.NONE),
				new SelectSet("SORTYEAR", (byte) Protocol.SelectSet.NONE),
				new SelectSet("AFFIL", (byte) Protocol.SelectSet.NONE), };
		return selectSet;
	}

	public void search(String outputPath, String searchRule, String addSearchRule) throws IRException {
		System.out.println(outputPath + " searchRule : " + searchRule + "\t" + addSearchRule);
		BufferedWriter bw = null;
		int downloadCnt = 1200;
		try {
			int start = 0;
			int paging = 1000;
			int end = paging;
			int progress = 0;

			LinkedList<SCOPUSBean_201810> resultsOut = new LinkedList<SCOPUSBean_201810>();
			Set<String> year = new HashSet<String>();

			int rotationCnt = 0;
			int pyear = 2000;
			while (true) {
				QueryConverterWoS qc = null;
				try {
					System.out.println("searching.... " + searchRule + addSearchRule);
					qc = new QueryConverterWoS(searchRule + addSearchRule);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				WhereSet[] ws = qc.getWhereSet();
				// for (WhereSet _w : ws) {
				// System.out.println(_w);
				// }

				OrderBySet[] os = new OrderBySet[] { new OrderBySet(true, "WEIGHT") };

				QuerySet qs = new QuerySet(1);
				Query query = new Query();
				query.setSelect(getSelectSet());
				query.setWhere(ws);
				query.setOrderby(os);
				query.setFrom("SCOPUS_2016");

				query.setSearch(true); // 검색 여부 설정
				query.setFaultless(true);
				query.setPrintQuery(false);
				query.setIgnoreBrokerTimeout(false);
				query.setResult(start, end - 1);
				System.out.println("paging " + start + "/" + (end - 1));

				start += paging;
				end = end + paging;

				query.setSearchOption(Protocol.SearchOption.PHRASEEXACT | Protocol.SearchOption.CACHE);
				qs.addQuery(query);
				// ResultSet rs = searchService.requestSearch("203.250.207.72",
				// 5555,
				// qs);
				String ip = "203.250.207.72";
				int port = 5555;
				ResultSet rs = null;
				SearchQueryHelper.setCommandSearchRequestProps(ip, port);
				CommandSearchRequest cmd = new CommandSearchRequest(ip, port);
				// logger.info("{}:{}", ip, port);
				try {
					int returnCode = cmd.request(qs);
					// System.out.println("returnCode " + returnCode);
					if (returnCode >= 0) {
						rs = cmd.getResultSet();
					} else {
						break;
					}
				} catch (IRException e) {
					throw e;
				}

				Result[] rArr = rs.getResultList();

				int totalSize = 0;
				Set<String> s = new HashSet<String>();
				for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
					Result rInfo = rArr[resIndex];
					totalSize = rInfo.getTotalSize();
					int realSize = rInfo.getRealSize();
					for (int rr = 0; rr < realSize; rr++) {
						progress++;
						int sIdx = 0;
						SCOPUSBean_201810 bean = new SCOPUSBean_201810();
						bean.EID = refine(new String(rInfo.getResult(rr, sIdx++)));
						bean.TITLE = refine(new String(rInfo.getResult(rr, sIdx++)));
						bean.ABS = refine(new String(rInfo.getResult(rr, sIdx++)));

						String tab = bean.TITLE + " " + bean.ABS;
						tab = tab.replaceAll("[!@#$%^&\\*\\(\\)\\-_\\+=\\|\\.,\\?\\\"';:]", "").replaceAll("\\s{1,}", " ")
								.trim();
						String[] abs = tab.split(" ");
						s.clear();
						// for (String _abs : abs) {
						// s.add(_abs);
						// }

						if (abs.length < 40) {
							continue;
						}

						// if (s.size() > 40) {
						// continue;
						// }
						bean.AUTHKEY = refine2(new String(rInfo.getResult(rr, sIdx++)));
						bean.INDEXKEY = refine2(new String(rInfo.getResult(rr, sIdx++)));
						bean.ASJC = refine2(new String(rInfo.getResult(rr, sIdx++)));
						bean.YEAR = refine(new String(rInfo.getResult(rr, sIdx++)));
						bean.AFFILIATION = refine2(new String(rInfo.getResult(rr, sIdx++)));

						if (resultsOut.size() >= downloadCnt)
							break;
						resultsOut.add(bean);
						year.add(bean.YEAR);
					}
					System.err.println(resultsOut.size() + " <=/ " + progress + " / " + totalSize);
					if (resultsOut.size() >= downloadCnt)
						break;
				}
				if (resultsOut.size() >= downloadCnt) {
					break;
				} else {
					if (totalSize < end) {
						addSearchRule = " (PY=(%s) NOT CU=(KOR))";
						addSearchRule = String.format(addSearchRule, pyear--);
						if (pyear == 1995) {
							break;
						}
					}
				}
				rotationCnt++;
			}
			System.out.println(year);

			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format(outputPath,
					resultsOut.size() + ""))), "UTF-8"));
			bw.write(new Gson().toJson(resultsOut));
			bw.flush();
			System.out.println("================= result ");
			System.out.println("================= ");
			System.out.println("output " + outputPath);
			System.out.println("resultsOut " + resultsOut.size());

		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 엑셀로된 검색식을 읽는다.<br>
	 * 대분류명<br>
	 * 중분류명 검색식<br>
	 * 중분류명 검색식<br>
	 * 대분류명<br>
	 * 중분류명 검색식 <br>
	 * 
	 * @author 정승한
	 * @date 2018. 10. 3.
	 */
	public LinkedHashMap<String, String> readSearchRule(File readExcelFile) {
		LinkedHashMap<String, String> searchRuleDatas = new LinkedHashMap<String, String>();
		ExcelUtil eu = new ExcelUtil();
		LinkedHashMap<Integer, LinkedList<LinkedList<LinkedList<String>>>> excelMap = null;
		try {
			excelMap = eu.generalReadExcel(readExcelFile);
			Set<Integer> sheetNum = excelMap.keySet();
			for (int i : sheetNum) {
				if (i > 0)
					break;
				System.out.println("sheet number" + i);
				LinkedList<LinkedList<LinkedList<String>>> sheetData = excelMap.get(i);
				String lClassName = "";
				for (LinkedList<LinkedList<String>> rowDatas : sheetData) {
					String mClassName = "";
					String searchRule = "";
					for (LinkedList<String> columns : rowDatas) {
						// row
						String first = columns.get(0).replaceAll("[\r\n]", " ").replaceAll("\\s{1,}", "_");
						String second = null;
						try {
							second = columns.get(1);
						} catch (Exception e) {
							second = null;
						}

						if (second != null) {
							if ("".equals(second.trim()) || "[null 아닌 공백]".equalsIgnoreCase(second.trim())) {
								second = null;
							}
						}
						// System.out.println("==>" + first +";" + second);

						if (second == null) {
							lClassName = first;
							// System.out.println("==>" + lClassName +";" +
							// mClassName);
						} else {
							mClassName = first;
							searchRule = second;
							searchRuleDatas.put(lClassName + ";" + mClassName, searchRule);
							// System.out.println("==>" + lClassName +";" +
							// mClassName);
						}

						// int columnSize = columns.size();
						// System.out.println(columnSize +"/" + lClassName);
						// switch (columnSize) {
						// case 1:
						// // 대분류명
						// lClassName = columns.get(0).replaceAll("/",
						// "_").replaceAll("\\s{1,}", " ");
						// break;
						// case 2:
						// // 중분류명 + 검색 식.
						// mClassName = columns.get(0);
						// searchRule = columns.get(1);
						// searchRuleDatas.put(lClassName + ";" + mClassName,
						// searchRule);
						// break;
						// default:
						// break;
						// }
					}
				}
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String s : searchRuleDatas.keySet()) {
			System.out.println("target datas " + s + "\t" + searchRuleDatas.get(s));
		}
		// System.exit(-1);
		return searchRuleDatas;
	}

	/**
	 * 파일에 EID 목록을 읽어서 다운로드 한다..<br>
	 * 
	 * @author 정승한
	 * @date 2018. 11. 21.
	 */
	public void readEIDSearchRule(String outputPath, File readEidExcelFile) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		List<String> searchRuleList = new ArrayList<String>();
		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(readEidExcelFile), "UTF-8"));
			String line = null;
			Set<String> eidSet = new HashSet<String>();
			StringBuffer buf = new StringBuffer();
			int totalCnt = 0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if ("".equals(line)) {
					continue;
				}
				totalCnt ++;
				eidSet.add(line);
				
				if (eidSet.size() > 399) {
					buf.setLength(0);
//					System.out.println(eidSet.size());
					buf.append("EID=(");
					for (String eid : eidSet) {
						buf.append(eid);
						buf.append(" ");
					}
					buf.append(")");
					searchRuleList.add(buf.toString());
					eidSet.clear();
//					break;
				}else{
//					System.out.println(searchRuleList.size());
				}
			}

			if (eidSet.size() > 0) {
				buf.setLength(0);
				buf.append("EID=(");
				for (String eid : eidSet) {
					buf.append(eid);
					buf.append(" ");
				}
				buf.append(")");
				searchRuleList.add(buf.toString());
				eidSet.clear();
			}
//			if(true){
//				for (String searchRule : searchRuleList) {
//					System.out.println(searchRule);
//				}
//				System.exit(-1);
//			}

			try {
				int progress = 0;
				LinkedList<SCOPUSBean_201810> resultsOut = new LinkedList<SCOPUSBean_201810>();
				//bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format(outputPath,
				//		"eidSet.json"))), "UTF-8"));
				int fileCnt = 1;
				for (String searchRule : searchRuleList) {
					QueryConverterWoS qc = null;
					try {
						System.out.println("searching.... " + searchRule);
						qc = new QueryConverterWoS(searchRule);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					WhereSet[] ws = qc.getWhereSet();
//					for (WhereSet _w : ws) {
//						System.out.println(_w);
//					}
					OrderBySet[] os = null;
					QuerySet qs = new QuerySet(1);
					Query query = new Query();
					query.setSelect(getSelectSet());
					query.setWhere(ws);
					query.setOrderby(os);
					query.setFrom("SCOPUS_2016");

					query.setSearch(true); // 검색 여부 설정
					query.setFaultless(true);
					query.setPrintQuery(false);
					query.setIgnoreBrokerTimeout(false);
					query.setResult(0, 500);
					query.setSearchOption(Protocol.SearchOption.CACHE);
					qs.addQuery(query);
					String ip = "203.250.207.72";
					int port = 5555;
					ResultSet rs = null;
					SearchQueryHelper.setCommandSearchRequestProps(ip, port);
					CommandSearchRequest cmd = new CommandSearchRequest(ip, port);
					try {
						int returnCode = cmd.request(qs);
						// System.out.println("returnCode " + returnCode);
						if (returnCode >= 0) {
							rs = cmd.getResultSet();
						} else {
							break;
						}
					} catch (IRException e) {
						throw e;
					}

					Result[] rArr = rs.getResultList();

//					Set<String> s = new HashSet<String>();
					for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
						Result rInfo = rArr[resIndex];
						int realSize = rInfo.getRealSize();
						for (int rr = 0; rr < realSize; rr++) {
							progress++;
							int sIdx = 0;
							SCOPUSBean_201810 bean = new SCOPUSBean_201810();
							bean.EID = refine(new String(rInfo.getResult(rr, sIdx++)));
							bean.TITLE = refine(new String(rInfo.getResult(rr, sIdx++)));
							bean.ABS = refine(new String(rInfo.getResult(rr, sIdx++)));

//							String tab = bean.TITLE + " " + bean.ABS;
//							tab = tab.replaceAll("[!@#$%^&\\*\\(\\)\\-_\\+=\\|\\.,\\?\\\"';:]", "").replaceAll("\\s{1,}", " ")
//									.trim();
//							String[] abs = tab.split(" ");
//							s.clear();
							// for (String _abs : abs) {
							// s.add(_abs);
							// }

//							if (abs.length < 40) {
//								continue;
//							}

							// if (s.size() > 40) {
							// continue;
							// }
							bean.AUTHKEY = refine2(new String(rInfo.getResult(rr, sIdx++)));
							bean.INDEXKEY = refine2(new String(rInfo.getResult(rr, sIdx++)));
							bean.ASJC = refine2(new String(rInfo.getResult(rr, sIdx++)));
							bean.YEAR = refine(new String(rInfo.getResult(rr, sIdx++)));
							bean.AFFILIATION = refine2(new String(rInfo.getResult(rr, sIdx++)));
							resultsOut.add(bean);
						}
						if(progress % 1000 ==0){
							System.err.println("download " + resultsOut.size() + " <=/ " + progress + " / " + totalCnt +" / " + realSize);
						}
					}
					
					if(resultsOut.size() % 1000 ==0){
						System.err.println(resultsOut.size() + " <=/ " + progress + " / " + totalCnt);
					}
					
					if(resultsOut.size() % 100000 ==0){
						bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format(outputPath,
								(fileCnt++)+ ".eidSet.json"))), "UTF-8"));
						bw.write(new Gson().toJson(resultsOut));
						bw.flush();
						bw.close();
						
						
						resultsOut.clear();
						System.err.println("write complete " + resultsOut.size() + " <=/ " + progress + " / " + totalCnt);
					}
				}
				if(resultsOut.size() > 0){
					
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format(outputPath,
							+ (fileCnt++)+ ".eidSet.json"))), "UTF-8"));
					bw.write(new Gson().toJson(resultsOut));
					bw.flush();
					System.out.println("write complete " );
				}
				
				System.out.println("================= result ");
				System.out.println("================= ");
				System.out.println("output " + outputPath);
				System.out.println("resultsOut " + resultsOut.size());

			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static void main(String[] args) {
		여운동_201810_SCOPUSSearcher r = new 여운동_201810_SCOPUSSearcher();
		// File readExcelFile = new
		// File("c:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\20181002.여운동\\searchRule.xlsx");
		File readExcelFile = new File("f:\\coreawin\\Documents\\OneDrive - hansung.ac.kr\\2018.KISTI\\20181002.여운동\\181109_중분류검색식 - 복사본.xlsx");
		String outputPath = "d:\\data\\201810\\yeo\\20181112\\";
		try {
			LinkedHashMap<String, String> searchRuleMap = r.readSearchRule(readExcelFile);
			Set<String> keySet = searchRuleMap.keySet();
			for (String fileName : keySet) {
				String searchRule = searchRuleMap.get(fileName);
				fileName = fileName.replaceAll("/", "_");
				String path = outputPath + fileName + ";%s.json";
				// System.out.println(path);
				// System.out.println(searchRule +" PY=(2001-) NOT CU=(KOR)");
				if ("".equals(searchRule.trim()))
					continue;
				System.out.println("==> searchRule : " + searchRule);
				r.search(path, searchRule, " (PY=(2001-) NOT CU=(KOR))");
				// r.search(path, searchRule);
				// break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*File readTextFile = new File("c:\\Users\\정승한\\OneDrive - hansung.ac.kr\\2018.KISTI\\20181002.여운동\\Verification_Paper_Year_SourceID_ASJC.txt");
		String outputPath = "d:\\data\\201810\\yeo\\20181121\\%s";
		try {
			r.readEIDSearchRule(outputPath, readTextFile);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	public String refine(String src) {
		if (src == null)
			return "";
		return src.replaceAll("(\\s{1,}|\\n{1,})", " ");
	}

	public LinkedHashSet<String> refine2(String src) {
		return refine2(src, "\n");
	}

	public LinkedHashSet<String> refine2(String src, String delim) {
		if (src == null)
			return null;
		String[] v = src.split(delim);
		LinkedHashSet<String> r = new LinkedHashSet<String>();
		for (String d : v) {
			d = d.trim();
			if ("".equals(d))
				continue;
			r.add(d);
		}
		return r;

	}

}
