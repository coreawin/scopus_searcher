package com.diquest.scopus.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 플랫폼에서 사용하는 문자열 처리 유틸
 * 
 * @author 이관재
 * @date 2015. 4. 3.
 * @version 1.0
 * @filename UtilString.java
 */
public class UtilString {

	/**
	 *
	 * 문자열이 Null인지 체크한다.
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 *            입력 문자열
	 * @param isTrim
	 *            트림여부
	 * @return
	 */
	public static String nullCkeck(String str, boolean isTrim) {
		String result = str;
		if (str == null)
			return "";

		if ("null".equalsIgnoreCase(str.trim())) {
			result = " ";
		}

		return isTrim ? result.trim() : result;
	}

	/**
	 *
	 * 문자열이 Null인지 체크한다.
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 *            입력 문자열
	 * @param defaultValue
	 *            기본값
	 * @return
	 */
	public static String nullCkeck(String str, String defaultValue) {
		if (str == null)
			return defaultValue;

		String result = str.trim();

		if ("null".equalsIgnoreCase(str.trim())) {
			result = defaultValue;
		}

		if (result.length() < 1) {
			result = defaultValue;
		}

		return result.trim();
	}

	/**
	 *
	 * 문자열이 Null인지 체크한다.
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 *            입력 문자열
	 * @param defaultValue
	 *            기본값
	 * @return
	 */
	public static String nullCkeck(String str) {
		return nullCkeck(str, false);
	}

	/**
	 *
	 * Object 문자열 타입 Null인지 체크한다.
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 *            입력 문자열
	 * @return
	 */
	public static String nullCkeck(Object str, String defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		return nullCkeck(str.toString().trim(), false);
	}

	/**
	 *
	 * 인스턴스가 null인지 체크한다.
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param obj
	 *            Object 인스턴스
	 * @param defaultValue
	 *            기본 인스턴스
	 * @return
	 */
	public static <T> T nullCkeckObject(Object obj, T defaultValue) {
		if (obj == null) {
			return defaultValue;
		}

		@SuppressWarnings(value = "unchecked")
		T result = (T) obj;
		return result;
	}

	/**
	 *
	 * 정수형 값 Null Check
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param v
	 *            값
	 * @return
	 */
	public static int nullCkeck(int v) {
		try {
			Integer.parseInt(String.valueOf(v));
			return v;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 *
	 * 실수(float형) 값 Null Check
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param v
	 *            값
	 * @return
	 */
	public static float nullCkeck(float v) {
		try {
			Float.parseFloat(String.valueOf(v));
			return v;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 *
	 * 실수(double형) 값 Null Check
	 * 
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param v
	 *            값
	 * @return
	 */
	public static double nullCkeck(double v) {
		try {
			Double.parseDouble(String.valueOf(v));
			return v;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 *
	 * SQL의 Where 절 조건을 생성한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param set
	 *            다중 조건
	 * @param isOrCondition
	 *            OR 연산 여부
	 * @return
	 */
	public static String whereContidion(Set<String> set, boolean isOrCondition) {
		StringBuffer whereCondition = new StringBuffer();
		for (int idx = 0; idx < set.size(); idx++) {
			if (idx == 0) {
				whereCondition.append(" (");
			}
			if ((set.size() - 1) == idx) {
				whereCondition.append(" eid=? ");
			} else {
				if (isOrCondition) {
					whereCondition.append(" eid=? or ");
				} else {
					whereCondition.append(" eid=? and ");
				}
			}
		}
		whereCondition.append(" ) ");
		return whereCondition.toString();
	}

	public static void main(String[] args) {
		Set<String> set = new HashSet<String>();
		set.add("ABB");
		set.add("CDD");
		set.add("QWE");
		System.out.println(UtilString.whereINContidion(set));

		String s = "{seq:1}{initials:M.}{indexed-name:Abdellaoui M.}{surname:Abdellaoui}";
		s = s.replaceAll("\\{", "").replaceAll("\\}", "㉶");
		System.out.println(s);
		String[] sArr = s.split("㉶");
		for (int i = 0; i < sArr.length; i++) {
			System.out.println(sArr[i]);
		}
		// UtilString.splite(s, regex);
	}

	/**
	 *
	 * SQL의 Where 절 조건을 생성한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param set
	 *            다중 조건
	 * @return
	 */
	public static String whereINContidion(Set<String> set) {
		StringBuffer whereCondition = new StringBuffer();
		int idx = 0;
		whereCondition.append(" ( ");
		for (String s : set) {
			whereCondition.append("?");
			if ((set.size() - 1) != idx++) {
				whereCondition.append(",");
			}
		}
		whereCondition.append(" ) ");
		return whereCondition.toString();
	}

	/**
	 *
	 * SQL의 Where 절 조건을 생성한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param set
	 *            다중 조건
	 * @param column
	 *            컬럼명
	 * @param isOrCondition
	 *            OR 연산 여부
	 * @return
	 */
	public static String whereContidionSetData(Set<String> set, String column, boolean isOrCondition) {
		StringBuffer whereCondition = new StringBuffer();
		int idx = 0;
		for (String d : set) {
			if (idx == 0) {
				whereCondition.append(" (");
			}
			if ((set.size() - 1) == idx) {
				whereCondition.append(" ");
				whereCondition.append(column);
				whereCondition.append("='");
				whereCondition.append(d);
				whereCondition.append("' ");
			} else {
				if (isOrCondition) {
					whereCondition.append(" ");
					whereCondition.append(column);
					whereCondition.append("='");
					whereCondition.append(d);
					whereCondition.append("' or");
				} else {
					whereCondition.append(" ");
					whereCondition.append(column);
					whereCondition.append("='");
					whereCondition.append(d);
					whereCondition.append("' and");
				}
			}
			idx++;
		}
		whereCondition.append(" ) ");
		return whereCondition.toString();
	}

	public static String whereContidion(Set<String> set, String columnName, boolean isOrCondition) {
		StringBuffer whereCondition = new StringBuffer();
		for (int idx = 0; idx < set.size(); idx++) {
			if (idx == 0) {
				whereCondition.append(" (");
			}
			if ((set.size() - 1) == idx) {
				whereCondition.append(" ");
				whereCondition.append(columnName);
				whereCondition.append("=?");
				whereCondition.append(" ");
			} else {
				if (isOrCondition) {
					whereCondition.append(" ");
					whereCondition.append(columnName);
					whereCondition.append("=? or ");
				} else {
					whereCondition.append(" ");
					whereCondition.append(columnName);
					whereCondition.append("=? and ");
				}
			}
		}
		whereCondition.append(" ) ");
		return whereCondition.toString();
	}

	public static Timestamp nullCkeck(Timestamp insertTime) {
		return insertTime;
	}

	public static HashMap<String, String> nullCkeck(HashMap<String, String> countryType) {
		return new HashMap<String, String>();
	}

	/**
	 * 
	 * 배열로된 문자열을 지정한 구분자로 구분된 문자열로 합친다.
	 * 
	 * --> javascript의 join('xxxx')함수 기능 수행
	 * 
	 * @author 이관재
	 * @date 2014. 5. 15.
	 * @param array
	 *            제너렉 타입 배열(여러 타입의 문자열이 필요 할경우 발생
	 * @param delimiter
	 *            구분자
	 * @return
	 */
	public static <T> String arrayJoin(T[] array, String delimiter) {
		StringBuffer sb = new StringBuffer();
		if (array != null && array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				T t = array[i];
				sb.append(t);
				if (i < array.length - 1) {
					sb.append(delimiter);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * String.split()함수에서 나누지 못하는 문자열을 사용자 정규식을 이용하여 String.Split()역활을 수행한다.
	 * 
	 * @author 이관재
	 * @date 2014. 5. 21.
	 * @param s
	 * @param regex
	 * @return
	 */
	public static String[] splite(String s, String regex) {
		List<String> spliteList = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(s);

		while (m.find()) {
			System.out.println(m.group());
		}
		return spliteList.toArray(new String[0]);
	}

	/**
	 * 두개 이상의 빈 문자열을 찾는 정규식
	 */
	public static final String REGX3 = "[\\s]{1,}";

	/**
	 * 특수문자를 제거한다.<br>
	 * 
	 * @author neon
	 * @date 2014. 5. 14.
	 * @param s
	 * @return
	 * @see SCOPUSUtil._SC
	 */
	public static String removeAllSpecialCharacter(String regex, String s) {
		if (s == null)
			return " ";
		return s.replaceAll(regex, " ").replaceAll(REGX3, " ");
	}

	/**
	 *
	 * 입력 문자열이 UTF-8인지 검증한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static boolean isUTF8(String str) throws Exception {
		byte[] bytes = str.getBytes("ISO-8859-1");
		return isUTF8(bytes, 0, bytes.length);
	}

	public static boolean isUTF8(byte[] buf, int offset, int length) {

		boolean yesItIs = false;
		for (int i = offset; i < offset + length; i++) {
			if ((buf[i] & 0xC0) == 0xC0) { // 11xxxxxx 패턴 인지 체크
				int nBytes;
				for (nBytes = 2; nBytes < 8; nBytes++) {
					int mask = 1 << (7 - nBytes);
					if ((buf[i] & mask) == 0)
						break;
				}

				// CJK영역이나 아스키 영역의 경우 110xxxxx 10xxxxxx 패턴으로 올수 없다.
				if (nBytes == 2)
					return false;

				// Check that the following bytes begin with 0b10xxxxxx
				for (int j = 1; j < nBytes; j++) {
					if (i + j >= length || (buf[i + j] & 0xC0) != 0x80)
						return false;
				}

				if (nBytes == 3) {
					// 유니코드 형태로 역치환 해서 0x0800 ~ 0xFFFF 사이의 영역인지 체크한다.
					char c = (char) (((buf[i] & 0x0f) << 12) + ((buf[i + 1] & 0x3F) << 6) + (buf[i + 2] & 0x3F));
					if (!(c >= 0x0800 && c <= 0xFFFF)) {
						return false;
					}
				}

				yesItIs = true;
			}
		}
		return yesItIs;
	}

	/**
	 * 
	 * 문자열을 ISO-8859-1 -> UTF-8로 인코딩을 한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param str
	 * @return
	 */
	public static String encodingString(String str) {
		if (str == null)
			return null;
		else {
			byte[] b;

			try {
				b = str.getBytes("ISO-8859-1");
				CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
				try {
					CharBuffer r = decoder.decode(ByteBuffer.wrap(b));
					return r.toString();
				} catch (CharacterCodingException e) {
					return new String(b, "MS949");
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return str;
	}

	/**
	 * 
	 * 이메일인지 체크한다.
	 *
	 * @author 이관재
	 * @date 2015. 4. 3.
	 * @version 1.0
	 * @param email
	 *            이메일 주소
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (email == null)
			return false;
		boolean b = Pattern.matches("[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+", email.trim());
		return b;
	}
}
