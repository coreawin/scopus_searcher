package com.diquest.scopus.searcher._2021.external;

import java.util.Random;

public class Dermy {
	
	public static void main(String[] args) {
		Random r = new Random(System.nanoTime());
		for(int i=0; i<100; i++){
			System.out.println(r.nextInt(5));
		}
	}

}
