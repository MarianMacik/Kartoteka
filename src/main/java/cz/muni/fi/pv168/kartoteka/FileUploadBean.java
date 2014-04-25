/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.types.ObjectId;
import org.primefaces.event.FileUploadEvent;
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
    private TestBean testBean;
    
    private CabinetCard actualCard;

    public FileUploadBean() {
    }

    public void uploadFile(FileUploadEvent event){
        System.out.println(event.getFile().getFileName());
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");  
        FacesContext.getCurrentInstance().addMessage("messages", msg);
        
        String selectedDB = testBean.getSelectedDB();
        //get GridFS instance
        GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));
        try {
            //get inputFile and save it to GridFS
            GridFSInputFile gfsFile = binaryDB.createFile(event.getFile().getInputstream(), event.getFile().getFileName());
            gfsFile.save();
            ObjectId fileId = (ObjectId) gfsFile.getId();
            System.out.println(gfsFile);
            
            String schemaTitle = filingCabinetManagerBean.getFilingCabinet().getSchema().getTitle();
            //now link id of it to CabinetCard
            DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schemaTitle);
            
            BasicDBObject match = new BasicDBObject("_id", actualCard.getId());
            
            BasicDBObject update = new BasicDBObject(filingCabinetManagerBean.getFilingCabinet().getSchema().getBinaryDataFieldName(), fileId);
            
            filingCabinet.update(match, new BasicDBObject("$push", update));
            
            
            
        } catch (IOException ex) {
            Logger.getLogger(FileUploadBean.class.getName()).log(Level.SEVERE, "Problem with file uploading", ex);
        }        
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
