/**
 * 
 */
package com.diquest.scopus.searchrule;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.GroupResult;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;

/**
 * 마리너 서버에 요청을 전담하는 서비스 클래스.
 * 
 * @author neon
 * @date   2014. 5. 2.
 * @Version 1.0
 */
public class SearchService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 마리너에 검색 질의를 요청한다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @param ip
	 *            마리너 검색 서버 IP 주소
	 * @param port
	 *            마리너 검색 서버 Port 번호
	 * @param querySet
	 *            완전히 구성된 QuerySet 객체
	 * @return Mariner3 ResultSet 객체.
	 * @throws IRException
	 */
	public ResultSet requestSearch(String ip, int port, QuerySet querySet) throws IRException {
		return requestSearch(ip, port, querySet, false);
	}

	/**
	 * 마리너에 검색 질의를 요청한다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @param ip
	 *            마리너 검색 서버 IP 주소
	 * @param port
	 *            마리너 검색 서버 Port 번호
	 * @param querySet
	 *            완전히 구성된 QuerySet 객체
	 * @param printQuery
	 *            입력된 쿼리식에 대한 콘솔 출력 여부.
	 * @return Mariner3 ResultSet 객체.
	 * @throws IRException
	 */
	public ResultSet requestSearch(String ip, int port, QuerySet querySet, boolean printQuery) throws IRException {
		SearchQueryHelper.setCommandSearchRequestProps(ip, port);
		CommandSearchRequest cmd = new CommandSearchRequest(ip, port);
//		logger.info("{}:{}", ip, port);
		try {
			if (printQuery) {
				int idx = 1;
				for (Query q : querySet.getQueryList()) {
					logger.debug("index " + idx + ", " + q.toString());
				}
			}
			int returnCode = cmd.request(querySet);
			if (returnCode >= 0) {
				return cmd.getResultSet();
			}
			logger.error("mariner errorCode : {}", returnCode);
			System.exit(01);
		} catch (IRException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return null;
	}
	
	/**
	 * 마리너에 검색 질의를 요청한다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @param ip
	 *            마리너 검색 서버 IP 주소
	 * @param port
	 *            마리너 검색 서버 Port 번호
	 * @param querySet
	 *            완전히 구성된 QuerySet 객체
	 * @param printQuery
	 *            입력된 쿼리식에 대한 콘솔 출력 여부.
	 * @return Mariner3 ResultSet 객체.
	 * @throws IRException
	 */
	public ResultSet requestSearchCommand(String ip, int port, QuerySet querySet) throws IRException {
		SearchQueryHelper.setCommandSearchRequestProps(ip, port);
		CommandSearchRequest cmd = new CommandSearchRequest(ip, port);
		try {
			int returnCode = cmd.request(querySet);
			if (returnCode >= 0) {
				return cmd.getResultSet();
			}
			logger.error("mariner errorCode : {}", returnCode);
			throw new IRException("Mariner ErrorCode : "+ returnCode);
		} catch (IRException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 검색 결과를 콘솔 화면에 출력한다.
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @param resultSet
	 * @param query
	 */
	public void printResult(ResultSet resultSet, Query query, boolean printGroup) {
		Result[] resultlist = resultSet.getResultList();

		SelectSet[] getselectSet = query.getSelectFields();

		for (int k = 0; resultlist != null && k < resultlist.length; k++) {
			Result result = resultlist[k];

			if (printGroup) {
				GroupResult[] gr = result.getGroupResults();
				for (GroupResult gResult : gr) {
					char[][] names = gResult.getIds();
					for (char[] namess : names) {
						String s = String.valueOf(names);
						StringBuffer sb = new StringBuffer();
						for (char c : namess) {
							sb.append(c);
						}
						System.out.println("group names " + sb.toString());
					}
					int[] values = gResult.getIntValues();
					for (int a : values) {
						System.out.println("int values " + a);
					}
				}
			}

			int errorCode = resultSet.getErrorCode();
			System.out.println("<!--" + errorCode + "-->");
			// 검색 결과 출력
			System.out.println("검색 결과 " + result.getRealSize());
			if (result.getRealSize() != 0) {
				for (int i = 0; i < result.getRealSize(); i++) {
					StringBuffer sb = new StringBuffer();
					if (result.getRecommend() != null) {
						sb.append("이 검색어에 대한 추천 검색어 :");
						sb.append(new String(result.getRecommend())).append("<BR>");
					}
					if (result.getRedirect() != null) {
						sb.append("이 검색어에 대한 바로가기 :");
						sb.append(new String(result.getRedirect())).append("<BR>");
					}
					for (k = 0; k < result.getNumField(); k++) {
						sb.append(new String(getselectSet[k].getField())).append(" : ");
						sb.append(new String(result.getResult(i, k))).append("<BR>");
					}
					System.out.println(sb.toString() + "<BR>");
				}
			} else {
				System.out.println("일치하는 내용을 찾을 수 없습니다.");
			}
		}
	}
}
