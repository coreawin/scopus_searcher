package com.diquest.scopus.searcher;

public class Worker implements Runnable{
	
	String searchRule = "";
	public Worker(String sr){
		searchRule = sr;
	}

	@Override
	public void run() {
		try {
			new 여운동_201812_SCOPUSSearcher(searchRule);
		} catch (Exception e) {
			e.printStackTrace();
		}
		여운동_201812_SCOPUSSearcher.bQueue.remove();
	}
	
}
