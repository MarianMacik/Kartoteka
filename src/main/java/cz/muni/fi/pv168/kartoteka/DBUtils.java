/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Majo
 */
@Named
@ApplicationScoped
public class DBUtils {
    
    final static Logger log = LoggerFactory.getLogger(DBUtils.class);
    
    private MongoClient mongoClient;
    
    @PostConstruct
    public void init(){
        try {
            mongoClient = new MongoClient( "localhost" , 27017 );
            System.out.println("DBUtilsBean initialized");
        } catch (UnknownHostException ex) {
            log.error("UnknownHostException problem when getting DB", ex);
        }
    }
    
    public MongoClient getMongoClient(){
            
           
            return mongoClient;
    }
    
    @PreDestroy
    public void closeConnection(){
        mongoClient.close();
        System.out.println("DBUtilsBean Closed");
    }
}
