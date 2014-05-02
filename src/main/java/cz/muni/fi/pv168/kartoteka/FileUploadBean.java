/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.types.ObjectId;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Majo
 */
@Named
@SessionScoped
public class FileUploadBean implements Serializable{
    
    @Inject
    private DBUtils dbUtils;

    @Inject
    private FilingCabinetManagerBean filingCabinetManagerBean;
    
    @Inject
    private MainManagerBean testBean;
    
    private CabinetCard actualCard;
    
    public FileUploadBean() {
    }

    public void uploadFile(FileUploadEvent event){
        System.out.println(event.getFile().getFileName());
        
        String selectedDB = testBean.getSelectedDB();
        //get GridFS instance
        GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));
        try {
            //get inputFile and save it to GridFS
            InputStream is = event.getFile().getInputstream();
            GridFSInputFile gfsFile = binaryDB.createFile(is, event.getFile().getFileName());
            gfsFile.save();
            ObjectId fileId = (ObjectId) gfsFile.getId();
            System.out.println(gfsFile);
            
            String schemaTitle = filingCabinetManagerBean.getFilingCabinet().getSchema().getTitle();
            //now link id of it to CabinetCard
            DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schemaTitle);
            
            BasicDBObject match = new BasicDBObject("_id", actualCard.getId());
            
            BasicDBObject update = new BasicDBObject("Files", fileId);
            
            filingCabinet.update(match, new BasicDBObject("$push", update));
            
            is.close();
            
        } catch (IOException ex) {
            Logger.getLogger(FileUploadBean.class.getName()).log(Level.SEVERE, "Problem with file uploading", ex);
        }    
        
        reloadActualCard();
    }
    
    public void deleteFile(ObjectId fileId){
        //first we will delete it from GridFS
        String selectedDB = testBean.getSelectedDB();
        //get GridFS instance
        GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));
        binaryDB.remove(fileId);
        
        //now we will remove ObjectId from document in filing cabinet
        String schemaTitle = filingCabinetManagerBean.getFilingCabinet().getSchema().getTitle();
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schemaTitle);
        
        //match for this card
        BasicDBObject match = new BasicDBObject("_id", actualCard.getId());
        BasicDBObject update = new BasicDBObject("Files", fileId);
        
        filingCabinet.update(match, new BasicDBObject("$pull", update));
        reloadActualCard();
    }
    
    private void reloadActualCard(){
        String collectionName = filingCabinetManagerBean.getFilingCabinet().getSchema().getTitle();
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(testBean.getSelectedDB()).getCollection(collectionName);
        
        BasicDBObject newActualCard = (BasicDBObject) filingCabinet.findOne(actualCard.getId());
        
        //data of the newActualCard
            List<Data> cardData = new ArrayList<>();

            //we will load particular values for CabinetCard according to schema
            for (SchemaField schemaField : filingCabinetManagerBean.getFilingCabinet().getSchema().getFields()) {
                if (schemaField.isRepeatable()) {

                    BasicDBList values = (BasicDBList) newActualCard.get(schemaField.getFieldTitle());

                    List<MyString> valuesList = new ArrayList<>();

                    if (values != null) {
                        for (Object obj : values) {
                            BasicDBObject value = (BasicDBObject) obj;
                            MyString myString = new MyString(value.getObjectId("_id"), value.getString("value"));
                            valuesList.add(myString);
                        }
                    }
                    //If values == null -> no values found -> we will add empty valuesList
                    cardData.add(new MultipleData(valuesList));
                } else {

                    //but we are in non repeatable schemaField, so we will obtain only one value
                    BasicDBList values = (BasicDBList) newActualCard.get(schemaField.getFieldTitle());
                    if (values != null && values.size() > 0) {
                        BasicDBObject value = (BasicDBObject) values.get(0);
                        cardData.add(new SimpleData(new MyString(value.getObjectId("_id"), value.getString("value"))));
                    } else {
                        //If values == null -> no values found -> we will add empty SimpleData, which adds empty MyString
                        cardData.add(new SimpleData());
                    }

                }
            }
            //now we will load binary files with theri IDs - IDs are for deletion
            List<Map.Entry<ObjectId, StreamedContent>> filesInDB = loadBinaryFiles(newActualCard, testBean.getSelectedDB());

            actualCard = new CabinetCard(newActualCard.getObjectId("_id"), cardData, filesInDB);

        }

    private List<Map.Entry<ObjectId, StreamedContent>> loadBinaryFiles(BasicDBObject cur, String selectedDB) {
        Map<ObjectId, StreamedContent> map = new HashMap<>();
        BasicDBList files = (BasicDBList) cur.get("Files");
        //if file field is defined
        if (files != null) {
            for (Object file : files) {
                ObjectId fileId = (ObjectId) file;
                GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));
                GridFSDBFile fileForOutput = binaryDB.findOne(fileId);
                //no need for closing InputStream - primefaces will close it accordint to documentation
                StreamedContent fileContent = new DefaultStreamedContent(fileForOutput.getInputStream(), "", fileForOutput.getFilename());
                map.put(fileId, fileContent);
            }
        }
        List<Map.Entry<ObjectId, StreamedContent>> result = new ArrayList<>(map.entrySet());
        //now we will sort it by names of the files
        result.sort(new Comparator<Map.Entry<ObjectId, StreamedContent>>() {
            @Override
            public int compare(Map.Entry<ObjectId, StreamedContent> entry1, Map.Entry<ObjectId, StreamedContent> entry2) {
                String str1 = entry1.getValue().getName().toLowerCase();
                String str2 = entry2.getValue().getName().toLowerCase();

                return str1.compareTo(str2);
            }
        });
        return result;
    }
    
    public String setCabinetCardAndShow(CabinetCard card){
        actualCard = card;
        return "cardupload.xhtml?faces-redirect=true";
    }
    
    public DBUtils getDbUtils() {
        return dbUtils;
    }

    public void setDbUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public CabinetCard getActualCard() {
        return actualCard;
    }

    public void setActualCard(CabinetCard actualCard) {
        this.actualCard = actualCard;
    }

}
