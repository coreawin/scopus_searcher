/**
 * 
 */
package com.diquest.scopus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author coreawin
 * @date 2014. 7. 15.
 * @Version 1.0
 */
public abstract class AReadFile {

	public Logger logger = LoggerFactory.getLogger(getClass());
	String path = "";

	public AReadFile(String path) throws IOException {
		this.path = path;
		verify();
	}

	/**
	 * @author coreawin
	 * @date 2014. 7. 15.
	 */
	private void verify() {
		new File(this.path).mkdirs();
	}

	protected void loadFile() throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				readline(line);
			}
		} catch (IOException e) {
//			e.printStackTrace();
			throw e;
		} finally {
			if (br != null)
				br.close();
		}
	}

	/**
	 * @author coreawin
	 * @date   2014. 7. 15.
	 * @param line
	 */
	public abstract void readline(String line);

}
