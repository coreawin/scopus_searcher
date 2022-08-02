package com.diquest.scopus.searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.ExecutorIBSSearch;
import com.diquest.scopus.ExecutorIBSSearch.InputParameter;
import com.diquest.scopus.searchrule.QueryConverterWoS_OLD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;
import com.diquest.scopus.util.LoadResourcese;
import com.diquest.scopus.writer.IBSExcelWriter;

public class IBSSearchAllTextFile {
	Logger logger = LoggerFactory.getLogger(getClass());
	private InputParameter param;
	SearchService ss = new SearchService();
	StringBuffer buffer = new StringBuffer();
	final String ENTER ="\r\n";

	public IBSSearchAllTextFile(InputParameter param) {
		this.param = param;
	}

	public void search() throws Exception {
		String source = param.source;
		File f = new File(source);
		System.out.println(source);
		if (f.isDirectory() && f.exists()) {
			findDir(f, param.target + File.separator + f.getName());
		} else {
			throw new Exception("해당 경로가 존재하지 않습니다.");
		}
		searchTitles(param.target);
		
		System.out.println("param.target : " + param.target);
		
		File file = new File(param.target);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getParentFile() + File.separator + "titles/FIND.DOI.ALL")));
		bw.write(buffer.toString());
		bw.flush();
		bw.close();
//		System.out.println(buffer);
	}

	private List<String> readFile(File f) {
		List<String> list = new LinkedList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if ("".equals(line))
					continue;
				list.add(line);
			}
			System.out.println("LOAD File : " + list.size() + "\t" + f.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;

	}

	private void searchTitles(String target) throws Exception {
		SelectSet[] ss = SearchQueryHelper.getSelectSet();

		Set<String> researchCode = researchText.keySet();
		LinkedList<LinkedHashMap<String, String>> data = new LinkedList<LinkedHashMap<String, String>>();
		String prePersonName = null;
		for (String researchCodeKey : researchCode) {
			System.out.println(researchCodeKey);
			buffer.append(researchCodeKey + ENTER);
			researchCodeKey = researchCodeKey.trim();
			String name = researchCodeKey;
			String keyValue = LoadResourcese.researchMatcher.get(researchCodeKey);
			if (keyValue != null) {
				name += " " + keyValue;
			}
			System.out.println("create excel file " + target);
			IBSExcelWriter writer = new IBSExcelWriter(new File(target, name + ".xlsx"));
			writer.write();
			List<String> titleList = researchText.get(researchCodeKey);
			String personName = null;
			String searchRule = "";
//			System.out.println(name + "\t" + personName);
			int cnt = 1;
			LinkedList<LinkedHashMap<String, String>> nofindTitles = new LinkedList<LinkedHashMap<String, String>>();
			for (String titles : titleList) {
				if (titles.startsWith("@")) {
					//사람이 시작되므로 시트를 생성해야 한다. 없으면 Skip /*20180509 이박사는 데이터가 없어도 시트를 생성해야 한다고 한다.*/
//					if(data.size()>0){
					if(personName!=null){
						System.out.println(researchCodeKey + " [create sheet] " + personName +" #CREATE_SHEET");
						for(LinkedHashMap<String, String> nft : nofindTitles){
							data.add(nft);
						}
						writer.writeWook(data, personName);
						nofindTitles.clear();
						data.clear();
					}
//					}
					personName = titles;
					prePersonName = personName;
					System.out.println(personName);
					buffer.append(personName + ENTER);
					continue;
				}

				 if (titles.trim().length() < 1) {
					 continue;	
				 }
				 
				String[] va = titles.split("\t");
				String doi = null;
				if(va.length > 1){
					doi = va[1].replaceAll("=", " ").trim();
					doi = doi.replaceAll("https://doi\\.org/", "");
					if("".equals(doi)){
						searchRule = "ti=(" + titles.replaceAll("[-,\\.\\(\\)/]", " ").replaceAll("\\s{1,}", " ").replaceAll("=", " ")
								+ ")";
					}else{
						if(doi.endsWith(".")){
							doi = doi.substring(0, doi.lastIndexOf("."));
							System.out.println(va[1] +"\t" + doi);
							titles = va[0] + "\t" + doi;
						}
						if(doi.indexOf("/")!=-1 || doi.indexOf(".")!=-1){
							searchRule = "doi=(" + doi+ ")";
						}else{
							searchRule = "eid=(" + doi+ ")";
						}
						
					}
					
				}else{
					titles = va[0];
					searchRule = "ti=(" + titles.replaceAll("[-,\\.\\(\\)/]", " ").replaceAll("\\s{1,}", " ").replaceAll("=", " ")
							+ ")";
				}
				
				searchRule = searchRule.replaceAll("#", "").replaceAll("\\s{1,}", " ").trim();
				QueryConverterWoS_OLD qc = new QueryConverterWoS_OLD(searchRule);
				WhereSet[] ws = qc.getWhereSet();
				// WhereSet[] ws = new WhereSet[]{
				// new WhereSet(SCOPUS_MARINER_FIELD.TITLE_E.getIndexField(),
				// Protocol.WhereSet.OP_HASANY, titles, 150)
				// };
				QuerySet qs = new QuerySet(1);
				Query query = new Query();
				query.setSearchOption(Protocol.SearchOption.CACHE | Protocol.SearchOption.STOPWORD
						| Protocol.SearchOption.BANNED | Protocol.SearchOption.PHRASEEXACT);
				query.setThesaurusOption(Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM);
				query.setSelect(ss);
				query.setWhere(ws);
				query.setFrom(ExecutorIBSSearch.marinerCollection);
				// query.setSearch(true);
				// query.setDebug(true);
				// query.setFaultless(true);
				query.setPrintQuery(true);
				// query.setLoggable(true);
				query.setResult(0, 10);
				qs.addQuery(query);
				// System.out.println(query);
				ResultSet rs = this.ss.requestSearch(ExecutorIBSSearch.marinerIP, ExecutorIBSSearch.marinerPort, qs);
				if (rs == null) {
//					System.err.println(name + "] row number : " + (cnt++) + " , SEARCH RULE : " + searchRule);
					continue;
				}
				Result[] rArr = rs.getResultList();
				for (int resIndex = 0; resIndex < rArr.length; resIndex++) {
					Result rInfo = rArr[resIndex];
					int totalSize = rInfo.getTotalSize();
					int realSize = rInfo.getRealSize();
					if (totalSize > realSize) {
						continue;
					}
					LinkedHashMap<String, String> dd = new LinkedHashMap<String, String>();
					if (totalSize > 0) {

						for (int rr = 0; rr < realSize; rr++) {
							dd = new LinkedHashMap<String, String>();
							String author = new String(rInfo.getResult(rr, 0));
							String title = new String(rInfo.getResult(rr, 1));
							String year = new String(rInfo.getResult(rr, 2));
							String docType = new String(rInfo.getResult(rr, 3));
							String eid = new String(rInfo.getResult(rr, 4));
							String firstAu = author.split(";", -1)[0];
							String corrAuthor = new String(rInfo.getResult(rr, 5));
							doi = new String(rInfo.getResult(rr, 6));

							/*dd.put("AU", author);
							dd.put("title", title);
							dd.put("titles", va[0]);
							dd.put("eq", va[0].equalsIgnoreCase(title) + "");
							dd.put("year", year);
							dd.put("docType", docType);
							dd.put("eid", eid);
							dd.put("corrAuthor", corrAuthor);
							dd.put("firstAu", firstAu);*/
							
							
							dd.put("AU", author);
							dd.put("title", title);
							dd.put("year", year);
							dd.put("docType", docType);
							dd.put("eid", eid);
							dd.put("11", "");
							dd.put("22", "");
							dd.put("firstAu", firstAu);
							dd.put("corrAuthor", corrAuthor);
							
							data.add(dd);
							System.out.println("row number : " + (cnt++) + " , SEARCH @"
									+ titles.replaceAll("\t", " ") + "\t" + doi);
							buffer.append(titles.replaceAll("\t", " ") + "\t" + doi + ENTER);
							break;
						}
					} else {
						dd = new LinkedHashMap<String, String>();
						dd.put("AU", "");
						dd.put("title", titles);
						dd.put("year", "");
						dd.put("docType", "");
						dd.put("eid", "");
						dd.put("11", "");
						dd.put("22", "");
						dd.put("firstAu", "");
						dd.put("corrAuthor", "");
						nofindTitles.add(dd);
						System.err.println("no result " + (cnt++) + " , SEARCH @" + titles.replaceAll("\t", " "));
						buffer.append("# " + titles.replaceAll("\t", " ") + ENTER);
						// System.err.println(name + "] no result " + (cnt++) +
						// " , SEARCH RULE : " + searchRule);
						// System.exit(0);
					}
				}
			}
			
			if(data.size()>0 || nofindTitles.size() > 0){
//				System.out.println(researchCodeKey + " [create sheet] " + personName +" #CREATE_SHEET");
				for(LinkedHashMap<String, String> nft : nofindTitles){
					data.add(nft);
				}
				writer.writeWook(data, personName);
				nofindTitles.clear();
				data.clear();
			}
			writer.write();
		}
	}

	private void findDir(File f, String target) throws Exception {
		File targetF = new File(target);
		if (!targetF.exists()) {
			targetF.mkdirs();
		}
		File[] subFile = f.listFiles();
		if (subFile == null) {
			return;
		}
		Pattern p = Pattern.compile("^(R[0-9]{3})");
		for (File subF : subFile) {
			if (subF.isDirectory() && subF.exists()) {
				findDir(subF, target + File.separator + subF.getName());
			} else if (subF.isFile() && subF.exists()) {
				String name = subF.getName();
				if (name.endsWith(".ALL")) {
//					System.out.println(subF.getName());
					List<String> allFileContents = readFile(subF);
					LinkedList<String> list = new LinkedList<String>();
					String preResearchCode = null;
					for (String str : allFileContents) {
						Matcher m = p.matcher(str);
						String researchMatcher = str.replaceAll("^(R[0-9]{3})", "[FIRST LINE]").trim();
						while(m.find()){
							researchMatcher = "[FIRST LINE]";
//							str = researchMatcher;
							break;
						}
//						System.out.println(researchMatcher);
						if (researchMatcher.equalsIgnoreCase("[FIRST LINE]")) {
							if (list.size() > 0) {
							if(preResearchCode!=null){
								researchText.put(preResearchCode, list);
							}
							}
							preResearchCode = str;
							list = researchText.get(str);
							if (list == null) {
								list = new LinkedList<String>();
							}
							continue;
						}
//						System.out.println("== " + str);
						list.add(str);
					}
					System.out.println("오로지 한개의 .ALL 파일만 지원한다.");
					if (list.size() > 0) {
						researchText.put(preResearchCode, list);
					}
					break;
				}
			}
		}
		Set<String> ks = researchText.keySet();
		for(String key : ks){
			LinkedList<String> list = researchText.get(key);
			for(String _s : list){
				if(_s.startsWith("@")){
					System.out.println(key +"\t" + _s);
				}
			}
		}
		
	}

	public Map<String, LinkedList<String>> researchText = new LinkedHashMap<String, LinkedList<String>>();
	// public LinkedList<File> allFiles = new LinkedList<File>();
}
