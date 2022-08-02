/**
 * 
 */
package com.diquest.scopus.searchrule;

import java.util.Calendar;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;

import kr.co.diquest.search.EJianaUtil;
import kr.co.diquest.search.rule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import kr.co.diquest.util.nlp.EStemming;
import kr.co.tqk.web.util.UtilString;

/**
 * WoS 검색 사이트에서 사용되는 검색식에 가급적 대응되는 문법 룰<br>
 * 
 * @author coreawin
 * @date 2014. 6. 16.
 * @Version 1.0
 * @see <a href=
 *      'http://images.webofknowledge.com/WOKRS5132R4.2/help/ko_KR/WOS/hp_advanced_search.html'>
 *      http://images.webofknowledge.com/WOKRS5132R4.2/help/ko_KR/WOS/hp_advan c
 *      e d . s e a r c h . h t m l < / a >
 */
public class QueryConverterWoS {
	Logger logger = LoggerFactory.getLogger(getClass());

	private final String DELIM_FIELD_RULE = "@DQ@";
	private final String ERROR_FIELD_RULE = "@DQ_ERR@";
	private static final String NOT_EXPRESSION = "@OP_NOT@";

	WhereSet[] whereSets = null;
	FilterSet[] filterSets = null;

	// Set<String> wildCardSearchTerms;
	// Set<String> searchTerms;

	Map<String, Set<String>> wildCardSearchTerms;
	Map<String, Set<String>> searchTerms;
	Set<String> higtlighteField;
	EStemming stemming;
	EJianaUtil util;
	/**
	 * 원본 검색식
	 */
	private String query = null;
	/**
	 * 정제된 검색식 (불필요한 띄어쓰기등과 같은 것을 정제한 식)
	 */
	private String queryRefine = null;

	public QueryConverterWoS(String query) throws QueryConverterException {
//		this.stemming = new EStemming();
		this.util = new EJianaUtil();
		this.query = query;
		this.queryRefine = refine(query);
		this.wildCardSearchTerms = new LinkedHashMap<String, Set<String>>();
		this.searchTerms = new LinkedHashMap<String, Set<String>>();
		this.higtlighteField = new HashSet<String>();
		// logger.debug("input query {} ", this.query);
		List<String> queryInfo = parseQuery();
		this.whereSets = makeWhereSetList(queryInfo);
		this.filterSets = makeFilterSets(queryInfo);
	}

	public Map<String, Set<String>> getSearchTerms() {
		return searchTerms;
	}

	/**
	 * 
	 * 필터 셋을 생성한다.
	 * 
	 * @author coreawin
	 * @date 2014. 6. 16.
	 * @param queryList
	 *            검색 쿼리 정보
	 * @return
	 */
	private FilterSet[] makeFilterSets(List<String> queryList) {
		List<FilterSet> result = new LinkedList<FilterSet>();
		if (queryList.size() == 0)
			return new FilterSet[0];
		boolean isNot = false;
		for (String e : queryList) {
			String[] values = e.split(DELIM_FIELD_RULE);
			if (values.length == 2) {
				String field = values[0];
				String value = values[1];
				SCOPUS_MARINER_FIELD efield = null;

				boolean isNotFlag = false;
				if (field.startsWith("(NOT")) {
					field = field.replaceAll("(^(\\(((!?)NOT)))", "").trim();
					isNotFlag = true;
				}

				field = field.replaceAll("\\(", "").trim();
				field = FieldConversion.conversionField(field);
				if (!"".equals(field)) {
					// logger.debug("field name {}", field);

					efield = SCOPUS_MARINER_FIELD.valueOf(field.toUpperCase().trim());
					if (!MARINER_FIELD._filterSet.contains(efield)) {
						continue;
					}
					if (isNotFlag) {
						value = NOT_EXPRESSION + value;
					}

					result.addAll(makeFilterSet(efield, value));
				}
			}
		}
		FilterSet[] r = new FilterSet[result.size()];
		int idx = 0;
		for (FilterSet f : result) {
			r[idx++] = f;
		}
		return r;
	}

	/**
	 * 
	 * 필터셋 리스트를 생성한다. 피인용수, 인용수
	 * 
	 * @author coreawin
	 * @date 2014. 6. 16.
	 * @param field
	 *            필드명
	 * @param value
	 *            검색 값
	 * @return 피인용수, 인용수에 대한 필터셋 정보
	 */
	private List<FilterSet> makeFilterSet(SCOPUS_MARINER_FIELD field, String value) {
		List<FilterSet> r = new LinkedList<FilterSet>();
		if (value == null)
			return r;
		value = value.replaceAll("\\(", "").replaceAll("\\)", "");
		switch (field) {
		case REFCOUNT:
		case CITCOUNT:
			// logger.debug("filter set {} / value ", field, value);
			byte filterOp = Protocol.FilterSet.OP_RANGE;
			if (value.trim().toUpperCase().startsWith(NOT_EXPRESSION)) {
				filterOp = Protocol.FilterSet.OP_NOT;
				value = value.trim().toUpperCase().substring(NOT_EXPRESSION.length());
			}
			if (value.contains("-")) {
				String[] values = value.split("-");
				if (values.length == 1) {
					values = new String[] { values[0], "" };
				}
				// logger.debug("{} / {}", values[0], values[1]);
				if ("".equals(values[0])) {
					values[0] = "0";
				}
				values[0] = values[0].toLowerCase().replaceAll("(and|or)", "").replaceAll("((?i)(^" + NOT_EXPRESSION + "))", "");
				if ("".equals(values[0])) {
					values[1] = "";
				}
				values[1] = values[1].toLowerCase().replaceAll("(and|or)", "");
				values[1] = values[1].replaceAll("(?i)(not$)", "");
				r.add(new FilterSet(filterOp, field.getFilterField(), new String[] { values[0].trim(), values[1].trim() }));
			}

			break;
		}
		return r;
	}

	/**
	 *
	 * 필터셋 정보만 생성되었을때 WhereSet을 가져온다
	 * 
	 * 검색 대상 필드 : ALL
	 * 
	 * @author 정승한
	 * @date 2015. 3. 27.
	 * @version 1.0
	 * @return
	 */
	public WhereSet[] getWhereSet() {
		if (this.filterSets.length > 0 && this.whereSets.length == 0) {
			if (this.whereSets.length == 0) {
				this.whereSets = new WhereSet[] { new WhereSet(SCOPUS_MARINER_FIELD.ALL.getIndexField(), Protocol.WhereSet.OP_HASALL, "Y", 150) };
			}
		}
		return this.whereSets;
	}

	/**
	 * 검색식을 정제합니다.
	 * 
	 * @author coreawin
	 * @date 2014. 6. 16.
	 */
	public String refine(String query) {
		query = query.replaceAll("(\\s{1,}-)", "-").replaceAll("(-\\s{1,})", "-").replaceAll("(\\s{1,})", " ").trim();
		return query;
	}

	/**
	 *
	 * 검색쿼리 파싱 처리
	 *
	 * @author 정승한
	 * @date 2015. 3. 27.
	 * @version 1.0
	 * @return
	 */
	private List<String> parseQuery() {
		Deque<String> fieldStack = new LinkedList<String>();
		Deque<String> contentsStack = new LinkedList<String>();
		// Map<String, String> queryList = new LinkedHashMap<String, String>();
		List<String> queryList = new LinkedList<String>();

		StringBuilder _buf = new StringBuilder();
		char[] charArr = queryRefine.toCharArray();
		for (char c : charArr) {
			switch (c) {
			case '=':
				String field = _buf.toString();
				_buf.setLength(0);
				if (fieldStack.size() > 0) {
					// logger.debug("query field : {} / {}", field,
					// contentsStack.toString());
					String fieldPop = fieldStack.pop();
					setNotFilterQuery(contentsStack, queryList, fieldPop);
					// queryList.add(fieldStack.pop() + DELIM_FIELD_RULE +
					// getStack(contentsStack));
				}
				fieldStack.add(field);
				contentsStack.clear();

				continue;
			case ' ':
				String v = _buf.toString();
				if (fieldStack.size() == 0 && v.trim().equalsIgnoreCase("not")) {
					queryList.add(" " + DELIM_FIELD_RULE + " NOT");
				} else if (fieldStack.size() == 0 && v.trim().toLowerCase().endsWith("not")) {
					queryList.add(" " + DELIM_FIELD_RULE + v.trim());
				} else {
					contentsStack.add(v);
				}
				_buf.setLength(0);
				break;
			case '"':

				break;
			case '(':
				break;
			case ')':

				break;
			default:
				break;
			}
			_buf.append(c);
		}
		if (_buf.length() > 0) {
			contentsStack.add(_buf.toString());
			_buf.setLength(0);
		}
		if (fieldStack.size() > 0) {
			String fieldPop = fieldStack.pop();
			setNotFilterQuery(contentsStack, queryList, fieldPop);
			// queryList.add(fieldStack.pop() + DELIM_FIELD_RULE +
			// getStack(contentsStack));
		}
		// System.out.println(queryList);
		// logger.debug("query info : {}", queryList);
		return queryList;
	}

	/**
	 * 
	 * 
	 * 검색식 앞을 체크하여 필터필드 앞쪽에 Not 연산자를 체크하여 Filter 필드에 NOT 필터 항목을 추가한다.
	 *
	 * @author 이관재
	 * @date 2017. 2. 14.
	 * @version 1.0
	 * @param contentsStack
	 * @param queryList
	 * @param fieldPop
	 */
	private void setNotFilterQuery(Deque<String> contentsStack, List<String> queryList, String fieldPop) {
		String beforeQuery = "";
		if (!queryList.isEmpty()) {
			beforeQuery = queryList.get(queryList.size() - 1);
		}
		// System.out.println("BEFORE QUERY : " + beforeQuery);
		boolean isNot = false;
		String refineField = fieldPop.trim().replaceAll("(?i)(\\()", "");
		String brace = beforeQuery.trim().replaceAll("(?i)(not$)", "");

		if (!beforeQuery.toLowerCase().endsWith("not")) {
			String braceR = brace.trim().replaceAll("([\\(]{1,})$", "").trim();
			if (braceR.toLowerCase().endsWith("not")) {
				brace = brace.replaceAll("(?i)(\\(not)", "(");
				isNot = true;
			} else {
				if (refineField.toUpperCase().startsWith("NOT")) {
					refineField = refineField.replaceAll("(?i)(^not)", "").trim();
					brace = brace.trim() + " NOT";
					isNot = true;
					fieldPop = fieldPop.replaceAll("(?i)(not)", "").trim();
				} else {
					queryList.add(fieldPop + DELIM_FIELD_RULE + getStack(contentsStack));
				}
			}
		} else {
			isNot = true;
		}
		if (isNot) {
			boolean isFilter = false;
			try {
				refineField = FieldConversion.conversionField(refineField.trim().toUpperCase());
				SCOPUS_MARINER_FIELD inputField = SCOPUS_MARINER_FIELD.valueOf(refineField);
				if (MARINER_FIELD._filterSet.contains(inputField)) {
					isFilter = true;
				}
			} catch (Exception e) {
				isFilter = false;
			}

			brace = brace.trim();
			if (isFilter) {
				String rBrace = brace.trim().replaceAll("(^([\\(]{1,})$)", "").trim();
				if (rBrace.length() < 1) {
					queryList.remove(queryList.size() - 1);
					queryList.add(brace + fieldPop + DELIM_FIELD_RULE + NOT_EXPRESSION + getStack(contentsStack));
				} else if (!brace.matches("(^([\\(]{1,})$)") && rBrace.length() > 0) {
					queryList.set(queryList.size() - 1, brace);
					queryList.add(fieldPop + DELIM_FIELD_RULE + NOT_EXPRESSION + getStack(contentsStack));
				} else {
					String braceR = brace.trim().replaceAll("([\\(]{1,})$", "").trim();
					if (braceR.toLowerCase().endsWith("not")) {
						queryList.set(queryList.size() - 1, brace.replaceAll("(?i)(not$)", ""));
						queryList.add(fieldPop + DELIM_FIELD_RULE + NOT_EXPRESSION + getStack(contentsStack));
					} else {
						queryList.add(fieldPop + DELIM_FIELD_RULE + getStack(contentsStack));
					}
				}
			} else {
				queryList.add(fieldPop + DELIM_FIELD_RULE + getStack(contentsStack));
			}
		}
	}

	private StringBuffer _tbuf = new StringBuffer();

	private String getStack(Deque<String> stack) {
		_tbuf.setLength(0);
		while (!stack.isEmpty()) {
			_tbuf.append(stack.pollFirst().trim());
			_tbuf.append(" ");
		}
		return _tbuf.toString().trim();
	}

	/**
	 * @author coreawin
	 * @date 2014. 6. 16.
	 * @param field
	 * @param isOpen
	 *            true이면 OpenBrace 갯수를 리턴<br>
	 *            false이면 CloseBrace 갯수를 리턴<br>
	 * @return
	 */
	private int countBrace(String field, boolean isOpen) {
		char[] cs = field.toCharArray();
		int cntBrace = 0;
		char prev = 0;
		for (char c : cs) {
			switch (c) {
			case '(':
				if (isOpen && prev != '\\')
					cntBrace += 1;
				break;
			case ')':
				if (!isOpen && prev != '\\')
					cntBrace += 1;
				break;
			default:
				break;
			}
			prev = c;
		}
		return cntBrace;
	}

	private void throwError(String msg, boolean b) throws QueryConverterException {
		if (!b) {
			throw new QueryConverterException("[ErrorCode-1002] 지원하지 않는 검색식 혹은 문법이 포함되어 있습니다. > " + msg);
		}
		throw new QueryConverterException("[ErrorCode-1001] 기존에 지원되던 검색식 혹은 문법이 포함되어 있습니다. > " + msg);
	}

	/**
	 * 다이퀘스트 검색 API를 만든다.
	 * 
	 * @author coreawin
	 * @date 2014. 6. 16.
	 * @param field
	 *            쿼리
	 * @param isField
	 *            입력된 쿼리가 필드 정보인가? true
	 * @return
	 * @throws QueryConverterException
	 */
	private List<WhereSet> makeWhereSet(String field, String query) throws QueryConverterException {
		_tbuf.setLength(0);
		List<WhereSet> result = new LinkedList<WhereSet>();
		char[] cs_field = field.toCharArray();
		// 필드라면.
		for (char c : cs_field) {
			switch (c) {
			case '(':
				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				break;
			case ')':
				throwError("", false);
				break;
			default:
				_tbuf.append(c);
				break;
			}
		}
		// logger.debug("field info {} = > {}", field, _tbuf.toString());
		field = _tbuf.toString();
		_tbuf.setLength(0);
		if ("EID".equalsIgnoreCase(field.trim()) && query.length() > 10) {
			// Query 객체를 검색 페이지에서 검색 서버로 전달하기 위해 serialize 를 하는 부분에서
			// WhereSet, FilterSet, SelectSet 등 배열로 저장되는 Query 내 객체들은
			// 각 객체 배열의 array size 를 우선 기록하고 그 개수만큼 이어서 serialize 하도록 되어있는데,
			// 소스를 확인하니 그 부분에서 array size 는 short type 으로 쓰도록 되어있습니다.
			// from 윤철민 팀장 2014-06-25
			// 보통 이런 쿼리는 HCP 및 RF에서 넘어오므로 이부분만 조치함 - 임시 조치임 (OR, AND 연산자가 들어와도
			// 무조건 OR검색)
			return makerForEID(field, query);
		}

		char[] cs_query = query.toCharArray();
		boolean isDoubleQuoter = false;
		boolean isHipen = false;
		// logger.debug("cs_query {}", query);
		char prev = 'x';
		StringBuffer _blnkBuf = new StringBuffer();
		for (char c : cs_query) {
			switch (c) {
			case '"':
				if (isDoubleQuoter) {
					// logger.debug("AAAAAAAAAAAA CLOSE DQ {}", _tbuf);
					result.addAll(maker(field, _tbuf.toString(), true));
					_tbuf.setLength(0);

					// 2016.10.25 add
					_blnkBuf.setLength(0);
					isDoubleQuoter = false;
				} else {
					isDoubleQuoter = true;
				}
				break;
			case '-':
				if (isHipen) {
					isHipen = true;
				}
				_tbuf.append(c);
				_blnkBuf.append(c);
				break;
			case ')':
				if (isDoubleQuoter || prev == '\\') {
					_tbuf.append("@BRACECLOSE@");
					_blnkBuf.append("@BRACECLOSE@");
				} else {
					_tbuf.append(c);
					_blnkBuf.append(c);
				}
				break;
			case '\\':
				break;
			case '(':
				if (isDoubleQuoter || prev == '\\') {
					_tbuf.append("@BRACEOPEN@");
					_blnkBuf.append("@BRACEOPEN@");
				} else {
					_tbuf.append(c);
					_blnkBuf.append(c);
				}
				break;
			case ' ':
				String _prev = _blnkBuf.toString().trim();

				// logger.debug("==> {} / {}", _tbuf, _prev);
				_blnkBuf.setLength(0);

				if (isDoubleQuoter) {
					_tbuf.append(c);
					// logger.debug("isDoubleQuoter : {} / {}", c, _tbuf);
				} else {
					if (_tbuf.length() > 0) {
						boolean isOp = false;
						if (_prev.contains("*") || "or".equalsIgnoreCase(_prev) || "and".equalsIgnoreCase(_prev) || "not".equalsIgnoreCase(_prev)) {
							int blankIdx = _tbuf.lastIndexOf(" ");
							if (blankIdx > -1) {
								_tbuf = _tbuf.delete(blankIdx, _tbuf.length());
								// logger.debug("A " + _tbuf.toString());
							}
							// if(_tbuf.toString().equalsIgnoreCase(_prev)){
							// }
							isOp = true;
						}

						// logger.debug("==> {} / {} ", _prev , _tbuf.toString()
						// + " : " + String.valueOf(isOp));
						if (_tbuf.indexOf("*") == -1 || _prev.contains("*")) {
							if (isOp) {
								// logger.debug("========= " + _tbuf +"\t" +
								// _prev);
								result.addAll(applyEjiana(field, new StringBuffer(_tbuf)));
								// result.addAll(maker(field, _tbuf.toString(),
								// false));
								if (!_tbuf.toString().equalsIgnoreCase(_prev)) {
									result.addAll(applyEjiana(field, new StringBuffer(_prev)));
									// result.addAll(maker(field, _prev,
									// false));
								}
								_tbuf.setLength(0);
							} else {
								_tbuf.append(c);
							}
						} else {
							if (isOp) {
								result.addAll(applyEjiana(field, new StringBuffer(_tbuf)));
								// result.addAll(maker(field, _tbuf.toString(),
								// false));
								if (!_tbuf.toString().equalsIgnoreCase(_prev)) {
									result.addAll(applyEjiana(field, new StringBuffer(_prev)));
									// result.addAll(maker(field, _prev,
									// false));
								}
							} else {
								result.addAll(applyEjiana(field, new StringBuffer(_tbuf)));
								// result.addAll(maker(field, _tbuf.toString(),
								// false));
							}
							_tbuf.setLength(0);
						}
					}
				}
				break;
			default:
				_tbuf.append(c);
				_blnkBuf.append(c);
				break;
			}
			prev = c;
		}
		// logger.debug("{} = _tbuf : {}", field, _tbuf);
		LinkedList<WhereSet> tmpResult = applyEjiana(field, new StringBuffer(_tbuf));
		// LinkedList<WhereSet> tmpResult = applyEjiana(field, new
		// StringBuffer(_blnkBuf));
		if (tmpResult.size() > 0) {
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
			result.addAll(tmpResult);
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}
		List<WhereSet> _result = new LinkedList<WhereSet>();

		int cnt = result.size();
		for (int idx = 0; idx < cnt; idx += 1) {
			WhereSet prevWs = null;
			if (idx > 0) {
				prevWs = result.get(idx - 1);
			}
			WhereSet currentWs = result.get(idx);
			WhereSet mo = makerOperator(prevWs, currentWs);
			if (mo != null) {
				_result.add(mo);
			}
			_result.add(currentWs);
		}

		return _result;
	}

	private LinkedList<WhereSet> applyEjiana(String field, StringBuffer src) throws QueryConverterException {
		LinkedList<WhereSet> result = new LinkedList<WhereSet>();
		LinkedList<WhereSet> tmpResult = new LinkedList<WhereSet>();
		boolean needBrace = false;
		if (src.toString().length() > 0) {
			String lq = src.toString().trim();
//			 logger.debug("query {}", lq);
			// src.setLength(0);
			if (lq.contains(" ")) {
				/** 2016.09.12 쿼리 개선 */
//				logger.debug("!개선 2016.10.20 쿼리 개선 영문형태소 분석기 넣기 {}", src.toString());
				/** 2018.04.17 영문형태소 분석기 제거 */
				logger.debug("!변경 2018.04.17 쿼리 개선 영문형태소 분석기 제거 {}", src.toString());
				String[] termSplitWhiteSpace = src.toString().trim().split(" ");
				LinkedHashSet<String> lSet = new LinkedHashSet<String>();
				StringBuilder buf = new StringBuilder(src);
				for (String term : termSplitWhiteSpace) {
					term = term.trim();
					// logger.debug("Ejiana term : {}", term);

					if ("AND".equalsIgnoreCase(term) || "OR".equalsIgnoreCase(term) || "NOT".equalsIgnoreCase(term)) {
						// [2016-10-24] 연산자 단어가 포함된 검색식이 들어왔을때 연산자가 적용될수 있도록 처리.
						// BY 이관재
						// logger.debug("FIELD : {}, TERM : {}", field, term);
						tmpResult.addAll(maker(field, term, false));
						continue;
					}

					if ("a".equalsIgnoreCase(term) || "the".equalsIgnoreCase(term) || "she".equalsIgnoreCase(term)) {
						// [2016-10-24] 연산자 단어가 포함된 검색식이 들어왔을때 연산자가 적용될수 있도록 처리.
						// BY 이관재
						// tmpResult.addAll(maker(field, term, true));
						continue;
					}

					if (this.query.indexOf("\"") != -1) {
						// 인접검색일때는 전치사 제거할 필요가 없음 없음.
						needBrace = true;
						tmpResult.addAll(maker(field, term, term.indexOf("-") == -1 ? false : true));
						continue;
					}

					if (term.indexOf("-") != -1 || term.indexOf("*") != -1) {
						// 인접검색일때는 전치사 제거할 필요가 없음 없음.
						needBrace = true;
						tmpResult.addAll(maker(field, term, term.indexOf("-") == -1 ? false : true));
						continue;
					} else {
						StringBuilder sb = new StringBuilder();
						char[] _ca = term.toCharArray();
						String prev = "";
						String post = "";
						for (char _c : _ca) {
							if (_c == '(' || _c == ')') {
								if (sb.length() > 0) {
									post += _c;
								} else {
									prev += _c;
								}
								continue;
							}
							sb.append(_c);
						}

						// if(sb.toString().trim())
						term = this.util.getStem(sb.toString());
						// if ("track".equalsIgnoreCase(term)) {
						// term = sb.toString().toLowerCase();
						// }
						if (!"".equals(term)) {
							needBrace = true;
							tmpResult.addAll(maker(field, prev + term + post, false));
						} else {
							int pl = prev.length();
							int pol = post.length();
							if (pl > pol) {
								prev = prev.substring(pol);
								post = "";
							} else if (pl < pol) {
								prev = "";
								post = post.substring(pl);
							}
							String t = prev + "" + post;
							if (!"".equals(t.trim())) {
								tmpResult.addAll(maker(field, prev + "" + post, false));
							}
						}
					}
				}
			} else {
				String _lq = this.util.getStem(lq);
//				 logger.debug("==> {} - {}" , _lq, lq);
				if ("".equals(_lq)) {
					if (lq.equalsIgnoreCase("and") | lq.equalsIgnoreCase("or") | lq.equalsIgnoreCase("not")) {
						tmpResult.addAll(maker(field, lq, false));
					} else {
						needBrace = true;
						tmpResult.addAll(maker(field, lq, true));
					}
				} else {
					if (lq.equalsIgnoreCase("(she)") | lq.equalsIgnoreCase("(a)") | lq.equalsIgnoreCase("(the)")) {
						needBrace = true;
						tmpResult.addAll(maker(field, lq, true));
					} else {
						tmpResult.addAll(maker(field, lq, false));
					}
				}
			}
		}
		if (tmpResult.size() > 0 && needBrace) {
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
			result.addAll(tmpResult);
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			return result;
		} else {
			return tmpResult;
		}
	}

	/**
	 *
	 * 검색 쿼리의 연산자를 생성한다.
	 * 
	 * @author 이관재
	 * @date 2015. 3. 27.
	 * @version 1.0
	 * @param prev
	 * @param curr
	 * @return
	 */
	private WhereSet makerOperator(WhereSet prev, WhereSet curr) {
		if (prev == null)
			return null;
		// logger.debug("prev {}, curr {}", prev, curr);
		if (prev.getOperation() == Protocol.WhereSet.OP_HASANYONE) {
			return new WhereSet(Protocol.WhereSet.OP_OR);
		} else if (prev.getOperation() == Protocol.WhereSet.OP_HASALLONE) {
			return new WhereSet(Protocol.WhereSet.OP_AND);
		}

		if (prev.getOperation() == Protocol.WhereSet.OP_BRACE_CLOSE && curr.getOperation() == Protocol.WhereSet.OP_BRACE_OPEN) {
			return new WhereSet(Protocol.WhereSet.OP_AND);
		}

		if ((prev.getOperation() == Protocol.WhereSet.OP_HASALL || prev.getOperation() == Protocol.WhereSet.OP_HASANY
				|| prev.getOperation() == Protocol.WhereSet.OP_BRACE_CLOSE)
				&& (curr.getOperation() == Protocol.WhereSet.OP_HASALL || curr.getOperation() == Protocol.WhereSet.OP_HASANY
						|| curr.getOperation() == Protocol.WhereSet.OP_BRACE_OPEN)) {
			// logger.debug("prev {}, curr {}, AND=================", prev,
			// curr);
			return new WhereSet(Protocol.WhereSet.OP_AND);
		}

		return null;
	}

	private List<WhereSet> makerForEID(String field, String query) throws QueryConverterException {
		List<WhereSet> result = new LinkedList<WhereSet>();
		char[] cs_query = query.toCharArray();
		boolean isDoubleQuoter = false;
		// System.out.println(query);
		_tbuf.setLength(0);
		for (char c : cs_query) {
			switch (c) {
			case '"':
			case ' ':
				_tbuf.append(c);
			case '(':
			case ')':
				break;
			default:
				_tbuf.append(c);
				break;
			}
		}
		// System.out.println(_tbuf);
		String terms = _tbuf.toString().replaceAll("(?i)(OR)", " ").replaceAll("\\s{1,}", " ").trim();

		/*
		 * makerForEID 를 거쳐서 생성된 필드임을 알려주는 일종의 FLAG 2015-07-03 식을 조합할 경우 문제가 된다.
		 * TODO 2015.07.20 맨 마지막에 and가 들어올경우 or로 변경되어야 한다.
		 * 
		 * @modify 2015.07.28 OR식으로 연결해야 될 때는 OP_HASANYONE 으로 FLAG넣어줌<BR> AND식으로
		 * 연결해야 될 때는 OP_HASALLONE 으로 FLAG 넣어줌.
		 */
		String op = "";
		if (terms.contains(" ")) {
			op = terms.substring(terms.lastIndexOf(" "), terms.length()).trim();
		}

		int opBraceCnt = countBrace(query, true);
		int cBraceCnt = countBrace(query, false);

		for (int i = 0; i < opBraceCnt; i++) {
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		}

		result.add(new WhereSet(SCOPUS_MARINER_FIELD.EID.getIndexField(), Protocol.WhereSet.OP_HASANY,
				terms.replaceAll("(?i)(AND)", "").replaceAll("\\(|\\)", ""), 150));
		// result.add(new WhereSet(Protocol.WhereSet.OP_HASANYONE));
		// }
		for (int i = 0; i < cBraceCnt; i++) {
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}

		// result.add(new WhereSet(SCOPUS_MARINER_FIELD.EID.getIndexField(),
		// Protocol.WhereSet.OP_HASANY, terms, 150));
		// result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));
		return result;
	}

	private void makeSearchTerm(String field, String term) {
		String upField = field.toUpperCase();
		String refineTerm = term.replaceAll("\\(", "").replaceAll("\\)", "").trim();
		refineTerm = refineTerm.replaceAll("(?i)@braceopen@", "(").replaceAll("(?i)@braceclose@", ")").trim();
		refineTerm = refineTerm.replaceAll("\\(", "").replaceAll("\\)", "").trim();
		if (refineTerm.equalsIgnoreCase("OR") || refineTerm.equalsIgnoreCase("AND")) {
			return;
		}
		try {

			SCOPUS_MARINER_FIELD f = SCOPUS_MARINER_FIELD.valueOf(upField);
			String fStr = f.name();
			switch (f) {
			case TITLE:
			case TK:
				fStr = SCOPUS_MARINER_FIELD.TITLE.name();
				higtlighteField.add(SCOPUS_MARINER_FIELD.TITLE.name());
				break;
			case ABS:
				fStr = SCOPUS_MARINER_FIELD.ABS.name();
				higtlighteField.add(SCOPUS_MARINER_FIELD.ABS.name());
				break;
			case TS:
			case ALL:
				higtlighteField.add(SCOPUS_MARINER_FIELD.TITLE.name());
				higtlighteField.add(SCOPUS_MARINER_FIELD.ABS.name());
				break;
			case AUTHKEY:
			case INDEXTERMS:
			default:
				return;
			}

			Set<String> hTerms = Collections.emptySet();
			Set<String> hwTerms = Collections.emptySet();
			if (searchTerms.containsKey(fStr)) {
				hTerms = searchTerms.get(fStr);
			} else {
				if (searchTerms.keySet().containsAll(higtlighteField)) {
					hTerms = searchTerms.get(SCOPUS_MARINER_FIELD.TITLE.name());
				} else {
					hTerms = new HashSet<String>();
					for (String hf : higtlighteField) {
						fStr = hf.toUpperCase();
						searchTerms.put(fStr, hTerms);
					}
				}
			}

			if (wildCardSearchTerms.containsKey(fStr)) {
				hwTerms = wildCardSearchTerms.get(fStr);
			} else {
				if (searchTerms.keySet().containsAll(higtlighteField)) {
					hwTerms = searchTerms.get(SCOPUS_MARINER_FIELD.TITLE.name());
				} else {
					hwTerms = new HashSet<String>();
					for (String hf : higtlighteField) {
						fStr = hf.toUpperCase();
						wildCardSearchTerms.put(fStr, hTerms);
					}
				}
			}

			if (refineTerm.contains("*")) {
				if (refineTerm.contains("-") || refineTerm.contains("_")) {
					String[] sArr = refineTerm.split("(-|_)");
					for (String s : sArr) {
						if (s.contains("*")) {
							hwTerms.add(s.replaceAll("[*]", "").toLowerCase());
						} else {
							hTerms.add(s.toLowerCase());
						}
					}
					hwTerms.add(refineTerm.toLowerCase());
				} else {
					hwTerms.add(refineTerm.replaceAll("[*]", "").toLowerCase());
				}
			} else {
				/**
				 * [2017-11-17] 영어 Stemming을 통해 원형이 포함된 단어에 대해서 Highlight할수 있도록
				 * 원형 단어를 포함.
				 * 
				 */
				String[] terms = refineTerm.split("\\s");
				for (String t : terms) {
					if (t.contains("-") || t.contains("_")) {
						String[] sArr = t.split("(-|_)");
						for (String s : sArr) {
							if (s.toLowerCase().endsWith("ing")) {
								String _term = stemming.analysis(term + " a");
								if (_term != null) {
									String[] arr = _term.split(" ");
									hTerms.add(arr[0]);
								}
							} else {
								String _term = stemming.analysis(refineTerm);
								if (_term != null) {
									hTerms.add(_term.toLowerCase());
								}
							}
							hTerms.add(s.toLowerCase());
						}
						hTerms.add(t.toLowerCase());
					} else {
						String _term = stemming.analysis(refineTerm + " a");
						if (_term != null) {
							String[] arr = _term.split(" ");
							hTerms.add(arr[0].trim());
							hTerms.add(refineTerm.trim());
						} else {
							hTerms.add(refineTerm.trim());
						}
						hTerms.add(t.toLowerCase());
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private List<WhereSet> maker(String field, String term, boolean isnear) throws QueryConverterException {
		// logger.info("maker : {} / {} ", field, term);
		term = term.trim();
		if ("".equals(term)) {
			throwError("검색어가 입력되지 않았습니다.", false);
		}
		LinkedList<WhereSet> result = new LinkedList<WhereSet>();
		field = FieldConversion.conversionField(field);

		try {
			SCOPUS_MARINER_FIELD inputField = SCOPUS_MARINER_FIELD.valueOf(field.toUpperCase().trim());

			if (MARINER_FIELD._filterSet.contains(inputField)) {
				// logger.debug("filter field : {} / {} ", field, term);
				if (term.contains("-")) {
					int o = countBrace(term, true);
					int c = countBrace(term, false);
					for (int i = 0; i < (c - o); i++) {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					}
					return result;
				}

				String refineTerm = term.replaceAll("\\(|\\)", "");

				if ("AND".equalsIgnoreCase(refineTerm.trim()) || "OR".equalsIgnoreCase(refineTerm.trim())) {
					return result;
				}
			}
		} catch (Exception e) {
			String notTerm = term.trim().replaceAll("\\(|\\)", "").toLowerCase();
			if (!notTerm.endsWith("not")) {
				throw new QueryConverterException(
						"[ErrorCode-1005] 지원하지 않은 검색필드[" + field.toUpperCase() + "]가 입력되었습니다.\n" + field.toUpperCase() + "=" + term);
			}
		}

		byte op = Protocol.WhereSet.OP_HASANY;

		int o = countBrace(term, true);
		int c = countBrace(term, false);

		// if(term.equalsIgnoreCase("or") || term.equalsIgnoreCase("and") ||
		// term.equalsIgnoreCase("not")){
		// o = 0;
		// c = 0;
		// }

		if (!term.equalsIgnoreCase("NOT")) {
			makeSearchTerm(field, term);
		}

		boolean isNot = false;

		/* 역슬래쉬가 붙어 있는 괄호는 검색 대상 괄호이다. */
		// term = term.replaceAll("\\\\\\(",
		// "@BRACEOPEN@").replaceAll("\\\\\\)", "@BRACECLOSE@").trim();
		term = term.replaceAll("\\(", "").replaceAll("\\)", "").trim();
		/*
		 * TI=(\"Total synthesis of (+)-demethoxycardinalin\")" 검색식에서 "가 있다면 (,
		 * )는 검색되게 한다.
		 */

		// logger.debug("term {} = {}", term, String.valueOf(o) + ":" +
		// String.valueOf(c));
		for (int idx = 0; idx < o; idx += 1) {
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		}

		if (term.equalsIgnoreCase("OR") && !isnear) {
			result.add(new WhereSet(Protocol.WhereSet.OP_OR));
		} else if (term.equalsIgnoreCase("AND") && !isnear) {
			result.add(new WhereSet(Protocol.WhereSet.OP_AND));
		} else if (term.equalsIgnoreCase("NOT") && !isnear) {
			result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
			isNot = true;
		} else {
			if (!"".equals(term)) {
				String[] hippenTermsDelim = null;
				term = term.replaceAll("@BRACEOPEN@", "(").replaceAll("@BRACECLOSE@", ")").trim();
				SCOPUS_MARINER_FIELD efield = SCOPUS_MARINER_FIELD.valueOf(field.toUpperCase().trim());
				// todo 연도검색일때는 하이픈이 제거되면 안된다
				// logger.debug("make term {} ", term);
				switch (efield) {
				case AUTHORNAME:
				case AUTHINDEXNAME:
				case AUTHFIRST:
				case AUTHLASTNAME:
					// term = term.replaceAll("(\\.|\\,|-)",
					// " ").replaceAll("(\\s{1,})", " ");
					term = term.replaceAll("(\\.|-)", " ").replaceAll("(\\s{1,})",
							" "); /*
									 * dqdoc 생성시 , 는 포함한다 .
									 */
					break;
				case SORTYEAR:
				case PUBYEAR:
				case DOI:
					isnear = false;
					break;
				case TITLE:
				case ABS:
				case AUTHKEY:
				case INDEXTERMS:
				case TS:
				case TK:
				case ALL:
					// logger.debug("term {}, {}", term , term.contains("-") +
					// field);
					if (term.contains("-") & !term.contains("*")) {
						isnear = true;
					}
					break;
				default:
					// term = term.replaceAll("-", " ").trim();
					break;
				}

				if (isnear) {
					// logger.debug("!개선 2016.09.01 완전일치검색 스테밍 적용하지 않는다. {}",
					// term);
					// logger.debug("!개선 2016.09.01 완전일치검색 스테밍 적용하지 않는다. {}",
					// efield);
					// DOI 필드에는 모든 단어가 들어가야 한다 - 2016.10.21
					String doiTerm = term;
					term = term.replaceAll("-", " ").trim();
					op = Protocol.WhereSet.OP_HASALL;
					if (isNot) {
						op = Protocol.WhereSet.OP_NOT;
					}
					switch (efield) {
					case PUBYEAR:
					case SORTYEAR:
						break;
					default:
						break;
					}
					switch (efield) {
					case SORTING:
						/* 정렬 필드는 검색 필드에 추가하지 않는다. 2014-05-23 */
						break;
					case ABS:
						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.ABS_EN, term));
						} else {
							result.addAll(withinSearchExceptWildCard(new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.ABS_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.ABS_EN }, term));
						}
						break;
					case TITLE:
						// logger.debug("{} / {}", term);
						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.TITLE_EN, term));
						} else {
							result.addAll(withinSearchExceptWildCard(new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_EN }, term));
							// result.addAll(nearWildCardMakeWhereSet(new
							// SCOPUS_MARINER_FIELD[] {
							// SCOPUS_MARINER_FIELD.TITLE },
							// new SCOPUS_MARINER_FIELD[] {
							// SCOPUS_MARINER_FIELD.TITLE }, term));
						}
						break;
					case AUTHKEY:
						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHKEY_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHKEY_E.getIndexField(),
							// op, term, 150));
						} else {
							result.addAll(nearWildCardMakeWhereSet(new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_EN }, term));
						}
						break;
					case INDEXTERMS:
						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.INDEXTERMS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.INDEXTERMS_E.getIndexField(),
							// op, term, 150));
						} else {
							result.addAll(nearWildCardMakeWhereSet(new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.INDEXTERMS_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.INDEXTERMS_EN }, term));
						}
						break;
					case KEY:
						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHKEY_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHKEY_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.INDEXTERMS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.INDEXTERMS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.addAll(nearWildCardMakeWhereSet(
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_P, SCOPUS_MARINER_FIELD.INDEXTERMS_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_EN, SCOPUS_MARINER_FIELD.INDEXTERMS_EN }, term));
						}
						break;
					case TS:
						if (term.indexOf("*") == -1) {

							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */

							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.TITLE_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.TITLE_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.ABS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.ABS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHKEY_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHKEY_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.INDEXTERMS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.INDEXTERMS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.addAll(nearWildCardMakeWhereSet(
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.ABS_P,
											SCOPUS_MARINER_FIELD.AUTHKEY_P, SCOPUS_MARINER_FIELD.INDEXTERMS_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_EN, SCOPUS_MARINER_FIELD.ABS_EN,
											SCOPUS_MARINER_FIELD.AUTHKEY_EN, SCOPUS_MARINER_FIELD.INDEXTERMS_EN },
									term));
						}
						break;
					case TK:
						if (term.indexOf("*") == -1) {
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.TITLE_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.TITLE_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHKEY_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHKEY_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.INDEXTERMS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.INDEXTERMS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.addAll(nearWildCardMakeWhereSet(
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.AUTHKEY_P,
											SCOPUS_MARINER_FIELD.INDEXTERMS_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_EN, SCOPUS_MARINER_FIELD.AUTHKEY_EN,
											SCOPUS_MARINER_FIELD.INDEXTERMS_EN },
									term));
						}
						break;
					case ALL:
						if (term.indexOf("*") == -1) {
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.EID.getIndexField(), op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.TITLE_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.TITLE_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.ABS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.ABS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHKEY_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHKEY_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.INDEXTERMS_EN, term));
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.INDEXTERMS_E.getIndexField(),
							// op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.AUTHORNAME.getIndexField(), op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.AFFIL_E.getIndexField(), op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.SRCTITLE.getIndexField(), op, term, 150));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.addAll(nearWildCardMakeWhereSet(
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.EID, SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.ABS_P,
											SCOPUS_MARINER_FIELD.AUTHKEY_P, SCOPUS_MARINER_FIELD.INDEXTERMS_P, SCOPUS_MARINER_FIELD.AUTHORNAME_P,
											SCOPUS_MARINER_FIELD.AFFIL, SCOPUS_MARINER_FIELD.SRCTITLE },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.EID, SCOPUS_MARINER_FIELD.TITLE_EN, SCOPUS_MARINER_FIELD.ABS_EN,
											SCOPUS_MARINER_FIELD.AUTHKEY_EN, SCOPUS_MARINER_FIELD.INDEXTERMS_EN, SCOPUS_MARINER_FIELD.AUTHORNAME,
											SCOPUS_MARINER_FIELD.AFFIL_E, SCOPUS_MARINER_FIELD.SRCTITLE },
									term));
						}
						break;
					case DAFFIL:
						if (term.indexOf("\\*") != -1) {
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.DAFFIL_E.getIndexField(), op, term, 150));
						} else {
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.DAFFIL_E, term));
						}
						break;
					case AFFIL:
						if (term.indexOf("\\*") != -1) {
							result.add(new WhereSet(SCOPUS_MARINER_FIELD.AFFIL.getIndexField(), op, term, 150));
						} else {
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AFFIL_E, term));
						}
						break;
					case DOI:
						result.add(new WhereSet(efield.getIndexField(), op, doiTerm, 150));
						break;
					case AUTHORNAME:

						if (term.indexOf("*") == -1) {
							/**
							 * 인접검색 반영.
							 * 
							 * @since 2015-09-30
							 * @author coreawin
							 */
							result.addAll(withinSearch(SCOPUS_MARINER_FIELD.AUTHORNAME, term));
							// if (term.indexOf("\\*") != -1) {
							// result.add(new
							// WhereSet(SCOPUS_MARINER_FIELD.AUTHORNAME_P.getIndexField(),
							// op, term, 150));
						} else {
							result.addAll(nearWildCardMakeWhereSet(new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHORNAME_P },
									new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHORNAME }, term));
						}
						break;
					case AUTHINDEXNAME:
					case SRCTITLE:
						result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						break;
					case CITCOUNT:
					case REFCOUNT:
						if (!term.contains("-")) {
							result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						}
						break;
					case SORTYEAR:
					case PUBYEAR:
						if (term.contains("-")) {
							String[] terms = term.split("-");
							int start = Integer.parseInt(terms[0]);
							int end = Integer.parseInt(terms[1]);
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							for (int idx = start; idx <= end; idx += 1) {
								result.add(new WhereSet(efield.getIndexField(), op, String.valueOf(idx), 150));
								if (idx != end)
									result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							}
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						}
						break;
					default:
						result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						break;
					}
				} else {
					// logger.debug("============================={} / {}
					// ===============", field, term);
					op = Protocol.WhereSet.OP_HASANY;
					if (!term.contains("*")) {
						op = Protocol.WhereSet.OP_HASALL;
					}
					if (isNot) {
						op = Protocol.WhereSet.OP_NOT;
					}

					switch (efield) {
					case TS:
						// /**TODO 2016.03.31 초록 system. 검색안되는 현상 대체 */
						// logger.debug("초록 인접검색 버그 : ABS_E -> ABS로 임시 대체
						// 2016.03.31");
						/** 2016.04.11 초록 system. 검색안되는 현상 엔진 버그 수정 완료 */
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result,
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.ABS_P, SCOPUS_MARINER_FIELD.AUTHKEY_P,
										SCOPUS_MARINER_FIELD.INDEXTERMS_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_E, SCOPUS_MARINER_FIELD.ABS_E, SCOPUS_MARINER_FIELD.AUTHKEY_E,
										SCOPUS_MARINER_FIELD.INDEXTERMS_E },
								term, 150);
						break;
					case TK:
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result,
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.AUTHKEY_P,
										SCOPUS_MARINER_FIELD.INDEXTERMS_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_E, SCOPUS_MARINER_FIELD.AUTHKEY_E,
										SCOPUS_MARINER_FIELD.INDEXTERMS_E },
								term, 150);
						break;
					case ALL:
						// /**TODO 2016.03.31 초록 system. 검색안되는 현상 대체 */
						// logger.debug("초록 인접검색 버그 : ABS_E -> ABS로 임시 대체
						// 2016.03.31");
						/** 2016.04.11 초록 system. 검색안되는 현상 엔진 버그 수정 완료 */
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						// result.add(new
						// WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						setWhereSet(result,
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.EID, SCOPUS_MARINER_FIELD.TITLE_P, SCOPUS_MARINER_FIELD.ABS_P,
										SCOPUS_MARINER_FIELD.AUTHKEY_P, SCOPUS_MARINER_FIELD.INDEXTERMS_P, SCOPUS_MARINER_FIELD.AUTHORNAME_P,
										SCOPUS_MARINER_FIELD.AFFIL, SCOPUS_MARINER_FIELD.SRCTITLE },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.EID, SCOPUS_MARINER_FIELD.TITLE_E, SCOPUS_MARINER_FIELD.ABS_E,
										SCOPUS_MARINER_FIELD.AUTHKEY_E, SCOPUS_MARINER_FIELD.INDEXTERMS_E, SCOPUS_MARINER_FIELD.AUTHORNAME,
										SCOPUS_MARINER_FIELD.AFFIL_E, SCOPUS_MARINER_FIELD.SRCTITLE },
								term, 150);
						// result.add(new
						// WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						break;
					case CITCOUNT:
					case REFCOUNT:
						if (!term.contains("-")) {
							result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						}
						break;
					case SORTYEAR:
					case PUBYEAR:
						if (term.contains("-")) {
							String[] terms = parsePeriod(efield, term);
							// String[] terms = term.split("-");
							int start = Integer.parseInt(terms[0]);
							int end = Integer.parseInt(terms[1]);
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							// StringBuffer buf = new StringBuffer();
							for (int idx = start; idx <= end; idx += 1) {
								// buf.append(idx);
								// buf.append(" ");
								result.add(new WhereSet(efield.getIndexField(), Protocol.WhereSet.OP_HASANY, String.valueOf(idx), 150));
								if (idx != end)
									result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							}
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						} else {
							result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						}
						break;
					case KEY:
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						// result.add(new
						// WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_P, SCOPUS_MARINER_FIELD.INDEXTERMS_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_E, SCOPUS_MARINER_FIELD.INDEXTERMS_E }, term, 150);
						// result.add(new
						// WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						break;
					case DOI:
						result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						break;
					case AUTHORNAME:
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHORNAME_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHORNAME }, term, 150);
						break;
					case ABS:
						// /**TODO 2016.03.31 초록 system. 검색안되는 현상 대체 */
						// logger.debug("초록 인접검색 버그 : ABS_E -> ABS로 임시 대체
						// 2016.03.31");
						/** 2016.04.11 초록 system. 검색안되는 현상 엔진 버그 수정 완료 */
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.ABS_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.ABS_E }, term, 150);
						break;
					case TITLE:
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.TITLE_E }, term, 150);
						break;
					case AUTHKEY:
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.AUTHKEY_E }, term, 150);
						break;
					case INDEXTERMS:
						/**
						 * 2016.04.20 OLED 단어가 OL 단어에 형태소 분석 매칭되서 일반필드에서 _E 로
						 * 검색하지 않도록 수정함.
						 */
						setWhereSet(result, new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.INDEXTERMS_P },
								new SCOPUS_MARINER_FIELD[] { SCOPUS_MARINER_FIELD.INDEXTERMS_E }, term, 150);
						break;
					default:
						result.add(new WhereSet(efield.getIndexField(), op, term, 150));
						break;
					}
				}
			} else {
				// throwError("검색어가 입력되지 않았습니다.", false);
			}
		}
		for (int idx = 0; idx < c; idx += 1) {
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}
		return result;
	}

	private void setWhereSet(LinkedList<WhereSet> result, SCOPUS_MARINER_FIELD[] wildCardFields, SCOPUS_MARINER_FIELD[] generalFields, String term,
			int weight) {
		if (wildCardFields == null) {
			wildCardFields = new SCOPUS_MARINER_FIELD[0];
		}
		if (generalFields == null) {
			generalFields = new SCOPUS_MARINER_FIELD[0];
		}
		String[] hippenTermsDelim = null;
		if (term.contains("-")) {
			term = term.replaceAll("-", " ");
			if (term.contains("*")) {
				hippenTermsDelim = term.split(" ");
			}
		}
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		if (hippenTermsDelim != null) {
			for (String _term : hippenTermsDelim) {
				// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				if (_term.contains("*")) {
					setSearchWhereSet(result, wildCardFields, _term, weight, Protocol.WhereSet.OP_HASANY);
				} else {
					setSearchWhereSet(result, generalFields, _term, weight, Protocol.WhereSet.OP_HASALL);
				}
				// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			}
		} else {
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
			if (term.contains("*")) {
				setSearchWhereSet(result, wildCardFields, term, weight, Protocol.WhereSet.OP_HASANY);
			} else {
				setSearchWhereSet(result, generalFields, term, weight, Protocol.WhereSet.OP_HASALL);
			}
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
	}

	private void setSearchWhereSet(LinkedList<WhereSet> result, SCOPUS_MARINER_FIELD[] fields, String term, int weight, byte op) {
		// if (fields.length > 1) {
		// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		// }
		for (SCOPUS_MARINER_FIELD field : fields) {
			/*
			 * [2017-02-06] Prepost 색인된 필드에 한하여 검색어에 Wildcard 문자열이 포함되어 있다면 해당
			 * 필드에는 검색어에는 '*'문자열을 제거하지 않는다. by 이관재<br />
			 */
			if (term.contains("*")) {
				if (field == SCOPUS_MARINER_FIELD.EID || field == SCOPUS_MARINER_FIELD.AFFIL || field == SCOPUS_MARINER_FIELD.AUTHORNAME_P) {
					result.add(new WhereSet(field.getIndexField(), op, term, weight));
				} else {
					result.add(new WhereSet(field.getIndexField(), op, term.replaceAll("\\*", ""), weight));
				}
			} else {
				/**
				 * [2017-11-17] 영어 형태소 분석시 일부 데이터에서 검색이 동작하지 않던 문제 발생 ~ing로 끝나는
				 * 단어에 대해서 영어 Stemming 처리를 통해 원형으로 검색어를 설정 재목, 초록, 저자, 인덱스 키워드
				 */
				if (field == SCOPUS_MARINER_FIELD.TITLE_E || field == SCOPUS_MARINER_FIELD.ABS_E || field == SCOPUS_MARINER_FIELD.INDEXTERMS_E
						|| field == SCOPUS_MARINER_FIELD.AUTHKEY_E) {
					String _term = stemming.analysis(term + " a");
					if (_term != null) {
						String[] arr = _term.split(" ");
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet(field.getIndexField(), op, arr[0].trim(), weight));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet(field.getIndexField(), op, term, weight));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					} else {
						result.add(new WhereSet(field.getIndexField(), op, term, weight));
					}
				} else {
					result.add(new WhereSet(field.getIndexField(), op, term, weight));
				}
			}
			// if (fields.length > 1) {
			result.add(new WhereSet(Protocol.WhereSet.OP_OR));
			// }
		}
		if (fields.length > 1) {
			result.removeLast();
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}
	}

	private String[] parsePeriod(SCOPUS_MARINER_FIELD efield, String terms) {
		String[] result = new String[] { "1996", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) };
		if (terms.contains("-")) {
			String[] tmp = terms.split("-");
			switch (efield) {
			case PUBYEAR:
			case SORTYEAR:
				result[0] = UtilString.nullCkeck(tmp[0], "1996");
				try {
					result[1] = UtilString.nullCkeck(tmp[1], String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				} catch (Exception e) {
				}
				break;
			}
		} else {
			return null;
		}
		// logger.debug("result {}/{}", result[0], result[1]);
		return result;
	}

	/**
	 * 인접 검색 문법에 wildcar가 입력될 경우 인접 검색이 아닌 white space단위로 구분되어 검색한다.<br>
	 * 
	 * @author coreawin
	 * @date 2014. 6. 23.
	 * @param fields
	 *            일반 필드
	 * @param efields
	 *            인접 검색 필드.
	 * @param term
	 * @return
	 */
	private List<WhereSet> nearWildCardMakeWhereSet(SCOPUS_MARINER_FIELD[] fields, SCOPUS_MARINER_FIELD[] efields, String term) {
		List<WhereSet> result = new LinkedList<WhereSet>();
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		String[] terms = term.split(" ");
		LinkedList<String> termList = new LinkedList<String>();
		StringBuffer _buf = new StringBuffer();
		for (String t : terms) {
			if (t.indexOf("*") != -1) {
				termList.add(_buf.toString());
				_buf.setLength(0);
				termList.add(t);
			} else {
				_buf.append(t);
				_buf.append(" ");
			}
		}
		termList.add(_buf.toString());

		for (String t : termList) {
			t = t.trim();
			if ("".equals(t))
				continue;

			if (t.indexOf("*") != -1) {
				// wildcard가 입력되어 있다면...
				if (fields.length > 1)
					result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				for (SCOPUS_MARINER_FIELD f : fields) {
					// FIELD간은 OR 검색이다.
					// logger.debug(f + " : " + t);
					/**
					 * [2017-02-06] Prepost 색인된 필드에 한하여 검색어에 Wildcard('*') 문자열이
					 * 포함되어 있다면 해당 필드에는 검색어에는 WildCard 문자열을 제거하지 않는다. by 이관재
					 * <br />
					 */
					if (f == SCOPUS_MARINER_FIELD.EID || f == SCOPUS_MARINER_FIELD.AFFIL || f == SCOPUS_MARINER_FIELD.DAFFIL
							|| f == SCOPUS_MARINER_FIELD.AUTHORNAME_P) {
						result.add(new WhereSet(f.getIndexField(), Protocol.WhereSet.OP_HASANY, t, 150));
					} else {
						result.add(new WhereSet(f.getIndexField(), Protocol.WhereSet.OP_HASANY, t.replaceAll("\\*", ""), 150));
					}
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				result.remove(result.size() - 1);
				if (fields.length > 1)
					result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			} else {
				// wildcard가 입력되어 있지 않다면...
//				if (efields.length > 1)
					result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				for (SCOPUS_MARINER_FIELD f : efields) {
					// FIELD간은 OR 검색이다.
					result.add(new WhereSet(f.getIndexField(), Protocol.WhereSet.OP_HASALL, t, 150));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				result.remove(result.size() - 1);
//				if (efields.length > 1)
					result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			}
			result.add(new WhereSet(Protocol.WhereSet.OP_AND));
		}
		result.remove(result.size() - 1);
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		return result;
	}

	private WhereSet[] makeWhereSetList(List<String> queryList) throws QueryConverterException {
		List<WhereSet> result = new LinkedList<WhereSet>();
		if (queryList.size() == 0)
			return new WhereSet[0];
		WhereSet ws = null;
		// logger.debug("input queryList {} ", queryList);
		boolean isNot = false;

		WhereSet prev = null;
		for (String e : queryList) {
			WhereSet curr = null;
			String[] values = e.split(DELIM_FIELD_RULE);
			if (values.length != 2) {
				String msg = String.format("검색어가 누락된 필드가 발견되었습니다. %s ", e.replaceAll(DELIM_FIELD_RULE, ""));
				logger.warn(msg);
				throw new QueryConverterException(msg);
				// continue;
			}
			String field = values[0];
			String value = values[1];
			String _value = value.replaceFirst("(\\(\\s{0,}\\))", ERROR_FIELD_RULE);

			if (_value.indexOf(ERROR_FIELD_RULE) != -1) {
				String msg = String.format("검색어가 누락된 필드가 발견되었습니다. %s : %s ", field, value);
				logger.warn(msg);
				throw new QueryConverterException(msg);
				// continue;
			}

			// logger.debug("* {} / {}", field, value);
			List<WhereSet> whereSetList = makeWhereSet(field, value);
			if (whereSetList.size() > 0) {
				curr = whereSetList.get(0);
				ws = makerOperator(prev, curr);
				if (ws != null) {
					result.add(ws);
				}
				prev = whereSetList.get(whereSetList.size() - 1);
				/*
				 * makerForEID를 통해서 나온 것이라면 해당 연산자를 삭제 이는 일종의 플래그 역활을 담당.
				 */
				if ((prev.getOperation() == Protocol.WhereSet.OP_HASALLONE) | (prev.getOperation() == Protocol.WhereSet.OP_HASANYONE)) {
					whereSetList.remove(whereSetList.size() - 1);
				}
			}
			result.addAll(whereSetList);
		}

		/** 20170103. 마지막 식에 AND나 OR로 끝난다면 삭제. 임시조치 */
		WhereSet lastWhereSet = result.get(result.size() - 1);
		if (lastWhereSet.getOperation() == Protocol.WhereSet.OP_AND || lastWhereSet.getOperation() == Protocol.WhereSet.OP_OR) {
			result.remove(result.size() - 1);
		}

		LinkedList<WhereSet> verifyingSet = new LinkedList<WhereSet>();
		int idx = 0;
		int openBraceCnt = 0;
		int closeBraceCnt = 0;
		WhereSet prevWs1 = null;

		LinkedList<WhereSet> resWsList = new LinkedList<WhereSet>();
		final byte _OPEN = Protocol.WhereSet.OP_BRACE_OPEN;
		final byte _CLOSE = Protocol.WhereSet.OP_BRACE_CLOSE;
		final byte _AND = Protocol.WhereSet.OP_AND;
		final byte _OR = Protocol.WhereSet.OP_OR;
		byte prevOp = Protocol.WhereSet.OP_HASALLONE;
		byte nextOp = Protocol.WhereSet.OP_HASALLONE;
		for (int i = 0; i < result.size(); i++) {
			WhereSet ws1 = result.get(i);
			byte o = ws1.getOperation();
			if (i < result.size() - 1) {
				nextOp = result.get(i + 1).getOperation();
			}
			if (prevOp == _OPEN && (o == _AND || o == _OR) && nextOp == _OPEN) {
				prevOp = o;
				continue;
			}

			if (prevOp == _CLOSE && (o == _AND || o == _OR) && nextOp == _CLOSE) {
				prevOp = o;
				continue;
			}

			if (o == _OPEN && (prevOp == _AND || prevOp == _OR) && nextOp == _CLOSE) {
				prevOp = o;
				continue;
			}

			resWsList.add(ws1);

			switch (o) {
			case Protocol.WhereSet.OP_BRACE_OPEN:
				openBraceCnt += 1;
				break;
			case Protocol.WhereSet.OP_BRACE_CLOSE:
				closeBraceCnt += 1;
				break;
			default:
				break;
			}
			prevOp = o;
		}
		// for (WhereSet ws1 : resWsList) {
		// System.out.println(ws1);
		// }

		/* 괄호 쌍 처리 이후에 쿼리 연산자 매칭을 수행한다. */
		cleansingQuery(verifyingSet, resWsList);
		int i = 0;
		boolean isError = false;
		while (!checkBraceWhereSet(verifyingSet)) {
			/* Stack을 활용하여 쿼리의 내부 처리를 수행한다. */
			List<WhereSet> refineRes = setBraceWhereSet(verifyingSet);
			verifyingSet.clear();
			/* 괄호 쌍 처리 이후에 쿼리 연산자 처리를 마무리 한다. (100번 이상 돌면 break 처리한다. */
			cleansingQuery(verifyingSet, refineRes);
			if (i >= 100) {
				isError = true;
				break;
			}
			i++;
		}

		// logger.debug("STACK CHECK : {}", checkBraceWhereSet(verifyingSet));

		WhereSet[] r = new WhereSet[verifyingSet.size()];
		for (WhereSet ws1 : verifyingSet) {
			r[idx++] = ws1;
		}

		if (isError) {
			logger.error(this.query);
			logger.error("open cnt {}, close cnt {}", openBraceCnt, closeBraceCnt);
			for (WhereSet w : r) {
				logger.error("error info: {}", w.toString());
			}
			throw new QueryConverterException("[ErrorCode-1003] 괄호 연산자가 올바르게 입력되어 있지 않습니다.");
		}
		return r;
	}

	/**
	 * STACK을 활용하여 괄호의 갯수를 맞춰준다.
	 */
	private LinkedList<WhereSet> setBraceWhereSet(List<WhereSet> vws) {
		LinkedList<WhereSet> res = new LinkedList<WhereSet>();
		LinkedList<WhereSet> stack = new LinkedList<WhereSet>();
		for (WhereSet ws : vws) {
			switch (ws.getOperation()) {
			case Protocol.WhereSet.OP_BRACE_OPEN:
				stack.push(ws);
				res.add(ws);
				break;
			case Protocol.WhereSet.OP_BRACE_CLOSE:
				if (!stack.isEmpty()) {
					stack.pop();
					res.add(ws);
				}
				break;
			default:
				res.add(ws);
			}
		}
		return res;
	}

	/**
	 * 
	 * 검색 WhereSet을 정상적인 쿼리로 정리한다.
	 *
	 * @author 이관재
	 * @date 2017. 2. 7.
	 * @version 1.0
	 * @param storeQuery
	 * @param srcQuery
	 */
	private void cleansingQuery(List<WhereSet> storeQuery, List<WhereSet> srcQuery) {

		final byte _OPEN = Protocol.WhereSet.OP_BRACE_OPEN;
		final byte _CLOSE = Protocol.WhereSet.OP_BRACE_CLOSE;
		final byte _AND = Protocol.WhereSet.OP_AND;
		final byte _OR = Protocol.WhereSet.OP_OR;

		for (int i = 0; i < srcQuery.size(); i++) {
			WhereSet ws1 = srcQuery.get(i);
			byte w = ws1.getOperation();
			byte n = -1;
			if (i + 1 < srcQuery.size()) {
				n = srcQuery.get(i + 1).getOperation();
				if (w == _OPEN & (n == _AND || n == _OR || n == _CLOSE)) {
					continue;
				}
				if ((w == _AND || w == _OR) & (n == _CLOSE)) {
					continue;
				}

				if ((w == _AND || w == _OR) & (n == _AND || n == _OR)) {
					continue;
				}
			}

			if (storeQuery.size() == 0) {
				if (w == _CLOSE || w == _AND || w == _OR) {
					continue;
				}
			}
			storeQuery.add(ws1);
			// logger.debug("====================== {}", resWsList.get(i));
		}
	}

	private boolean checkBraceWhereSet(List<WhereSet> vws) {
		int opB = 0;
		int cB = 0;
		for (WhereSet ws : vws) {
			switch (ws.getOperation()) {
			case Protocol.WhereSet.OP_BRACE_OPEN:
				opB++;
				break;
			case Protocol.WhereSet.OP_BRACE_CLOSE:
				cB++;
				break;
			default:
			}
		}
		if (opB != cB) {
			return false;
		} else {
			return true;
		}
	}

	public FilterSet[] getFilterSet() {
		return this.filterSets;
	}

	/**
	 * within 검색을 지원한다.<br>
	 * 
	 * @author coreawin
	 * @date 2015. 9. 30.
	 */
	public List<WhereSet> withinSearch(SCOPUS_MARINER_FIELD field, String terms) {
		terms = terms.replaceAll("[:/,]", " ").replaceAll("\\s{1,}", " ").trim();
		String[] termA = terms.split(" ");
		List<WhereSet> result = new LinkedList<WhereSet>();
		if (termA.length == 1) {
			result.add(new WhereSet(field.getIndexField(), Protocol.WhereSet.OP_HASALL, terms, 150));
		} else {
			// result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
			int termASize = termA.length;
			for (int idx = 0; idx < termASize; idx++) {
				for (int jdx = 0; jdx < termASize; jdx++) {
					if (jdx > idx) {
						String iTerm = termA[idx].trim();
						String jTerm = termA[jdx].trim();
						if (iTerm.equalsIgnoreCase(jTerm)) {
							result.add(new WhereSet(field.getIndexField(), Protocol.WhereSet.OP_HASALL, jTerm, 300));
						} else {
							String[] q = new String[] { iTerm, jTerm };
							result.add(new WhereSet(field.getIndexField(), Protocol.WhereSet.OP_PROXIMITY_WITHIN, q, new int[] { 300, 300 },
									(jdx - idx) * 1));
						}
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					}
				}
			}
			if (result.size() > 0) {
				result.remove(result.size() - 1);
				result.add(0, new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			}
		}
		return result;
	}

	/**
	 * 인접검색시 *가 들어오면 *를 제외한 나머지 부분에 대한 인접검색을 지원한다.<br>
	 * 
	 * @author coreawin
	 * @date 2015. 9. 30.
	 */
	/**
	 * @author pc
	 * @date 2016. 9. 1.
	 * @param fields
	 *            prepost 필드.
	 * @param enfields
	 *            인접기능은 있고 스테밍이 되지 않은 필드.
	 * @param terms
	 * @return
	 */
	public List<WhereSet> withinSearchExceptWildCard(SCOPUS_MARINER_FIELD[] fields, SCOPUS_MARINER_FIELD[] enfields, String terms) {
		terms = terms.replaceAll("\\s{1,}", " ");
		String[] termA = terms.split(" ");
		LinkedList<WhereSet> result = new LinkedList<WhereSet>();
		if (termA.length == 1) {
			if (terms.indexOf("*") != -1) {
				for (SCOPUS_MARINER_FIELD _fields : fields) {
					result.add(new WhereSet(_fields.getIndexField(), Protocol.WhereSet.OP_HASANY, terms.replaceAll("\\*", ""), 150));
					result.add(new WhereSet(Protocol.WhereSet.OP_AND));
				}
			} else {
				for (SCOPUS_MARINER_FIELD _fields : enfields) {
					result.add(new WhereSet(_fields.getIndexField(), Protocol.WhereSet.OP_HASALL, terms, 150));
					result.add(new WhereSet(Protocol.WhereSet.OP_AND));
				}
			}
			if (result.size() > 0) {
				result.removeLast();
			}
		} else {
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
			int termASize = termA.length;
			int wildTermsCnt = 0;
			Set<Integer> check = new HashSet<Integer>();
			for (int idx = 0; idx < termASize; idx++) {
				String ia = termA[idx].trim();
				if (ia.indexOf("*") != -1 || ia.indexOf("-") != -1) {
					if (ia.indexOf("*") != -1) {
						ia = ia.replaceAll("\\*", "");
					}
					for (SCOPUS_MARINER_FIELD _fields : fields) {
						result.add(new WhereSet(_fields.getIndexField(), Protocol.WhereSet.OP_HASANY, ia, 150));
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					}
					wildTermsCnt += 1;
					continue;
				}
				check.add(idx);
				for (int jdx = 0; jdx < termASize; jdx++) {
					if (jdx > idx) {
						String ja = termA[jdx].trim();
						if (ja.indexOf("*") != -1)
							continue;
						String[] q = new String[] { ia, ja };
						for (SCOPUS_MARINER_FIELD _fields : enfields) {
							byte prevOperation = result.getLast().getOperation();
							if (prevOperation != Protocol.WhereSet.OP_AND && prevOperation != Protocol.WhereSet.OP_BRACE_OPEN) {
								/**
								 * 2016.09.01 위에서 wildcard 식 작성후 AND 가 없으므로
								 * 넣어준다.
								 */
								result.add(new WhereSet(Protocol.WhereSet.OP_AND));
							}
							result.add(new WhereSet(_fields.getIndexField(), Protocol.WhereSet.OP_PROXIMITY_WITHIN, q, new int[] { 300, 300 },
									(jdx - idx) * 1));
							result.add(new WhereSet(Protocol.WhereSet.OP_AND));
						}
					}
				}
			}
			// logger.debug("terms {} {}", (termASize),( wildTermsCnt));
			if (termASize - wildTermsCnt < 2) {
				for (SCOPUS_MARINER_FIELD _fields : enfields) {
					for (int _idx : check) {
						result.add(new WhereSet(_fields.getIndexField(), Protocol.WhereSet.OP_HASALL, termA[_idx], 300));
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					}
				}
			}
			if (fields.length > 0 && termASize > 0) {
				result.removeLast();
			}
			result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		}
		return result;
	}

	/**
	 * @author coreawin
	 * @date 2014. 6. 16.
	 * @param args
	 * @throws QueryConverterException
	 */
	public static void main(String[] args) throws QueryConverterException {
		String query = "TITLE=McGill Univ OR Quebec OR Canada ABS=(web contents titl) AUTHKEY=web contents OR titl";
		query = "TK=(\"sensor\")  PY=(2012-2017) AND  TK=(\"precision medicine\")";
		query = "TK=((route and (optimal or guidance or planning or choice)) and (transportation or transit)) PY=(2012-2017)";
		query = "TK=(\"route\" and (\"optimal\" or \"guidance\" or \"planning\" or \"choice\")) PY=(2012-2017)";
		query = "TK=((\"optimal\" or \"guidance\" or \"planning\" or \"choice\") \"route\") PY=(2012-2017)";
		query = "DOI=(10.1134/S0006297908080063)";
		System.setProperty("EJIANA_HOME", "E:\\project\\2014\\KISTI_SCOPUS_IBS_SEARCH\\resources") ;
		QueryConverterWoS queryConverter = new QueryConverterWoS(query);
		WhereSet[] _result = queryConverter.getWhereSet();
		queryConverter.logger.debug("=============================== result ===========================================");
		int idx = 0;
		for (WhereSet ws : _result) {
			queryConverter.logger.debug("{} /  {}", idx++, ws);
		}

		FilterSet[] _filters = queryConverter.getFilterSet();
		for (FilterSet fs : _filters) {
			queryConverter.logger.debug("FILTER = {}", fs);
		}

		System.out.println(queryConverter.getWildCardSearchTerms());
		System.out.println(queryConverter.getSearchTerms());
	}

	public Map<String, Set<String>> getWildCardSearchTerms() {
		return wildCardSearchTerms;
	}

}
