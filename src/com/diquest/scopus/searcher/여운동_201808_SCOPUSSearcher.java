package com.diquest.scopus.searcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import com.diquest.scopus.searchrule.QueryConverterWoS_OLD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;

/**
 * 컴퓨터 사이언스 (17**) 분야의 논문.<br>
 * 1. 세부분류코드 <br>
 * 2. 제목 <br>
 * 3. 초록 <br>
 * 4. 저자 키워드  <br>
 * 5. 색인자 키워드  <br>
 * 6. 저널명  <br>
 * 7. 년도  <br>
 *
 * 
 * @author 정승한
 * @date 2018. 8. 5.
 */
public class 여운동_201808_SCOPUSSearcher {
	SearchService searchService = new SearchService();

	// 수집범위: computer science (17**) 분야의 논문
	// 수집기간: 2016년 부터 2018년 현재까지
	// 수집칼럼: (1) 세부분류코드 (2) 제목 (3) 초록 (4) 저자 키워드 (5) 색인자 키워드 (6) 저널명 (7) 년도

	public static SelectSet[] getSelectSet() {
		SelectSet[] selectSet = new SelectSet[] {
				new SelectSet(SCOPUS_MARINER_FIELD.EID.name(), (byte) Protocol.SelectSet.NONE),
				new SelectSet("ASJC", (byte) Protocol.SelectSet.NONE), new SelectSet("TITLE", (byte) Protocol.SelectSet.NONE),
				new SelectSet("ABS", (byte) Protocol.SelectSet.NONE), new SelectSet("AUTHKEY", (byte) Protocol.SelectSet.NONE),
				new SelectSet("INDEXTERMS", (byte) Protocol.SelectSet.NONE),
				new SelectSet("SRCTITLE", (byte) Protocol.SelectSet.NONE),
				new SelectSet("SORTYEAR", (byte) Protocol.SelectSet.NONE), };
		return selectSet;
	}

	public void search(String searchRule) throws IRException {
		System.out.println("searchRule : " + searchRule);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:/data/여운동_201808.tsv")), "UTF-8"));
			int start = 0;
			int paging = 1000;
			int end = paging;
			int progress = 0;
			while (true) {
				QueryConverterWoS_OLD qc = null;
				try {
					qc = new QueryConverterWoS_OLD(searchRule);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				WhereSet[] ws = qc.getWhereSet();
				// for (WhereSet _w : ws) {
				// System.out.println(_w);
				// }

				QuerySet qs = new QuerySet(1);
				Query query = new Query();
				query.setSelect(getSelectSet());
				query.setWhere(ws);
				query.setFrom("SCOPUS_2016");

				query.setSearch(true); // 검색 여부 설정
				query.setFaultless(true);
				query.setPrintQuery(true);
				query.setIgnoreBrokerTimeout(true);
				query.setResult(start == 0 ? start : start + 1, end);
				// System.out.println("paging " + (start == 0 ? start : start +
				// 1) + "/" + end);
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
				for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
					Result rInfo = rArr[resIndex];
					totalSize = rInfo.getTotalSize();
					int realSize = rInfo.getRealSize();
					// System.out.println(totalSize + ":" + realSize);
					// if (totalSize > realSize) {

					// continue;
					// }

					StringBuffer buf = new StringBuffer();

					for (int rr = 0; rr < realSize; rr++) {
						progress++;
						int sIdx = 0;
						buf.setLength(0);
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++)).replaceAll("\n", " | ")));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++)).replaceAll("\n", " | ")));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\t");
						buf.append(refine(new String(rInfo.getResult(rr, sIdx++))));
						buf.append("\r\n");
						bw.write(buf.toString());
						// if (progress % 100 == 0) {
						// System.out.println(progress + " / " + totalSize);
						// }
					}
					System.out.println(progress + " / " + totalSize);
				}
				bw.flush();
				if (progress == totalSize)
					break;

			}
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

	public static void main(String[] args) {
		여운동_201808_SCOPUSSearcher r = new 여운동_201808_SCOPUSSearcher();
		try {
			r.search("ASJC=(1700 OR 1701 OR 1702 OR 1703 OR 1704 OR 1705 OR 1706 OR 1707 OR 1708 OR 1709 OR 1710 OR 1711 OR 1712) and PY=(2016 OR 2017 OR 2018)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String refine(String src) {
		if (src == null)
			return "";
		return src.replaceAll("(\\s{1,}|\\n{1,})", " ");
	}

}
