/**
 * 
 */
package com.diquest.scopus.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ejiana.tagger.EJianaBuffer;
import com.diquest.ejiana.tagger.EJianaConst;
import com.diquest.ejiana.tagger.PosTagger;

/**
 * 영문 Stemming 처리를 위한 EJiana 추출 연동 클래스.ㄴ
 * 
 * @author neon
 * @date 2013. 7. 19.
 * @Version 1.0
 */
public class EStemming {

	static Logger logger = LoggerFactory.getLogger(EStemming.class);

	// static EStemming instance;
	// 입출력 버퍼

	PosTagger tagger = null;
	EJianaBuffer buffer = null;
	private String _home;
	public EStemming() {
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

//		logger.info("EJIANA_HOME : {}", _home);

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

//	public static EStemming getInstance() {
//		if (instance == null) {
//			instance = new EStemming();
//		}
//		return instance;
//	}

	/**
	 * 입력한 키워드의 원형을 리턴한다. <br>
	 * 입력한 키워드와 원형이 같다면 null을 리턴한다.<br>
	 * 
	 * @author neon
	 * @date 2013. 7. 19.
	 * @param keyword
	 * @return
	 */
	public String analysis(String keyword) {
		if(true) return keyword;
		EJianaBuffer jianaBuf = new EJianaBuffer();
		StringBuffer buf = new StringBuffer();
		buf.setLength(0);
		jianaBuf.init(keyword.toCharArray());
		// 분석 실행
		tagger.analyze(jianaBuf);
		// 형태소 정보 출력
		boolean braceOpen = false;
		int isStem = -1;
		try {
			for (int i = 0; i < jianaBuf.nTerm; i++) {
				// Stem
				if (jianaBuf.stemLength[i] > 1) {
					// // 형태소
					String sourceStr = new String(jianaBuf.input, jianaBuf.termStart[i], jianaBuf.termLength[i]);
					String stemmingStr = new String(jianaBuf.stem, jianaBuf.stemStart[i], jianaBuf.stemLength[i]);
					if ("PLED".equalsIgnoreCase(sourceStr) || "LED".equalsIgnoreCase(sourceStr)) {
						stemmingStr = sourceStr;
					}

					if (braceOpen) {
						buf.append(stemmingStr.toLowerCase());
					} else {
						buf.append(stemmingStr.toLowerCase() + " ");
					}
					isStem += 1;
				} else {
					String s = new String(jianaBuf.input, jianaBuf.termStart[i], jianaBuf.termLength[i]);
					if (s.length() < 1)
						continue;
					if (s.charAt(0) == ' ') {
						continue;
					}
					if (s.charAt(0) == '(') {
						braceOpen = true;
					}
					if (s.charAt(0) == ')') {
						braceOpen = false;
					}
					buf.append(s.trim());
					if (jianaBuf.termLength[i] > 1) {
						if (braceOpen) {
						} else {
							buf.append(' ');
						}
					}
				}
			}
			if (isStem >= 0) {
				return buf.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("에러 키워드 : " + keyword);
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static void main(String[] args) {
		System.setProperty("EJIANA_HOME", "D:\\Project_WorkSpace\\2014\\KISTI_SCOPUS_2014_PLATFORM\\WebContent\\WEB-INF\\");
		String a = new EStemming().analysis("milling");
		System.out.println(a);
		// System.out.println(EStemming.getInstance("E:\\project\\2014\\KISTI_SCOPUS_2014_PLATFORM\\WebContent\\WEB-INF")
		// .analysis("Light Emitting diode(WLED)"));
		// System.out.println(EStemming.getInstance().analysis("Reynolds average
		// Navier stoke (RANS)equation"));
	}

}
