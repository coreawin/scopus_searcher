package com.diquest.scopus.searchrule;


/**
 * 
 * 검색식을 체크하는 모듈이다.
 * 
 * @fileName : CheckWosSearchRule.java
 * @author : 이관재
 * @date : 2014. 5. 8.
 * @Version : 1.0
 */
public class CheckWosSearchRule {

	/**
	 * 
	 * 
	 * Wos 형식의 검색인지 판단하을 수행한다.
	 * 
	 * @author 이관재
	 * @date 2014. 5. 9.
	 * @param scopusRule
	 * @return
	 */
	public static String checkWosSearchRule(String scopusRule) throws Exception {

		// 신규 검색식과 기존 검색식을 구분하기 위해 처리
		QueryConverterWoS_OLD conventor = new QueryConverterWoS_OLD(scopusRule);
		if (conventor.getWhereSet().length > 0) {
			return scopusRule;
		} else {
			throw new RuntimeException("[ErrorCode-1003] 지원하지 않는 검색식 입니다.");
		}
	}
	
	
	public static void main(String[] args) {
		try {
			System.setProperty("EJIANA_HOME", "E:\\project\\2014\\KISTI_SCOPUS_IBS_SEARCH\\resources");
			String q = checkWosSearchRule("TI=(fast growth)");
			System.out.println(q);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
