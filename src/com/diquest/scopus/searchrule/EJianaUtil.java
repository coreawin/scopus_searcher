package com.diquest.scopus.searchrule;

import java.io.File;

import com.diquest.ejiana.tagger.EJianaBuffer;
import com.diquest.ejiana.tagger.EJianaConst;
import com.diquest.ejiana.tagger.PosTagger;
import com.diquest.ejiana.tagger.TagSet;

/**
 * 영문 형태소 분석을 하면 아래 태깅된 용어는 색인에서 제외되므로 해당 필드에 검색시에는 해당 용어로 검색하면 안된다. //50~ 기능품사
 * public static final byte CC = 50; // Coordinating conjunction <br>
 * public static final byte DT = 51; // Determiner<br>
 * public static final byte EX = 52; // Existential there<br>
 * public static final byte IN = 53; // Preposition or subordinating conjunction
 * <br>
 * public static final byte LS = 54; // List item marker<br>
 * public static final byte POS = 55; // Possessive ending<br>
 * public static final byte PRP = 56; // Personal pronoun<br>
 * public static final byte PRP$ = 57; // Possessive pronoun<br>
 * public static final byte SYM = 58; // Symbol<br>
 * public static final byte TO = 59; // to<br>
 * public static final byte ETC1 = 60; // ".",<br>
 * public static final byte ETC2 = 61; // "\"",<br>
 * public static final byte ETC3 = 62; // "#",<br>
 * public static final byte ETC4 = 63; // "$",<br>
 * public static final byte ETC5 = 64; // ":",<br>
 * public static final byte ETC6 = 65; // "''",<br>
 * public static final byte ETC7 = 66; // "``",<br>
 * public static final byte ETC8 = 67; // "(",<br>
 * public static final byte ETC9 = 68; // ")",<br>
 * public static final byte ETC10 = 69; // ","<br>
 * public static final byte ETC11 = 70; // "--"<br>
 * 
 * @author pc
 * @date 2016. 10. 20.
 */
public class EJianaUtil {

	private static EJianaUtil instance;
	PosTagger tagger = null;
	EJianaBuffer buffer = null;
	private String _home;

	public EJianaUtil() {
		if(true) return;
//		_home = UserUsePlatformDefinition.COMMON_SETTING_PROPETIES.getProperty("EJIANA_HOME");
		if (_home == null) {
			_home = System.getProperty("EJIANA_HOME");
			if (_home == null) {
				_home = System.getenv("EJIANA_HOME");
			}
		}

		if (_home == null) {
			_home = System.getenv("IR4_HOME");
			if (_home == null) {
				_home = System.getProperty("IR4_HOME");
			}
		}

		if (_home == null) {
			_home = "./";
		} else {
			_home = _home + File.separator;
		}

		// if (_home == null) {
		// System.out.println("Ejiana Home이 설정되어 있지 않습니다.");
		// }

		// 분석 메인 모듈
		tagger = new PosTagger();
		// 입출력 버퍼
		buffer = new EJianaBuffer();
		// tagger 초기화
		tagger.init(_home, EJianaConst.CN_FLAG);
	}

	// public static EJianaUtil getInstance() {
	// if (instance == null) {
	// instance = new EJianaUtil();
	// }
	// return instance;
	// }

	public String getStem(String input) {
		if(true) return input;
		String[] inputs = input.split(" ");
		StringBuilder buf = new StringBuilder();
		for (String i : inputs) {
			/**
			 * [2017.02.08] By 이관재 <br/>
			 * - CJK(한자,일본어, 한글) 검색어가 추가로 들어 갔을때 Ejiana에서 SYM 태그로 추출되어 나타나는 현상을
			 * 발견.<br/>
			 * - 검색어에 영문이 포함되어 있다면 형태소 분석기를 통과하여 처리하게 한다.
			 */
			if (i.matches("(.*[a-zA-ZＡ-Ｚａ-ｚ]{1,}.*)")) {
				buffer.init(i.toCharArray());
				// 분석 실행
				tagger.analyze(buffer);
				// 출력 - 출력 buffer의 사용 방법은 printResult 메소드 참고
				String t = printResult(buffer);
				if ("".equals(t))
					continue;
				buf.append(t);
			} else {
				buf.append(i);
			}
			buf.append(" ");
		}
		return buf.toString().toLowerCase().replaceAll("\\(\\)", "").trim();
	}

	private String printResult(EJianaBuffer buffer) {
		if(true) return "";
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < buffer.nTerm; i++) {
			String tag = TagSet.tagStrings[buffer.termTag[i]];
			String terms = new String(buffer.input, buffer.termStart[i], buffer.termLength[i]);
			String stem = new String(buffer.stem, buffer.stemStart[i], buffer.stemLength[i]).trim();
//			System.out.println(terms + "\t" + stem + "\t" + tag);
			// if("a".equalsIgnoreCase(terms) | "the".equalsIgnoreCase(terms) |
			// "she".equalsIgnoreCase(terms)){
			// //logger.debug("!2016.10.25 이 단어들은 조합되서 들어올때 형태소 분석 되어서 들어오면
			// 안된다.");
			// buf.append(terms);
			// continue;
			// }
			if ("CC".equalsIgnoreCase(tag) || "DT".equalsIgnoreCase(tag) || "EX".equalsIgnoreCase(tag) || "IN".equalsIgnoreCase(tag)
					|| "LS".equalsIgnoreCase(tag) || "POS".equalsIgnoreCase(tag) || "PRP".equalsIgnoreCase(tag) || "PRP$".equalsIgnoreCase(tag)
					|| "SYM".equalsIgnoreCase(tag) || "TO".equalsIgnoreCase(tag) || "RB".equalsIgnoreCase(tag) || "RBS".equalsIgnoreCase(tag)
					|| "RBR".equalsIgnoreCase(tag) || "WDT".equalsIgnoreCase(tag)) {
				if (buf.length() > 0 & "SYM".equalsIgnoreCase(tag)) {
					buf.append(terms);
				} else {
					//2017-12-14 일부 화학식(AL2O3)이 생략이 되어 띄어쓰기로 집어 넣어 처리
					buf.append(" ");
				}
				continue;
				// return "";
			} else {
				if ("=".equals(stem)) {
					buf.append(terms);
				} else {
					// buf.append(terms);
					buf.append(stem);
				}
			}
		}
		return buf.toString().trim();
	}

	public static void main(String[] args) {
//		System.setProperty("EJIANA_HOME", "E:\\project\\2014\\KISTI_SCOPUS_IBS_SEARCH\\resources");
		String a = new EJianaUtil().getStem("Mos2");
		System.out.println(a);
	}

}
