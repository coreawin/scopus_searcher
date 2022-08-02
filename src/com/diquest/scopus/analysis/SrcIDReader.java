package com.diquest.scopus.analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SrcIDReader {
	BufferedReader br;
	private String path;

	public SrcIDReader(String path) throws Exception {
		this.path = path;

	}

	public Set<String> readId() {
		Set<String> srcID = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(SrcIDReader.class.getResourceAsStream(path), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				srcID.add(line.trim());
			}
			return srcID;
		} catch (Exception e) {
			e.printStackTrace();
			return srcID;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public Map<String, String> readSrcFirstASJC() {
		Map<String, String> srcAsjc = new HashMap<String, String>();
		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(SrcIDReader.class.getResourceAsStream(path), "UTF-8"));
			while ((line = br.readLine()) != null) {
				line = line.trim();
				System.out.println(line);
				String[] lineAr = line.split("\t", -1);
				String srcID = lineAr[3];
				if (lineAr.length == 5) {
					String srcAs = lineAr[4];
					srcAsjc.put(srcID, srcAs);
				} else {
					srcAsjc.put(srcID, "");
				}
			}
			System.out.println(srcAsjc);
		} catch (Exception e) {
			System.out.println(line);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return srcAsjc;
	}

	public Map<String, String> readIssnInfo() {
		Map<String, String> srcAsjc = new HashMap<String, String>();
		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(SrcIDReader.class.getResourceAsStream(path), "UTF-8"));
			while ((line = br.readLine()) != null) {
				String[] lineAr = line.split("\t", -1);
				String srcID = lineAr[2];
				String srcAs = lineAr[5];
				srcAsjc.put(srcID, srcAs);
			}
			System.out.println(srcAsjc);
		} catch (Exception e) {
			System.out.println(line);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return srcAsjc;
	}

	public static void main(String[] args) throws Exception {
		String issn = "1098-4402 0168-9002 1475-7516 1087-0156 1672-9072 1087-0156 2045-2322 0962-1083 2045-2322 2330-4022 1098-0121 0003-2700 1948-7185 1530-6984 1387-2877 1550-7998 1466-8033 1463-9076 1089-5639 1529-2908 2045-2322 0908-665X 0022-1007 1567-1739 2211-2855 0935-9648 2041-1723 1996-1944 2045-2322 1936-0851 0021-8979 1533-4880 1944-8244 1094-4087 1098-0121 1936-0851 2045-2322 1944-8244 0021-2172 1073-7928 1073-2780 1931-4523 1098-0121 2296-424X 0036-8075";
		SrcIDReader r = new SrcIDReader("E:\\project\\2014\\KISTI_SCOPUS_2014_PLATFORM\\ibs\\topJournalASJCInfo.txt");
		String[] issns = issn.split("([\\s]{1,})");
		System.out.println(r.readSrcFirstASJC());
		Map<String, String> srcAsjc = r.readIssnInfo();
		System.out.println(srcAsjc);
		for (String i : issns) {
			if (srcAsjc.containsKey(i)) {
				System.out.println(i + "\t" + srcAsjc.get(i)+"_j");
			} else {
				System.out.println(i + "\t" + "");
			}

		}
	}
}
