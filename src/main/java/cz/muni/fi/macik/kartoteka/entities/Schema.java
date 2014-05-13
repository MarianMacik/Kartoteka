package cz.muni.fi.macik.kartoteka.entities;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * Class represents schema for filing cabinet - list of schema fields and also
 * name of the field for binary files. Currently it is 'Files' but with this approach
 * it is easy to implement custom name of this field.
 * @author Marián Macik
 */
public class Schema {
    
    private ObjectId id;
    
    private String title = new String();
    
    private List<SchemaField> fields = new ArrayList<>();

    private String binaryDataFieldName = new String();
    
    /**
     * Transforms schema to DB Object - we use it only when we are adding new
     * schema to DB - then this schema doesn't have fields so far.
     * @return BasicDBObject that represents schema in DB
     */
    public BasicDBObject schemaToDBObject(){
        BasicDBObject obj = new BasicDBObject();
        obj.put("title", title);
        return obj;
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<SchemaField> getFields() {
        return fields;
    }
    
    public void setFields(List<SchemaField> fields) {
        this.fields = fields;
    }
    
    public String getBinaryDataFieldName() {
        return binaryDataFieldName;
    }
    
    public void setBinaryDataFieldName(String binaryDataFieldName) {
        this.binaryDataFieldName = binaryDataFieldName;
    }
//</editor-fold>
    
    
}
