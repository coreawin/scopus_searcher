package com.diquest.scopus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class LoadResourcese {

	private static String researchMatcherFile = "../NEON_ECLIPSE_KISTI_SCOPUS_SEARCHER/data/research_matching.txt";

	public static HashMap<String, String> researchMatcher = new HashMap<String, String>();

	static{
		try {
			loadResearchMatcher();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadResearchMatcher() throws Exception {
		File f = new File(researchMatcherFile);
		System.out.println(f.getAbsolutePath());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f),
				"UTF-8"));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if ("".equals(line))
				continue;
			String code = line.substring(0, line.indexOf(" "));
			String value = line.substring(line.indexOf(" ") + 1, line.length());
			researchMatcher.put(code, value);
			System.out.println("load research matcher " + code +" " + value);
		}
		br.close();
	}

	public static void main(String[] args) {
		new LoadResourcese();

	}

}
