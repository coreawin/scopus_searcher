package com.diquest.scopus.searcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.bson.Document;
import org.bson.types.Binary;

//import com.diquest.k.patent.h.CompressUtil;
import com.diquest.scopus.MongoDBConnector;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class 여운동_201808_KIPRISXML {
	public static void main(String[] args) {

		MongoClient mc = null;

		try {
			mc = MongoDBConnector.getInstance("203.250.207.75", 27017);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MongoDatabase mdb = mc.getDatabase("KISTI_2017_KIPRIS");
		MongoCollection<Document> mcd = mdb.getCollection("KOREA");

		for(int idx=2003; idx<2018; idx++){
			File f = new File("D:/data/kipris/"+idx);
			if(!f.isDirectory()){
				f.mkdirs();
			}
			BasicDBObject doc = new BasicDBObject("ayear", String.valueOf(idx));
			FindIterable<Document> fIter = mcd.find(doc).batchSize(1000).noCursorTimeout(true);
			long cnt = mcd.count(doc);
			System.out.println(cnt);
			MongoCursor<Document> mCur = fIter.iterator();
			int i = 0;
			while (mCur.hasNext()) {
				i++;
				if(i%1000==0){
					System.out.println("progress " + i +"/" + cnt);
				}
				Document d = mCur.next();
				String xml = "";
				BufferedWriter bw = null;
				try {
					Binary bi = (Binary) d.get("xml_st96");
					if(bi==null) continue;
//					xml = CompressUtil.getInstance().unCompress(bi.getData());
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(f.getAbsolutePath() + File.separator + d.getString("_id") +".xml"))));
					bw.write(xml);
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					if(bw!=null)
						try {
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		}

	}
}
