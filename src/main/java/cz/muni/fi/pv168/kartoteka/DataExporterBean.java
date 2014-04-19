/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Majo
 */

@Named
@RequestScoped
public class DataExporterBean implements Serializable{
    
    @Inject
    private DBUtils dbUtils;
    
    private StreamedContent fileToExport;

    public DataExporterBean() {
        
    }

    public void prepareDownload(String nameOfCollection, String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(nameOfCollection);
        
        BufferedWriter bw = null;
        
        //filename is user(or selectedDB it is the same)_nameOfCollection.json
        String filename = selectedDB + "_" + nameOfCollection + ".json";
        
        StringBuilder sb = new StringBuilder();
        DBCursor cursor = collection.find();
        while(cursor.hasNext()){
            //currently processed document
            BasicDBObject cur = (BasicDBObject) cursor.next();
            sb.append(cur.toString());
            sb.append(System.lineSeparator());
        }
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        this.fileToExport = new DefaultStreamedContent(is, "text", filename);
        
        
    }
    
    public DBUtils getDbUtils() {
        return dbUtils;
    }

    public void setDbUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public StreamedContent getFile() {
        return fileToExport;
    }

    public void setFile(StreamedContent file) {
        this.fileToExport = file;
    }
}
