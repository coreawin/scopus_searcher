/**
 * 
 */
package com.diquest.scopus.searchrule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.diquest.scopus.searchrule.MARINER_FIELD.SCOPUS_MARINER_FIELD;

/**
 * 사용자가 입력한 고급 검색식의 항목을 검색이 가능한 항목으로 변경한다.<br>
 * 예 (home button).ab. => (home button).abs.
 * G-PASS에서 가져옴. 2014.05.02.
 * @author neon
 * @date 2013. 9. 13.
 * @Version 1.0
 */
public class FieldConversion {
	public static final Map<String, String> _CUSTOM_FIELD_INFO = new HashMap<String, String>();
	public static final Map<String, String> _CUSTOM_FIELD_INFO_REVERSE = new HashMap<String, String>();
	static {
		
		//SCOPUS.com 검색 필드
		_CUSTOM_FIELD_INFO.put("AF-ID", SCOPUS_MARINER_FIELD.AFID.name());
		_CUSTOM_FIELD_INFO.put("AF-ID-G", SCOPUS_MARINER_FIELD.AFIDG.name());
		_CUSTOM_FIELD_INFO.put("D-AFFIL", SCOPUS_MARINER_FIELD.DAFFIL.name());
		_CUSTOM_FIELD_INFO.put("D-AFFILCOUNTRY", SCOPUS_MARINER_FIELD.DAFFILCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("AUTHOR-NAME", SCOPUS_MARINER_FIELD.AUTHORNAME.name());
		_CUSTOM_FIELD_INFO.put("AUTHOR-EMAIL", SCOPUS_MARINER_FIELD.AUTHEMAIL.name());
		_CUSTOM_FIELD_INFO.put("AU-ID", SCOPUS_MARINER_FIELD.AUID.name());
		_CUSTOM_FIELD_INFO.put("AU-ID-G", SCOPUS_MARINER_FIELD.AUIDG.name());
		_CUSTOM_FIELD_INFO.put("FIRST-ASJC", SCOPUS_MARINER_FIELD.FIRSTASJC.name());
		_CUSTOM_FIELD_INFO.put("SRCTITLE-ABBREV", SCOPUS_MARINER_FIELD.SRCTITLEABBREV.name());
		_CUSTOM_FIELD_INFO.put("REF-EID", SCOPUS_MARINER_FIELD.REFEID.name());
		_CUSTOM_FIELD_INFO.put("REF-COUNT", SCOPUS_MARINER_FIELD.REFCOUNT.name());
		_CUSTOM_FIELD_INFO.put("CIT-EID", SCOPUS_MARINER_FIELD.CITEID.name());
		_CUSTOM_FIELD_INFO.put("CIT-COUNT", SCOPUS_MARINER_FIELD.CITCOUNT.name());
		_CUSTOM_FIELD_INFO.put("CR-COUNTRY", SCOPUS_MARINER_FIELD.CRCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("CR-CITY", SCOPUS_MARINER_FIELD.CRCITY.name());
		_CUSTOM_FIELD_INFO.put("CR-EMAIL", SCOPUS_MARINER_FIELD.CREMAIL.name());
		_CUSTOM_FIELD_INFO.put("CR-ORG", SCOPUS_MARINER_FIELD.CRORG.name());
		_CUSTOM_FIELD_INFO.put("CR-NAME", SCOPUS_MARINER_FIELD.CRNAME.name());
		_CUSTOM_FIELD_INFO.put("CR-FIRST", SCOPUS_MARINER_FIELD.CRFIRST.name());
		_CUSTOM_FIELD_INFO.put("CR-LAST", SCOPUS_MARINER_FIELD.CRLAST.name());
		_CUSTOM_FIELD_INFO.put("XML-C", SCOPUS_MARINER_FIELD.XMLC.name());
		_CUSTOM_FIELD_INFO.put("CR-AFID", SCOPUS_MARINER_FIELD.CRAFID.name());
		_CUSTOM_FIELD_INFO.put("CR-DORG", SCOPUS_MARINER_FIELD.CRDORG.name());
		_CUSTOM_FIELD_INFO.put("CR-DCOUNTRY", SCOPUS_MARINER_FIELD.CRDCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("CR-INDEXNAME", SCOPUS_MARINER_FIELD.CRINDEXNAME.name());
		_CUSTOM_FIELD_INFO.put("AUTH-INITIALNAME", SCOPUS_MARINER_FIELD.AUTHINIT.name());
		_CUSTOM_FIELD_INFO.put("SRC-COUNTRY", SCOPUS_MARINER_FIELD.SRCCOUNTRY.name());

		
		//기존의 검색 필드 정보를 마리너 검색 필드로 지정 저장 한다.
		_CUSTOM_FIELD_INFO.put("TI", SCOPUS_MARINER_FIELD.TITLE.name());
		_CUSTOM_FIELD_INFO.put("AB", SCOPUS_MARINER_FIELD.ABS.name());
		_CUSTOM_FIELD_INFO.put("KW", SCOPUS_MARINER_FIELD.KEY.name());
		_CUSTOM_FIELD_INFO.put("AFFILIATION", SCOPUS_MARINER_FIELD.AFFIL.name());
		_CUSTOM_FIELD_INFO.put("COUNTRY", SCOPUS_MARINER_FIELD.AFFILCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("SOURCETITLE", SCOPUS_MARINER_FIELD.SRCTITLE.name());
		_CUSTOM_FIELD_INFO.put("SOURCETYPE", SCOPUS_MARINER_FIELD.SRCTYPE.name());
		_CUSTOM_FIELD_INFO.put("SOURCEID", SCOPUS_MARINER_FIELD.SRCID.name());
		_CUSTOM_FIELD_INFO.put("PBY", SCOPUS_MARINER_FIELD.SORTYEAR.name());
		_CUSTOM_FIELD_INFO.put("CITTYPE", SCOPUS_MARINER_FIELD.DOCTYPE.name());
		_CUSTOM_FIELD_INFO.put("AU", SCOPUS_MARINER_FIELD.AUTHORNAME.name());
		_CUSTOM_FIELD_INFO.put("AUTHORID", SCOPUS_MARINER_FIELD.AUID.name());
		_CUSTOM_FIELD_INFO.put("AFID", SCOPUS_MARINER_FIELD.AFID.name());
		
		_CUSTOM_FIELD_INFO.put("XMLCITEDBY", SCOPUS_MARINER_FIELD.XMLC.name());
		_CUSTOM_FIELD_INFO.put("REFCNT", SCOPUS_MARINER_FIELD.REFCOUNT.name());
		_CUSTOM_FIELD_INFO.put("CITCNT", SCOPUS_MARINER_FIELD.CITCOUNT.name());
		_CUSTOM_FIELD_INFO.put("CR_CC", SCOPUS_MARINER_FIELD.CRCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("CR_EMAIL", SCOPUS_MARINER_FIELD.CREMAIL.name());
		_CUSTOM_FIELD_INFO.put("CR_ORG", SCOPUS_MARINER_FIELD.CRORG.name());
		_CUSTOM_FIELD_INFO.put("CR_NAME", SCOPUS_MARINER_FIELD.CRNAME.name());
		_CUSTOM_FIELD_INFO.put("EMAIL", SCOPUS_MARINER_FIELD.AUTHEMAIL.name());
		_CUSTOM_FIELD_INFO.put("D_AFFILIATION", SCOPUS_MARINER_FIELD.DAFFIL.name());
		_CUSTOM_FIELD_INFO.put("D_COUNTRY", SCOPUS_MARINER_FIELD.DAFFILCOUNTRY.name());
		
		
		//WoS 형태의 필드 정보를 지정한다.
		_CUSTOM_FIELD_INFO.put("PY", SCOPUS_MARINER_FIELD.SORTYEAR.name());
		_CUSTOM_FIELD_INFO.put("CU", SCOPUS_MARINER_FIELD.AFFILCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("DO", SCOPUS_MARINER_FIELD.DOI.name());
		_CUSTOM_FIELD_INFO.put("OO", SCOPUS_MARINER_FIELD.DAFFIL.name());
		_CUSTOM_FIELD_INFO.put("OG", SCOPUS_MARINER_FIELD.AFFIL.name());
		_CUSTOM_FIELD_INFO.put("SO", SCOPUS_MARINER_FIELD.SRCTITLE.name());
		_CUSTOM_FIELD_INFO.put("CR-AU", SCOPUS_MARINER_FIELD.CRNAME.name());
		_CUSTOM_FIELD_INFO.put("CR-OG", SCOPUS_MARINER_FIELD.CRORG.name());
		_CUSTOM_FIELD_INFO.put("CR-CU", SCOPUS_MARINER_FIELD.CRCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("DCU", SCOPUS_MARINER_FIELD.DAFFILCOUNTRY.name());
		_CUSTOM_FIELD_INFO.put("GT-AG", SCOPUS_MARINER_FIELD.GT_AGENCY.name());
		_CUSTOM_FIELD_INFO.put("GT-AC", SCOPUS_MARINER_FIELD.GT_ACRONYM.name());
		
		Set<Entry<String, String>> es = _CUSTOM_FIELD_INFO.entrySet();
		for (Entry<String, String> e : es) {
			_CUSTOM_FIELD_INFO_REVERSE.put(e.getValue(), e.getKey());
		}
	}

	static Pattern fieldPattern = Pattern.compile("(?<=\\.)([a-z,A-Z,_-]{1,})");

	public static void conversion(String aquery) {
		Matcher m = fieldPattern.matcher(aquery);
		while (m.find()) {
			System.out.println(m.group());
		}
	}

	/**
	 * 사용자 정의 필드명을 검색을 위한 검색식 필드명으로 교체한다.
	 * 
	 * @author neon
	 * @date 2013. 9. 13.
	 * @param field
	 * @return
	 */
	public static String conversionField(String field) {
		field = field.toUpperCase().trim();
		String s = _CUSTOM_FIELD_INFO.get(field);
		if (s == null) {
			s = field;
		}
		return s.toUpperCase();
	}
	
	/**
	 * 사용자 정의 필드명을 검색을 위한 검색식 필드명으로 교체한다.
	 * 
	 * @author neon
	 * @date 2013. 9. 13.
	 * @param field
	 * @return
	 */
	public static String conversionReverseField(String field) {
		String s = _CUSTOM_FIELD_INFO_REVERSE.get(field);
		if (s == null) {
			s = field;
		}
		return s;
	}

	public static void main(String... args) {
//		String aquery = "((\"touch display screen\").ti.ab.) AND ((A).publ_type. (\"APPLE INC.\" OR \"SHAMBAYATI MAZY\").ap.  )  ";
//		conversion(aquery);
		
		for(SCOPUS_MARINER_FIELD  f: MARINER_FIELD._GROUP_FIELDS){
			System.out.println(f.getGroupField());
		}
	}

}
