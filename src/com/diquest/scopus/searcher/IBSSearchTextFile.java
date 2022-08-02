package com.diquest.scopus.searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.scopus.ExecutorIBSSearch.InputParameter;
import com.diquest.scopus.searchrule.QueryConverterWoS_OLD;
import com.diquest.scopus.searchrule.SearchQueryHelper;
import com.diquest.scopus.searchrule.SearchService;
import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;
import com.diquest.scopus.util.LoadResourcese;
import com.diquest.scopus.writer.IBSExcelWriter;

public class IBSSearchTextFile {
	Logger logger = LoggerFactory.getLogger(getClass());
	private InputParameter param;
	SearchService ss = new SearchService();
	

	public IBSSearchTextFile(InputParameter param) {
		this.param = param;
	}

	public void search() throws Exception {
		String source = param.source;
		File f = new File(source);
		if (f.isDirectory() && f.exists()) {
			findDir(f, param.target + File.separator + f.getName());
		} else {
			throw new Exception("해당 경로가 존재하지 않습니다.");
		}
		searchTitles(param.target);
	}

	private List<String> readFile(File f) {
		List<String> list = new LinkedList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if("".equals(line)) continue;
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

		Set<String> fileKey = txtFiles.keySet();

		LinkedList<LinkedHashMap<String, String>> data = new LinkedList<LinkedHashMap<String, String>>();
		for (String fileNameKey : fileKey) {
			String name = fileNameKey;
			String keyValue = LoadResourcese.researchMatcher.get(fileNameKey);
			if (keyValue != null) {
				name += " " + keyValue;
			}
			IBSExcelWriter writer = new IBSExcelWriter(new File(target, name +".xlsx"));
			List<File> file = txtFiles.get(fileNameKey);
			data.clear();
			for (File _file : file) {
				List<String> list = readFile(_file);
				String searchRule = "";
				int cnt = 1;
				for (String titles : list) {
					// if (titles.trim().length() < 1) {
					// continue;
					// }
					searchRule = "ti=(" + titles.replaceAll("[-,\\.\\(\\)/]", " ").replaceAll("\\s{1,}", " ").replaceAll("=", " ")+ ")";
					QueryConverterWoS_OLD qc = new QueryConverterWoS_OLD(searchRule);
					WhereSet[] ws = qc.getWhereSet();
//					WhereSet[] ws = new WhereSet[]{
//							new WhereSet(SCOPUS_MARINER_FIELD.TITLE_E.getIndexField(), Protocol.WhereSet.OP_HASANY, titles, 150)
//					};
					QuerySet qs = new QuerySet(1);
					Query query = new Query();
					query.setSearchOption(Protocol.SearchOption.CACHE | Protocol.SearchOption.STOPWORD
							| Protocol.SearchOption.BANNED | Protocol.SearchOption.PHRASEEXACT);
					query.setThesaurusOption(Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM);
					query.setSelect(ss);
					query.setWhere(ws);
					query.setFrom("SCOPUS_2016");
//					query.setSearch(true);
//					query.setDebug(true);
//					query.setFaultless(true);
					query.setPrintQuery(true);
//					query.setLoggable(true);
					query.setResult(0, 10);
					qs.addQuery(query);
//					System.out.println(query);
					ResultSet rs = this.ss.requestSearch("203.250.207.72", 5555, qs);
					if (rs == null) {
						System.err.println(name + "] row number : " + (cnt++) + " , SEARCH RULE : " + searchRule);
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
						if (totalSize > 0) {
							for (int rr = 0; rr < realSize; rr++) {
								LinkedHashMap<String, String> dd = new LinkedHashMap<String, String>();
								String author = new String(rInfo.getResult(rr, 0));
								String title = new String(rInfo.getResult(rr, 1));
								String year = new String(rInfo.getResult(rr, 2));
								String docType = new String(rInfo.getResult(rr, 3));
								String eid = new String(rInfo.getResult(rr, 4));
								String firstAu = author.split(";", -1)[0];
								String corrAuthor = new String(rInfo.getResult(rr, 5));
								String doi = new String(rInfo.getResult(rr, 6));

								dd.put("AU", author);
								dd.put("title", title);
//								dd.put("titles", titles);
//								dd.put("eq", titles.equalsIgnoreCase(title)+"");
								dd.put("year", year);
								dd.put("docType", docType);
								dd.put("eid", eid);
								dd.put("11", "");
								dd.put("22", "");
								dd.put("firstAu", firstAu);
								dd.put("corrAuthor", corrAuthor);
								dd.put("doi", doi);
								data.add(dd);
								System.out.println(name + "] row number : " + (cnt++) + " , SEARCH @" + titles.replaceAll("\t", " ") + "\t" + doi);
								
								break;
							}
						} else {
							System.err.println(name + "] no result " + (cnt++) + " , SEARCH @" + titles.replaceAll("\t", " ") +"");
//							System.exit(0);
						}
					}
				}
				writer.writeWook(data, _file.getName());
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
		for (File subF : subFile) {
			if (subF.isDirectory() && subF.exists()) {
				findDir(subF, target + File.separator + subF.getName());
			} else if (subF.isFile() && subF.exists()) {
				String name = subF.getName();
				String dirName = subF.getParentFile().getName();
				if (name.endsWith(".txt")) {
					System.out.println("load : " + dirName + "\t" + name);
					LinkedList<File> list = txtFiles.get(dirName);
					if (list == null) {
						list = new LinkedList<File>();
					} else {
						list.add(subF);
					}
					txtFiles.put(dirName, list);
				}
			}
		}
	}

	public Map<String, LinkedList<File>> txtFiles = new LinkedHashMap<String, LinkedList<File>>();
}
