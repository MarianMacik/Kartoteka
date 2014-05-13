package cz.muni.fi.macik.kartoteka.beans;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.types.ObjectId;

/**
 * Bean class for handling the list of user schemas, it also handles logged user DB
 * @author Marián Macik
 */
@Named
@SessionScoped
public class MainManagerBean implements Serializable{
    private String selectedDB;
    
    private List<Entry<ObjectId,String>> schemas = new ArrayList<>();
    
    @Inject
    private DBUtils dbUtils;
    
    /**
     * Loads the list of schemas currently own by logged in user.
     * @return null - it refreshes the page via JSF
     */
    public String loadUserSchemas(){
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        
        DBCursor cursor = collection.find();
        
        Map<ObjectId, String> map = new HashMap<>();
        
        while (cursor.hasNext()) {
            
            BasicDBObject cur = (BasicDBObject) cursor.next();
            ObjectId id = cur.getObjectId("_id");
            String title = cur.getString("title");
            
            map.put(id, title);
            
	}
        
        schemas = new ArrayList<>(map.entrySet());
        schemas.sort(new Comparator<Entry<ObjectId, String>>(){
                        @Override
                        public int compare(Entry<ObjectId, String> entry1, Entry<ObjectId, String> entry2){
                            String str1 = entry1.getValue().toLowerCase();
                            String str2 = entry2.getValue().toLowerCase();
                            
                            return str1.compareTo(str2);
                        }
        });
        return null;
    }
    
    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
    public String getSelectedDB() {
        return selectedDB;
    }
    
    public void setSelectedDB(String selectedDB) {
        this.selectedDB = selectedDB;
    }
    
    public List<Entry<ObjectId, String>> getSchemas() {
        return schemas;
    }
    
    public void setSchemas(List<Entry<ObjectId, String>> schemas) {
        this.schemas = schemas;
    }
    
    public DBUtils getDbUtils() {
        return dbUtils;
    }
    
    public void setDbUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }
//</editor-fold>

}
