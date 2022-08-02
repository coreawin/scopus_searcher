/**
 * 
 */
package com.diquest.scopus.searchrule;

import java.util.Set;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.GroupBySet;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;

/**
 * 검색 API 헬퍼.
 * 
 * @author neon
 * @date 2014. 5. 2.
 * @Version 1.0
 */
public class SearchQueryHelper {

	/**
	 * 검색 결과를 위한 SelectSet을 가져온다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 */
	public static SelectSet[] getSelectSet() {
		SelectSet[] selectSet = new SelectSet[MARINER_FIELD._SELECT_FIELDS.length];
		int cnt = 0;
		for (SCOPUS_MARINER_FIELD e : MARINER_FIELD._SELECT_FIELDS) {
			selectSet[cnt++] = new SelectSet(e.getValue(), (byte) Protocol.SelectSet.NONE);
		}
		return selectSet;
	}

	/**
	 * 검색 결과를 위한 SelectSet을 가져온다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 */
	public static SelectSet[] getSelectSet(Set<String> field) {
		SelectSet[] selectSet = new SelectSet[MARINER_FIELD._SELECT_FIELDS.length];
		int cnt = 0;
		for (SCOPUS_MARINER_FIELD e : MARINER_FIELD._SELECT_FIELDS) {
			switch (e) {
			case TITLE:
			case ABS:
				if (field.contains(e.name())) {
					selectSet[cnt++] = new SelectSet(e.getValue() + "_NO_HTML", (byte) Protocol.SelectSet.HIGHLIGHT);
				} else {
					selectSet[cnt++] = new SelectSet(e.getValue() + "_NO_HTML", (byte) Protocol.SelectSet.NONE);
				}
				break;
			case TITLE_E:
			case ABS_E:
				if (field.contains(e.name())) {
					selectSet[cnt++] = new SelectSet(e.getValue(), (byte) Protocol.SelectSet.HIGHLIGHT);
				} else {
					selectSet[cnt++] = new SelectSet(e.getValue(), (byte) Protocol.SelectSet.NONE);
				}
				break;
			default:
				selectSet[cnt++] = new SelectSet(e.getValue(), (byte) Protocol.SelectSet.NONE);
				break;
			}
		}
		return selectSet;
	}

	/**
	 * 검색 결과를 위한 SelectSet을 가져온다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 */
	public static SelectSet[] getSelectCitByDocument(String type) {
		SelectSet[] selectSet = new SelectSet[2];
		selectSet[0] = new SelectSet(SCOPUS_MARINER_FIELD.EID.getValue(), (byte) Protocol.SelectSet.NONE);
		if ("CITL".equals(type)) {
			selectSet[1] = new SelectSet(SCOPUS_MARINER_FIELD.CITEID.getValue(), (byte) Protocol.SelectSet.NONE);
		} else {
			selectSet[1] = new SelectSet(SCOPUS_MARINER_FIELD.REFEID.getValue(), (byte) Protocol.SelectSet.NONE);
		}
		return selectSet;
	}

	public static SelectSet[] getViewAdvancedStatistic() {
		SelectSet[] selectSet = new SelectSet[MARINER_FIELD._SELECT_FIELD_VIEW_ADV_STATISTICS.length];
		int cnt = 0;
		for (SCOPUS_MARINER_FIELD e : MARINER_FIELD._SELECT_FIELD_VIEW_ADV_STATISTICS) {
			selectSet[cnt++] = new SelectSet(e.getValue(), (byte) Protocol.SelectSet.NONE);
		}
		return selectSet;
	}

	/**
	 * 검색 결과를 위한 GroupSet을 가져온다. <br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 * @throws Exception
	 */
	public static GroupBySet[] getGroupBySet() throws Exception {
		GroupBySet[] groupSet = new GroupBySet[MARINER_FIELD._GROUP_FIELDS.length];
		int cnt = 0;
		for (SCOPUS_MARINER_FIELD e : MARINER_FIELD._GROUP_FIELDS) {
			switch (e) {
			case SORTYEAR:
			case PUBYEAR:
				groupSet[cnt++] = new GroupBySet(e.getGroupField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_NAME), "DESC");
				break;

			default:
				groupSet[cnt++] = new GroupBySet(e.getGroupField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC");
				break;
			}
		}
		return groupSet;
	}

	/**
	 * 검색 결과를 위한 GroupSet을 가져온다. <br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 * @throws Exception
	 */
	public static GroupBySet[] getGroupBySetViewAdvancedStatistic(SCOPUS_MARINER_FIELD type) throws Exception {
		GroupBySet[] groupSet = new GroupBySet[1];
		switch (type) {
		case YEAR_AFFIL:
		case YEAR_ASJC:
		case YEAR_AUID:
		case YEAR_CN:
		case YEAR_FASJC:
		case YEAR_AUKEY:
			groupSet[0] = new GroupBySet(type.getStatField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC");
			break;
		default:
			groupSet = new GroupBySet[0];
			break;
		}
		return groupSet;
	}

	/**
	 * 검색 결과를 위한 GroupSet을 가져온다. <br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 * @throws Exception
	 */
	public static GroupBySet[] getDashBoardYearInfo(SCOPUS_MARINER_FIELD type) throws Exception {
		GroupBySet[] groupSet = new GroupBySet[1];
		switch (type) {
		case YEAR_AFFIL:
		case YEAR_ASJC:
		case YEAR_AUID:
		case YEAR_AUKEY:
		case YEAR_CN:
		case YEAR_FASJC:
			groupSet[0] = new GroupBySet(type.getStatField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC");
			break;
		case SORTYEAR:
			groupSet[0] = new GroupBySet(SCOPUS_MARINER_FIELD.SORTYEAR.getGroupField(),
					(byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_NAME), "ASC");
			break;
		default:
			groupSet = new GroupBySet[0];
			break;
		}
		return groupSet;
	}

	/**
	 * 검색 결과를 위한 GroupSet을 가져온다. <br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 * @throws Exception
	 */
	public static GroupBySet[] getGroupBySetViewAdvancedStatistic() throws Exception {
		GroupBySet[] groupSet = new GroupBySet[MARINER_FIELD.VIEW_TIME_GROUP_INFO.length];
		for (int i = 0; i < MARINER_FIELD.VIEW_TIME_GROUP_INFO.length; i++) {
			groupSet[i] = new GroupBySet(MARINER_FIELD.VIEW_TIME_GROUP_INFO[i].getStatField(),
					(byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC");
		}
		return groupSet;
	}

	/**
	 * 검색 결과를 위한 통계정보 GroupBySet을 가져온다. <br>
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @return
	 * @throws Exception
	 */
	public static GroupBySet[] getViewStatisticsGroupBySet() throws Exception {
		GroupBySet[] groupSet = new GroupBySet[MARINER_FIELD.VIEW_STATITICS_INFO.length];
		int cnt = 0;
		for (SCOPUS_MARINER_FIELD e : MARINER_FIELD.VIEW_STATITICS_INFO) {
			switch (e) {
			case SORTYEAR:
			case PUBYEAR:
				groupSet[cnt++] = new GroupBySet(e.getGroupField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_NAME), "DESC");
				break;

			default:
				groupSet[cnt++] = new GroupBySet(e.getGroupField(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC");
				break;
			}
		}
		return groupSet;
	}

	/**
	 * OrderBySet 항목을 설정한다.
	 * 
	 * @author neon
	 * @date 2014. 5. 2.
	 * @param order
	 * @return
	 */
	public static OrderBySet[] getOrderBySet(String order) {
		SCOPUS_MARINER_FIELD field = SCOPUS_MARINER_FIELD.SORTYEAR;
		boolean desc = false;
		if (order != null) {
			String orderArr[] = order.split(":");
			if (orderArr[0].equals("SORTYEAR")) {
				if (orderArr[1].equals("desc")) {
					desc = false;
					field = SCOPUS_MARINER_FIELD.SORTYEAR;
				} else {
					desc = true;
					field = SCOPUS_MARINER_FIELD.SORTYEAR;
				}
			} else if (orderArr[0].equals("CIT_COUNT")) {
				desc = false;
				field = SCOPUS_MARINER_FIELD.CITCOUNT;
			} else if (orderArr[0].equals("REF_COUNT")) {
				desc = false;
				field = SCOPUS_MARINER_FIELD.REFCOUNT;
			}
		}
		return new OrderBySet[] { new OrderBySet(desc, field.getSortField(), Protocol.OrderBySet.OP_NONE) };
	}

	/**
	 * 검색 요청 환경 변수를 생성하여 리턴한다. search Time out 333 hours
	 * 
	 * @author neon
	 * @date 2013. 7. 8.
	 * @param ip
	 *            마리너 검색 요청 IP
	 * @param port
	 *            마리너 검색 요청 Port
	 * @return
	 */
	public static String setCommandSearchRequestProps(String ip, int port) {
		// String log = "search timeout setting 20 minutes";
		// Logger logger = LoggerFactory.getLogger(SearchQueryHelper.class);
		// logger.debug(log);
		CommandSearchRequest.setProps(ip, port, 20000 * 60 * 1000, 50, 150);
		return "";
	}

	/**
	 * 특허 데이터의 XML 데이터만 요청한다.
	 */
	public static SelectSet[] SELECT_XML_FIELD = new SelectSet[] {
			new SelectSet(SCOPUS_MARINER_FIELD.EID.getValue(), (byte) (Protocol.SelectSet.NONE), 300),
			new SelectSet(SCOPUS_MARINER_FIELD.XML.getValue(), (byte) (Protocol.SelectSet.NONE), 300), };

	/**
	 * EID를 통한 문헌 상세보기 검색 쿼리 Helper
	 * 
	 * @author neon
	 * @date 2013. 6. 28.
	 * @param eid
	 *            논문 EID
	 * @return
	 */
	public static QuerySet getDetailViewQuerySet(String eid) {
		QuerySet qs = new QuerySet(1);
		Query query = new Query("<b>", "</b>");
		query.setSearch(true); // 검색 여부 설정
		// query.setSearchOption(Protocol.SearchOption.CACHE);
		query.setDebug(true);
		query.setLoggable(true);
		query.setPrintQuery(true);
		// query.setResultCutOffSize(0);
		// query.setMaxHighlight(2);
		// query.setFaultless(true);
		query.setBrokerPagingInfo("");
		query.setBrokerPrevious(false);
		query.setIgnoreBrokerTimeout(true);
		query.setSelect(SELECT_XML_FIELD);
		query.setWhere(new WhereSet[] { new WhereSet(SCOPUS_MARINER_FIELD.EID.getIndexField(), Protocol.WhereSet.OP_HASANY, eid, 150) });
		query.setFrom("SCOPUS_2016");
		query.setValue("DetailView", eid);
		query.setResult(0, 2);
		qs.addQuery(query);
		return qs;
	}

	public static void main(String[] args) throws IRException {
	}

}
