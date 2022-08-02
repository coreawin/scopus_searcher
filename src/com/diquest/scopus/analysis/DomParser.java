package com.diquest.scopus.analysis;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomParser {
	/**
	 * <cto:unique-author seq="1"> <cto:auth-initials>A.</cto:auth-initials>
	 * <cto:auth-indexed-name>Amanov A.</cto:auth-indexed-name>
	 * <cto:auth-surname>Amanov</cto:auth-surname>
	 * <cto:auth-id>36447543900</cto:auth-id> <cto:auth-e-address
	 * type="email">amanov_a@yahoo.com</cto:auth-e-address> </cto:unique-author>
	 * 
	 * 교신저자가 없는경우 이메일 주소가 잇는 사람을 교신저자로 사용한다.
	 * 
	 * @author 정승한
	 * @date 2018. 6. 16.
	 * @param args
	 */
	public static String parseCorrespond(String xml) {
		String result = null;
		try {
			// DOM Document 객체 생성하기 위한 메서드
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			// DOM 파서로부터 입력받은 파일을 파싱하도록 요청
			DocumentBuilder parser = f.newDocumentBuilder();
			Document xmlDoc = null;
			// DOM 파서로부터 입력받은 파일을 파싱하도록 요청
			xmlDoc = parser.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			NodeList nList = xmlDoc.getElementsByTagName("cto:unique-author");
			System.out.println("----------------------------");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("cto:auth-e-address").item(0) != null) {
						String indexName = eElement.getElementsByTagName("cto:auth-indexed-name").item(0).getTextContent();
						String email = eElement.getElementsByTagName("cto:auth-e-address").item(0).getTextContent();
						System.out.println("indexed name : " + indexName);
						System.out.println("email : " + email);
						if(email.indexOf("@")!=-1){
							result = indexName +"|" + email;
							System.out.println("result : " + result);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
