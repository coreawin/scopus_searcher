package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.diquest.scopus.util.ExcelSheetBean;
import com.diquest.scopus.util.ReadExcel;

public class CPCUploader {
	private String jdbcurl = null;
	private String user = null;
	private String pwd = null;

	public CPCUploader() {
		jdbcurl = "jdbc:tibero:thin:@203.250.206.62:8639:KIST02";
		user = "lexispatent";
		pwd = "Lexispatent+0610";
		try {
			Class.forName("com.tmax.tibero.jdbc.TbDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			conn = getConnection();
			psmt = conn.prepareStatement(query);
			System.out.println("psmt " + psmt);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(jdbcurl, user, pwd);
		con.setAutoCommit(false);
		return con;
	}

	Connection conn = null;
	PreparedStatement psmt = null;

	String query = "insert into LEXISPATENT.KLN_PM_CPC_DESCRIPTION2 (CPC_CODE, DESCRIPTION, DESCRIPTION_KOR,IPC_CODE_MATCH,DOT_NUMBER)  values (?, ?, ?, ?, ?)";
	int batchcnt = 0;
	public void insertBatch(String... abc) {
		try {
			int idx = 0;
			psmt.setString(1, abc[idx++]);
			psmt.setString(2, abc[idx++]);
			psmt.setString(3, abc[idx++]);
			psmt.setString(4, abc[idx++]);
			psmt.setString(5, abc[idx++]);
//			psmt.executeUpdate();
			psmt.addBatch();
			psmt.clearParameters();
			batchcnt++;
			if(batchcnt%1000==0 && batchcnt >0){
//				System.out.println(batchcnt +"\t 진행중...." + psmt);
				batchcnt = 0;
				psmt.executeBatch();
			}
		} catch (Exception e) {
//			System.out.println(abc[0]);
//			System.out.println(abc[1]);
//			System.out.println(abc[2]);
//			System.out.println(abc[3]);
//			System.out.println(abc[4]);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			System.exit(-1);
		}
	}
	
	public int[] executeBatch() throws SQLException{
		if(batchcnt>1){
			psmt.executeBatch();
			batchcnt = 0;
		}
		conn.commit();
		return new int[0];
	}
	
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		CPCUploader cpcloader = new CPCUploader();
		ReadExcel re = new ReadExcel();
		String path = "z:/NLP그룹/10.프로젝트/2018/KISTI/IPC CPC/CPC/CPC 분류표(국영문).xlsx";
		LinkedHashMap<String, ExcelSheetBean> excels = re.readExcelFile(new File(path), 9);
		System.out.println("BBBBBBB");
		Set<String> keys = excels.keySet();
		int cnt = 0;
		for (String k : keys) {
			ExcelSheetBean esb = excels.get(k);
			LinkedList<String[]> datas = esb.getSheetData();
			for (String[] rows : datas) {
				int idx = 0;
				String cpccode = rows[idx++];
				String dot = rows[idx++];
				String eng = rows[idx++];
				String kor = rows[idx++];
				String blank1 = rows[idx++];
				String ipcmatch = rows[idx++];
				if("".equals(cpccode.trim())) continue;
				cpcloader.insertBatch(cpccode, eng, kor, ipcmatch, dot);
				if(cnt%1000==0){
					System.out.println(cnt +"\t 진행중....");
				}
				cnt ++;
			}
		}
		cpcloader.executeBatch();
		System.out.println(cnt +"\t  ABC 9완료....");
	}

}
