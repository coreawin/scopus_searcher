/**
 * 
 */
package com.diquest.scopus.analysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.scopus.analysis.TestScopusSearch.DOC;

/**
 * @author coreawin
 * @date 2014. 6. 19.
 * @Version 1.0
 */
public class HCPCheck {
	static String[] eids = new String[] { "84887581653", "84861017679",

	};

	static Logger logger = LoggerFactory.getLogger("com.diquest.scopus.analysis.HCPCheck");
	
	private static String builddate = "20160422";

	/**
	 * 
	 * DB 커넥션 가져온다.
	 * 
	 */
	static class ConnectionFactory {

		private static ConnectionFactory instance = null;

		Connection conn = null;

		private static final String URL = "jdbc:oracle:thin:@203.250.195.124:1531:KISTI5";
		private static final String USER = "scopus";
		private static final String PASS = "scopus+11";

		public static synchronized ConnectionFactory getInstance() {
			if (instance == null) {
				instance = new ConnectionFactory();
			}
			return instance;
		}

		private ConnectionFactory() {
		}

		public Connection getConnection() throws Exception {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
				return conn = DriverManager.getConnection(URL, USER, PASS);
			} catch (Exception ex) {
				logger.debug("Error: " + ex.getMessage());
				throw ex;
			}
		}

		public void release(Connection conn) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		public void release(PreparedStatement pstmt, Connection conn) {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		public void release(ResultSet rs, PreparedStatement pstmt, Connection conn) {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}

		public void release(ResultSet rs, PreparedStatement pstmt) {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}

	}

	static {
		try {
			builddate = getHCPBuildInfo();
			setThreadsoldInfo();
//			logger.debug("HCP Build date " + builddate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		;
	}

	public static String getHCPBuildInfo() throws SQLException {
		String query = "select distinct regdate from SCOPUS_2014_HCP_VIEW_1 order by regdate desc";
		String result = "20141001";
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		ConnectionFactory factory = null;
		try {
			factory = ConnectionFactory.getInstance();
			conn = factory.getConnection();
			psmt = conn.prepareStatement(query);
			rs = psmt.executeQuery();
			while (rs.next()) {
				result = rs.getString(1);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally {
			if (factory != null)
				factory.release(rs, psmt, conn);
		}
		return result;
	}

	public static void checkHCP() throws SQLException {
		String regdate = builddate;
		String query = "select eid, cit_count, (ranking*100),  ranking from SCOPUS_2014_HCP_MAIN  where eid = ? and regdate = ?";

		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		ConnectionFactory factory = null;
		try {
			factory = ConnectionFactory.getInstance();
			conn = factory.getConnection();
			for (String eid : eids) {
				psmt = conn.prepareStatement(query);
				psmt.setString(1, eid);
				psmt.setString(2, regdate);
				rs = psmt.executeQuery();
				while (rs.next()) {
					boolean isHCP = false;
					String _eid = rs.getString(1);
					int cit_count = rs.getInt(2);
					//2022.07.22 4건 이하는 Top HCP에서 제거한다.
					if(cit_count <= 4) continue;
					double _ranking = Double.parseDouble(rs.getString(3));
					if (_ranking < 1) {
						isHCP = true;
					}
					logger.debug(_eid + "\t" + cit_count + "\t" + _ranking + "\t" + isHCP);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally {
			if (factory != null)
				factory.release(rs, psmt, conn);
		}
	}

	public static DOC checkHCP(DOC doc) throws SQLException {
		String regdate = builddate;
//		logger.debug("****************HCP BUILD DATE : " + regdate +"\t" + doc.eid);
		String query = "select eid, cit_count, (ranking*100),  ranking, asjc_code from SCOPUS_2014_HCP_MAIN  where eid = ? and regdate = ?";
		// logger.debug("HCP RegDate " + regdate);

		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		ConnectionFactory factory = null;
		try {
			factory = ConnectionFactory.getInstance();
			conn = factory.getConnection();
			psmt = conn.prepareStatement(query);
			psmt.setString(1, doc.eid);
			psmt.setString(2, regdate);
			rs = psmt.executeQuery();
//			logger.debug(query);
			while (rs.next()) {
				boolean isHCP = false;
				String _eid = rs.getString(1);
				int cit_count = rs.getInt(2);
				
				//2022.07.22 4건 이하는 Top HCP에서 제거한다.
				if(cit_count <= 4) continue;
				
//				logger.debug("@@@@@@@@@@@@@ HCPCheck EID : " + _eid + "/ cit " + cit_count);
				double _ranking = Double.parseDouble(rs.getString(3));
				doc.cit_count = String.valueOf(cit_count);
				doc.ranking = String.valueOf(_ranking);
//				logger.debug();
//				if(_eid.equals("85049935958")){
//					System.err.println(_eid + "\t" + cit_count + "\t" + _ranking + "\t" + isHCP);
//					
//				}
				if (_ranking <= 1d) {
					isHCP = true;
					doc.hcpFirstAsjc = rs.getString(5) + " ";
					doc.isFirstHCP = String.valueOf(isHCP).toUpperCase();
					logger.info("HCP 1%" + doc.eid + " : " + doc.hcpFirstAsjc);
					break;
				}
			}

			String isHCP = doc.isFirstHCP.trim();
			if ("".equals(isHCP)) {
				doc.isFirstHCP = "FALSE";
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally {
			if (factory != null)
				factory.release(rs, psmt, conn);
		}
		return doc;
	}

	static Map<String, Integer> hcpThreshold;

	public static void setThreadsoldInfo() {
		hcpThreshold = new HashMap<String, Integer>();
		String query = "select asjc_code, publication_year, MIN(threshold) AS thresold" + " from SCOPUS_2014_HCP_VIEW_1"
				+ " WHERE regdate = ?" + " GROUP BY asjc_code, publication_year"
				+ " ORDER BY ASJC_CODE asc, publication_year DESC";

		PreparedStatement psmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = ConnectionFactory.getInstance().getConnection();
			psmt = conn.prepareStatement(query);
			logger.debug(builddate);
			psmt.setString(1, builddate);
			rs = psmt.executeQuery();
			while (rs.next()) {
				String asjc = rs.getString(1);
				String year = rs.getString(2);
				int _t = rs.getInt(3);
				hcpThreshold.put(asjc + year, _t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionFactory.getInstance().release(rs, psmt, conn);
		}
//		logger.debug("init hcpthreshold info " + builddate + "\t" + hcpThreshold.size());

	}

	protected static boolean check1ProThreshold(String asjc, String year, String threshold) {
		if (hcpThreshold.containsKey(asjc + year)) {
			if (Integer.parseInt(threshold) >= hcpThreshold.get(asjc + year)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 특정년도의 특정 분류의 상위 1% 쓰레숄드값을 가져와서 입력한 쓰레숄드값과 비교한다. <br>
	 * 입력한 쓰레숄드 값이 상위 1%쓰레숄드값보다 크면 true를 리턴한다. <br>
	 * 
	 * @author pc
	 * @date 2015. 6. 15.
	 * @param asjc
	 * @param year
	 * @return
	 * @throws SQLException
	 */
	protected static boolean check1ProThreshold(Connection conn, String asjc, String year, String threshold)
			throws SQLException {
		String regdate = builddate;
		// threshold = "-1";
		// logger.debug(asjc +", " + year +","+regdate + "\t" +
		// threshold);
		String query = "select threshold from SCOPUS_2014_HCP_VIEW_1 where asjc_code = ? and publication_year = ? and regdate = ?";

		PreparedStatement psmt = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			psmt = conn.prepareStatement(query);
			psmt.setString(1, asjc);
			psmt.setString(2, year);
			psmt.setString(3, regdate);
			rs = psmt.executeQuery();
			while (rs.next()) {
				int _t = rs.getInt(1);
				// logger.debug(_t);
				if (Integer.parseInt(threshold) >= _t) {
//					logger.debug("HCP RegDate " + asjc + "\t" + year + "\t" + threshold + "\t" + _t);
					result = true;
				}
				break;

			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rs != null)
				rs.close();
			if (psmt != null)
				psmt.close();
		}
		return result;
	}

	/**
	 * 논문의 나머지 ASJC 분류에 대한 상위 1% 쓰레숄드 여부를 구한다.
	 * 
	 * @author pc
	 * @date 2015. 6. 15.
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static DOC checkASJC(DOC doc) throws Exception {
		// if ("true".equalsIgnoreCase(doc.isTopJ)) {
		// doc.hcpasjc = doc.asjc.replaceAll(" ", ",");
		// return doc;
		// }
		Connection conn = null;
		ConnectionFactory factory = null;
		try {
			factory = ConnectionFactory.getInstance();
			conn = factory.getConnection();
			String[] asjcs = doc.asjc.split(" ");
			String hasjc = "";
			String firstAsjc = doc.hcpFirstAsjc.trim();
			boolean firstIndex = true;
			for (String asjc : asjcs) {
				if (firstAsjc.equals(asjc.trim())) {
					continue;
				}
				// logger.debug(doc.eid + "\t" + doc.cit_count);
				boolean is1Pro = check1ProThreshold(conn, asjc, doc.sortyear, doc.cit_count);
				if (is1Pro) {
					if (firstIndex) {
						hasjc = asjc;
						firstIndex = false;
					} else {
						hasjc = hasjc + "," + asjc;
					}
				}
			}
			doc.hcpSubAsjc = hasjc;
		} catch (Exception e) {
			throw e;
		} finally {
			if (conn != null)
				conn.close();
		}
		return doc;
	}

	/**
	 * 논문의 나머지 ASJC 분류에 대한 상위 1% 쓰레숄드 여부를 구한다.
	 * 
	 * @author pc
	 * @date 2015. 6. 15.
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static DOC checkASJC2(DOC doc) throws Exception {
		String key = doc.asjc + doc.sortyear;
		boolean check1pro = false;
		try {
			String[] asjcs = doc.asjc.split(" ");
			String hasjc = "";
			String firstAsjc = doc.hcpFirstAsjc.trim();
			boolean firstIndex = true;
			for (String asjc : asjcs) {
				if (firstAsjc.equals(asjc.trim())) {
					continue;
				}
				boolean is1Pro = false;
				if (hcpThreshold.containsKey(key)) {
					int threshold = hcpThreshold.get(key);
					int citCnt = 0;
					try {
						citCnt = Integer.parseInt(doc.cit_count);
					} catch (Exception e) {
					}

					if (citCnt >= threshold) {
						is1Pro = true;
					}
				}
				if (is1Pro) {
					check1pro = true;
					if (firstIndex) {
						hasjc = asjc;
						firstIndex = false;
					} else {
						hasjc = hasjc + "," + asjc;
					}
				}
			}
			doc.hcpSubAsjc = hasjc;
		} catch (Exception e) {
			throw e;
		}
		if(check1pro){
			logger.debug("HCP 1% " + doc.eid );
		}
		return doc;
	}

	/**
	 * @author coreawin
	 * @date 2014. 6. 19.
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		HCPCheck.checkHCP();

	}

}
