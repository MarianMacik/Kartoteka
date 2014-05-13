package cz.muni.fi.macik.kartoteka.beans;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Class for exporting data of one filing cabinet
 * @author Marián Macik
 */

@Named
@RequestScoped
public class DataExporterBean implements Serializable{
    
    @Inject
    private DBUtils dbUtils;
    
    private StreamedContent fileToExport;

    public DataExporterBean() {
        
    }

    /**
     * Method prepares file for export - output file is json serialization
     * of documents in particular collection which represents filing cabinet
     * @param nameOfCollection - collection to export
     * @param selectedDB - DB in which the collection is
     */
    public void prepareDownload(String nameOfCollection, String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(nameOfCollection);
        
        //filename is selectedDB_nameOfCollection.json
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
        //no need for closing InputStream - primefaces will close it according to documentation
        this.fileToExport = new DefaultStreamedContent(is, "text", filename);
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
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
//</editor-fold>
}
