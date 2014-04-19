/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import org.bson.types.ObjectId;

/**
 *
 * @author Majo
 */
public class Main {
    public static void main(String[] args) throws UnknownHostException {
        MongoClient mongoClient = new MongoClient("localhost" , 27017);
        DBUtils DBUtils = new DBUtils();
        DBCollection collection = mongoClient.getDB("test").getCollection("testData");
        
        DBCursor cursor = collection.find();
        ObjectId id = new ObjectId();
        while (cursor.hasNext()) {
            
            BasicDBObject cur = (BasicDBObject) cursor.next();
            
            if (cur.containsField("x")) {
                System.out.println(cur.getDouble("x")); 
            }
            
            id = cur.getObjectId("_id");
		System.out.println(cur.getObjectId("_id"));
                System.out.println(cur);
	}
        
        System.out.println("\n\n");
        
        System.out.println(collection.find(new BasicDBObject("_id",id)).next());
        
//        BasicDBObject ins = new BasicDBObject();
//        ins.put("druheID", id);
//        ins.put("nazov", "debilko");
//        collection.insert(ins);
        
        
        System.out.println("\n\n");
        DBCursor curss = collection.find();
        while (curss.hasNext()) {
            
            
                System.out.println(curss.next());
	}
    }
}
