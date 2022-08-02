package com.diquest.scopus.searcher;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.analysis.HCPCheck;
import com.diquest.scopus.analysis.SrcIDReader;
import com.diquest.scopus.analysis.TestScopusSearch.DOC;
import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import com.diquest.scopus.searchrule.QueryConverterException;
import com.diquest.scopus.searchrule.QueryConverterWoS_OLD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;
import com.diquest.scopus.util.ExcelSheetBean;
import com.diquest.scopus.util.ReadExcel;

/**
 * R 연구단.<br>
 * EID는 안다.<br>
 * 1저자 및 교신저자 를 찾아야 하고, 이 경우에만 HCP 체크 진행한다.<br>
 * EID 목록이 주어져 있다. 이걸 가지고 찾아야 함. <br>
 * 교신저자 찾는건 수동으로 찾아야 함.
 * 
 * @author 정승한
 * @date 2018. 8. 5.
 */
public class IBSSearchR {
	static FileOutputStream fs = null;
	static SXSSFWorkbook workbook;
	static SXSSFSheet sheet = null;

	static CellStyle style = null;
	static CellStyle 입사style = null;
	SearchService searchService = new SearchService();

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

	public static SelectSet[] getSelectSet() {
		SelectSet[] selectSet = new SelectSet[] {
				new SelectSet(SCOPUS_MARINER_FIELD.EID.name(), (byte) Protocol.SelectSet.NONE),
				new SelectSet("AUTHOR_NAME", (byte) Protocol.SelectSet.NONE),
				new SelectSet("CR_NAME", (byte) Protocol.SelectSet.NONE),
				new SelectSet("ASJC", (byte) Protocol.SelectSet.NONE), new SelectSet("SRCID", (byte) Protocol.SelectSet.NONE), 
				new SelectSet(SCOPUS_MARINER_FIELD.SORTYEAR.name(), (byte) Protocol.SelectSet.NONE), 		
				new SelectSet("CIT_COUNT", (byte) Protocol.SelectSet.NONE),
		};
		return selectSet;
	}

	public DOC search(String searchRule) throws IRException {
		DOC doc = new DOC();
//		System.out.println("searchRule " + searchRule);
		QueryConverterWoS_OLD qc = null;
		try {
			qc = new QueryConverterWoS_OLD(searchRule);
		} catch (QueryConverterException e1) {
			e1.printStackTrace();
		}
		WhereSet[] ws = qc.getWhereSet();
		// for(WhereSet _w : ws){
		// System.out.println(_w);
		// }
		//
		QuerySet qs = new QuerySet(1);
		Query query = new Query();
		query.setSelect(getSelectSet());
		query.setWhere(ws);
		query.setFrom("SCOPUS_2016");

		query.setSearch(true); // 검색 여부 설정
		query.setResultCutOffSize(2);
		query.setFaultless(true);
		query.setPrintQuery(false);
		query.setIgnoreBrokerTimeout(true);
		query.setSearchOption(Protocol.SearchOption.PHRASEEXACT | Protocol.SearchOption.CACHE);
		query.setResult(0, 1);

		qs.addQuery(query);
		// ResultSet rs = searchService.requestSearch("203.250.207.72", 5555,
		// qs);
		String ip = "203.250.207.72";
		int port = 5555;
		ResultSet rs = null;
		SearchQueryHelper.setCommandSearchRequestProps(ip, port);
		CommandSearchRequest cmd = new CommandSearchRequest(ip, port);
		// logger.info("{}:{}", ip, port);
		try {
			int returnCode = cmd.request(qs);
			// System.out.println("returnCode " + returnCode );
			if (returnCode >= 0) {
				rs = cmd.getResultSet();
			}
		} catch (IRException e) {
			throw e;
		}

		Result[] rArr = rs.getResultList();
		for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
			Result rInfo = rArr[resIndex];
			int totalSize = rInfo.getTotalSize();
			int realSize = rInfo.getRealSize();
			if (totalSize > realSize) {
				continue;
			}
			if (totalSize == 1) {
				for (int rr = 0; rr < realSize; rr++) {
					LinkedHashMap<String, String> dd = new LinkedHashMap<String, String>();
					int sIdx = 0;
					String eid = new String(rInfo.getResult(rr, sIdx++));
					String fAuthor = new String(rInfo.getResult(rr, sIdx++));
					String cAuthor = new String(rInfo.getResult(rr, sIdx++));
					doc.eid = eid;
					doc.authorname = fAuthor;
					doc.crname = cAuthor;
					doc.asjc = new String(rInfo.getResult(rr, sIdx++));
					doc.srcid = new String(rInfo.getResult(rr, sIdx++));
					doc.sortyear = new String(rInfo.getResult(rr, sIdx++));
					doc.cit_count = new String(rInfo.getResult(rr, sIdx++));
//					System.out.println("search result eid " + eid +"\tcit_cout: " + doc.cit_count);
					doc.isTopJ = journalIDSet.contains(doc.srcid) ? "TRUE" : "FALSE";
					doc.srcFirstAsjc = journalIDAscjMap.get(doc.srcid) != null ? journalIDAscjMap.get(doc.srcid) : "";
					try {
						doc = HCPCheck.checkASJC2(doc);

					} catch (Exception e) {
						e.printStackTrace();
					}
					// System.out.println(eid + "\t]:" + fAuthor + "\t]:" +
					// cAuthor + "," + doc.hcpSubAsjc + "\t" + doc.isTopJ);
				}
			} else {
			}
		}
		return doc;
	}

	public static void main(String[] args) {
		IBSSearchR r = new IBSSearchR();
		String readFileName = "R001_인지 및 사회성 연구단1.xlsx";
		String readPath = "c:/Users/정승한/OneDrive - hansung.ac.kr/2018.KISTI/10.IBS/R연구단/180731-R연구단/";
		readPath = "c:/Users/정승한/OneDrive - hansung.ac.kr/2018.KISTI/10.IBS/R연구단/180731-R연구단-1% 교신저자만 체크/";
		
		String writePath = "d:\\data\\scopus\\R연구단2\\";
//		writePath = "d:/data/";

		File dir = new File(readPath);
		File[] files = dir.listFiles();
		SortedMap<String, TreeSet<String>> isCheck1 = new TreeMap<String, TreeSet<String>>();
		for(File f : files){
			if(f.getName().endsWith(".xlsx")){
				System.out.println("read file Name : " + f.getAbsolutePath());
				
				ReadExcel re = new ReadExcel();
				LinkedHashMap<String, LinkedList<LinkedList<String>>> map = new LinkedHashMap<String, LinkedList<LinkedList<String>>>();
				TreeSet<String> isCheckedSet = new TreeSet<String>();
				
				try {
					LinkedHashMap<String, ExcelSheetBean> treeMap = re.readExcelFile(f, 2);
					Set<String> set = treeMap.keySet();
					for (String sheetName : set) {
						
						ExcelSheetBean esb = treeMap.get(sheetName);
						LinkedList<LinkedList<String>> newData = new LinkedList<LinkedList<String>>();
						LinkedList<String[]> rowdata = esb.getSheetData();
						boolean 입사전1프로 = false;
						boolean 입사후1프로 = false;
						for (String[] _rows : rowdata) {
							try{
							LinkedList<String> clms = new LinkedList<String>();
							int idx = 0;
							for (String _e : _rows) {
								if(idx!=23){
									clms.add(_e);
//									System.out.println("_e " + _e);
								}
								idx++;
							}
							String eid = _rows[19];
							String firstRnp = _rows[21];
//							System.out.println(firstRnp);
							if(eid!=null) eid = eid.trim();
							if("".equals(eid)) eid = _rows[18].replaceAll("2-s2.0-", "");
							/*64이면 입사전 0이면 입사후*/
							String 입사전여부 = _rows[_rows.length-1];
//							System.out.println("입사전여부 " + 입사전여부);
							DOC doc = r.search("EID=(" + eid + ")");
							if("3".equals(입사전여부)){
								doc.입사여부 = "입사";
							}
							boolean is1Pro = false;
							if (!"".equalsIgnoreCase(doc.hcpSubAsjc.trim())) {
								// TOP 1Pro
								is1Pro = true;
							}
//							if(!is1Pro) continue;
//							clms.add(firstRnp);
//							clms.add(doc.isTopJ); /*임시 주석처리*/
							clms.add(doc.authorname.split("\n")[0] + " | " + doc.crname);
							clms.add(String.valueOf(is1Pro==true?"HCP 1% - "+doc.입사여부:""));
							String ABC = "";
							if(!"".equals(firstRnp) && "true".equalsIgnoreCase(doc.isTopJ.trim())){
								if("입사".equals(doc.입사여부)){
									ABC = "+";
									입사후1프로 = true;
								}else{
									ABC = "- ";
									입사전1프로 = true;
								}
							}
							clms.add(ABC);
							clms.add(입사전여부);	
//							for(String _c : clms){
//								System.out.println(idx1++ + "===> " + _c);
//							}
							newData.add(clms);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						isCheckedSet.add(sheetName+"\t"+(입사전1프로==true?"true":"")+"\t" + (입사후1프로==true?"true":""));
						map.put(sheetName, newData);
//						System.out.println("write txt file : " + f.getName() + ".txt");
//						BufferedWriter bw = null;
//						try {
//							bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(writePath + f.getName() + ".txt")),
//									"UTF-8"));
//							for (LinkedList<String> arows : newData) {
//								for (String a : arows) {
//									bw.write(a.replaceAll("(\\s{1,}|\\n{1,})", " "));
//									bw.write("\t");
//								}
//								bw.write("\r\n");
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						} finally {
//							if (bw != null)
//								bw.close();
//						}
					}
					System.out.println("write excel file : " + f.getName() + "_ouput.xlsx");
					
					writeExcel(new File(writePath +f.getName()+"_ouput.xlsx"), map);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isCheck1.put(f.getName(), isCheckedSet);
				//break;
			}
		}
		Set<String> sets = isCheck1.keySet();
		for(String k : sets){
			System.out.println(k);
			TreeSet<String> vs = isCheck1.get(k);
			for(String _v : vs){
				System.out.println(_v);
			}
		}
		
	}

	public static void writeExcel(File writeFile, LinkedHashMap<String, LinkedList<LinkedList<String>>> map) throws IOException {
		XSSFRow row;
		XSSFCell cell;
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook();
			createHeaderStyle(workbook);
			create입사Style(workbook);
			
			Set<String> keySet = map.keySet();
			for (String sheetName : keySet) {
				LinkedList<LinkedList<String>> newData = map.get(sheetName);
				XSSFSheet newSheet = workbook.createSheet(sheetName);
				int rowIdx = 0;
				int columnIdx = 0;
				String[] header = new String[] { "Authors", "Title", "Year", "Source title", "Volume", "Issue", "Art. No.",
						"Page start", "Page end", "Page count", "Cited by", "DOI", "Link", "Affiliations",
						"Authors with affiliations", "Document Type", "Access Type", "Source", "EID", "EID1", "Journal 1% Journal ID",
						"1st or RP", "저널1%여부","1저자및교신저자명", "HCP 여부" };
				row = newSheet.createRow(rowIdx++);
				for (int idx = 0; idx < header.length; idx++) {
					cell = row.createCell(idx);
					cell.setCellValue(header[idx]);
					cell.setCellStyle(style);
				}

				for (LinkedList<String> arows : newData) {
					row = newSheet.createRow(rowIdx++);
					columnIdx = 0;
					String last = arows.removeLast();
					for (String a : arows) {
//						if(columnIdx==23) a= "";
						cell = row.createCell(columnIdx++);
						if("3".equals(last)){
							cell.setCellStyle(입사style);
						}
//						if(columnIdx > 25) a = "";
						cell.setCellValue(a);
//						System.out.println(columnIdx + "\t" + a);
					}
//					System.out.println("===");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		FileOutputStream outstream = null;
		try {
			outstream = new FileOutputStream(writeFile);
			workbook.write(outstream);
			outstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outstream != null) {
				outstream.flush();
				outstream.close();
			}
		}
		System.out.println("write File " + writeFile.getCanonicalPath());
	}

	public void flush() {
		try {
			workbook.write(fs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public void close() {
		if (fs != null)
			try {
				flush();
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void createHeaderStyle(XSSFWorkbook workbook) {
		style = workbook.createCellStyle();
		style.setWrapText(true);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	}
	
	private static void create입사Style(XSSFWorkbook workbook) {
		입사style = workbook.createCellStyle();
		입사style.setFillForegroundColor((short)3); 
		입사style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND); 
	}

}
