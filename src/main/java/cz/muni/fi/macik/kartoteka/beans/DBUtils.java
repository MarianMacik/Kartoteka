package cz.muni.fi.macik.kartoteka.beans;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Bean class provides connection with DB - one mongoClient for whole app
 * as advised in MongoDB documentation
 * @author Marián Macik
 */
@Named
@ApplicationScoped
public class DBUtils {
    
    final static Logger log = LoggerFactory.getLogger(DBUtils.class);
    
    private MongoClient mongoClient;
    
    /**
     * Init method to initialize connection with DB
     */
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
    
    /**
     * Method for closing connection with DB when app is to be stopped
     */
    @PreDestroy
    public void closeConnection(){
        mongoClient.close();
        System.out.println("DBUtilsBean Closed");
    }
}
