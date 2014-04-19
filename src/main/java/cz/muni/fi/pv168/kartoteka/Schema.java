/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

/**
 *
 * @author Majo
 */
public class Schema {
    
    private ObjectId id;
    
    private String title = new String();
    
    private List<SchemaField> fields = new ArrayList<>();

    public BasicDBObject schemaToDBObject(){
        BasicDBObject obj = new BasicDBObject();
        obj.put("title", title);
        return obj;
    }
    
    
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
    
    
    
    
}
