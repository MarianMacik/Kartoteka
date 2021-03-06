package cz.muni.fi.macik.kartoteka.beans;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import cz.muni.fi.macik.kartoteka.entities.CabinetCard;
import cz.muni.fi.macik.kartoteka.entities.Data;
import cz.muni.fi.macik.kartoteka.entities.FilingCabinet;
import cz.muni.fi.macik.kartoteka.entities.MultipleData;
import cz.muni.fi.macik.kartoteka.entities.MyString;
import cz.muni.fi.macik.kartoteka.entities.SchemaField;
import cz.muni.fi.macik.kartoteka.entities.SimpleData;
import cz.muni.fi.macik.kartoteka.validator.Validator;
import cz.muni.fi.macik.kartoteka.validator.ValidatorType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.types.ObjectId;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Bean class for handling all actions connected with particular filing cabinet
 * @author Marián Macik
 */
@Named
@SessionScoped
public class FilingCabinetManagerBean implements Serializable {

    @Inject
    private DBUtils dbUtils;

    @Inject
    private SchemaManagerBean schemaManager;

    private FilingCabinet filingCabinet;

    private CabinetCard newCabinetCard;

    private CabinetCard editCabinetCard;

    /**
     * Method loads filing cabinet - schema together with cards - from DB
     * @param id - id of schema, when we load schema, we can load also cabinet by schema title
     * @param selectedDB - actual user DB
     * @return "cabinet.xhtml?faces-redirect=true" - navigates to page to view the cabinet
     */
    public String loadFilingCabinetAndShow(ObjectId id, String selectedDB) {
        FilingCabinet newFilingCabinet = new FilingCabinet();

        //load schema first, with help of schemaManager method loadSchema - returns schema
        newFilingCabinet.setSchema(schemaManager.loadSchema(id, selectedDB));

        //list of future cabinet cards
        List<CabinetCard> cabinetCards = new ArrayList<>();

        String collectionName = newFilingCabinet.getSchema().getTitle();
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(collectionName);
        //now we will load data itself from the scheme

        //all documents in collection
        DBCursor cursor = collection.find();

        while (cursor.hasNext()) {
            //currently processed document
            BasicDBObject cur = (BasicDBObject) cursor.next();

            //data of the future CabinetCard
            List<Data> cardData = new ArrayList<>();

            //we will load particular values for CabinetCard according to schema
            for (SchemaField schemaField : newFilingCabinet.getSchema().getFields()) {
                if (schemaField.isRepeatable()) {

                    BasicDBList values = (BasicDBList) cur.get(schemaField.getFieldTitle());

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
                    BasicDBList values = (BasicDBList) cur.get(schemaField.getFieldTitle());
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
            List<Entry<ObjectId, StreamedContent>> filesInDB = loadBinaryFiles(cur, selectedDB);

            cabinetCards.add(new CabinetCard(cur.getObjectId("_id"), cardData, filesInDB));

        }
        newFilingCabinet.setFilingCabinetData(cabinetCards);

        filingCabinet = newFilingCabinet;
        prepareNewCabinetCard();
        editCabinetCard = null;
        return "cabinet.xhtml?faces-redirect=true";
    }

    /**
     * Method for adding dataField for particluar schema field - when we want to add/edit card
     * @param index - represents schema field of a card
     * @param cabinetCard - cabinet card to add it to
     */
    public void addDataField(int index, CabinetCard cabinetCard) {
        Data data = cabinetCard.getCardData().get(index);

        //check if it is really MultipleData object, so we can cast it to this type
        if (data.isRepeatable()) {
            MultipleData multipleData = (MultipleData) data;
            multipleData.addData();
        } else {
            //special case, when we have simple data, but nothing was found in db, so we have to add a field in this cas
            SimpleData simpleData = (SimpleData) data;
            List<MyString> list = new ArrayList<>();
            list.add(new MyString());
            simpleData.setData(list);
        }

    }

    /**
     * Method for removing data field from card
     * @param index - represents schema field of a card
     * @param cabinetCard - cabinet card to remove it from
     * @param fieldToRemove - field to remove so we can remove field even in the middle
     */
    public void removeDataField(int index, CabinetCard cabinetCard, MyString fieldToRemove) {
        Data data = cabinetCard.getCardData().get(index);

        //check if it is really MultipleData object, so we can cast it to this type
        if (data.isRepeatable()) {
            MultipleData multipleData = (MultipleData) data;
            multipleData.removeData(fieldToRemove);
        } else {
            //special case, when you want to delete the only one field - so you are not inserting even an empty string into db
            SimpleData simpleData = (SimpleData) data;
            simpleData.setData(new ArrayList<>());
        }

    }

    /**
     * Method for saving new cabinet card to DB
     * @param selectedDB - actual user DB
     */
    public void addNewCabinetCard(String selectedDB) {
        String collectionName = filingCabinet.getSchema().getTitle();
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(collectionName);
        
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        BasicDBObject newRecord = new BasicDBObject();
        //to indicate if there was a validation error
        boolean validationError = false;
        String message = new String();
        for (SchemaField schemaField : filingCabinet.getSchema().getFields()) {
            String schemaFieldTitle = schemaField.getFieldTitle();
            List<MyString> myStringData = newCabinetCard.getCardData().get(filingCabinet.getSchema().getFields().indexOf(schemaField)).getData();
            BasicDBList list = new BasicDBList();

            //if it is empty and it is required - then it is validation problem
            if (schemaField.isMandatory() && myStringData.isEmpty()) {
                message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("requiredMessage");
                ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                validationError = true;
            }

            //Content validator
            Validator validator = schemaField.getValidator();
            boolean noValidationMessagesSoFar = true;
            boolean noRequiredMessageSoFar = true;
            for (MyString myString : myStringData) {

                //first - validation of required constraint
                if (schemaField.isMandatory() && myString.getString().isEmpty() && noRequiredMessageSoFar) { //if validation is unsuccessful
                    message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("requiredMessage");
                    ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                    validationError = true;
                    noRequiredMessageSoFar = false;
                }

                //if validator is null, then it is only true/false constraint and we dont have to validate it or it is only File constraint
                if (validator != null && noValidationMessagesSoFar) {
                    if (!validator.validate(myString.getString())) {

                        ValidatorType validatorType = validator.getValidatorType();
                        if (validatorType == ValidatorType.NUMBER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeNumberMessage");
                        } else if (validatorType == ValidatorType.LETTER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeLettersMessage");
                        } else if (validatorType == ValidatorType.NUMBER_AND_LETTER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeNumberOrLettersMessage");
                        } else if (validatorType == ValidatorType.REGEX) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("notMatchingRegexMessage") + " " + schemaField.getRegex() + "!";
                        }

                        ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                        noValidationMessagesSoFar = false;
                        validationError = true;
                    }
                }
                BasicDBObject obj = new BasicDBObject();
                obj.put("_id", myString.getId());
                obj.put("value", myString.getString());
                list.add(obj);
            }
            newRecord.put(schemaFieldTitle, list);
        }

        if (validationError) {
            return;
        }

        collection.insert(newRecord);

        //Hide the dialog, because submit was succesful
        RequestContext.getCurrentInstance().execute("addDialog.hide()");

        //refresh whole filingCabinet and erase newCabinetCard
        loadFilingCabinetAndShow(filingCabinet.getSchema().getId(), selectedDB);
    }

    /**
     * Method for editing cabinet card in DB
     * @param selectedDB - actual user DB
     */
    public void editCabinetCard(String selectedDB) {

        String collectionName = filingCabinet.getSchema().getTitle();
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(collectionName);
        
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        BasicDBObject update = new BasicDBObject();
        //to indicate if there was a validation error
        boolean validationError = false;
        String message = new String();
        for (SchemaField schemaField : filingCabinet.getSchema().getFields()) {
            String schemaFieldTitle = schemaField.getFieldTitle();
            List<MyString> myStringData = editCabinetCard.getCardData().get(filingCabinet.getSchema().getFields().indexOf(schemaField)).getData();
            BasicDBList list = new BasicDBList();

            //if it is empty and it is required - then it is validation problem
            if (schemaField.isMandatory() && myStringData.isEmpty()) {
                message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("requiredMessage");
                ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                validationError = true;
            }

            //Content validator
            Validator validator = schemaField.getValidator();
            boolean noValidationMessagesSoFar = true;
            boolean noRequiredMessageSoFar = true;

            for (MyString myString : myStringData) {
                //first - validation of required constraint
                if (schemaField.isMandatory() && myString.getString().isEmpty() && noRequiredMessageSoFar) { //if validation is unsuccessful
                    message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("requiredMessage");
                    ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                    validationError = true;
                    noRequiredMessageSoFar = false;
                }

                //if validator is null, then it is only true/false constraint and we dont have to validate it or it is only File constraint
                if (validator != null && noValidationMessagesSoFar) {
                    if (!validator.validate(myString.getString())) {

                        ValidatorType validatorType = validator.getValidatorType();
                        if (validatorType == ValidatorType.NUMBER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeNumberMessage");
                        } else if (validatorType == ValidatorType.LETTER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeLettersMessage");
                        } else if (validatorType == ValidatorType.NUMBER_AND_LETTER) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("mustBeNumberOrLettersMessage");
                        } else if (validatorType == ValidatorType.REGEX) {
                            message = rb.getString("fieldLabel") + " " + schemaField.getFieldTitle() + " " + rb.getString("notMatchingRegexMessage") + " " + schemaField.getRegex() + "!";
                        }

                        ctx.addMessage("validationMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
                        noValidationMessagesSoFar = false;
                        validationError = true;
                    }
                }

                BasicDBObject obj = new BasicDBObject();
                obj.put("_id", myString.getId());
                obj.put("value", myString.getString());
                list.add(obj);
            }
            update.put(schemaFieldTitle, list);

        }

        if (validationError) {
            return;
        }

        ObjectId cardId = editCabinetCard.getId();
        BasicDBObject match = new BasicDBObject("_id", cardId);

        collection.update(match, new BasicDBObject("$set", update));
        //Hide the dialog, because submit was succesful
        RequestContext.getCurrentInstance().execute("editDialog.hide()");

        //refresh whole filingCabinet and erase editCabinetCard
        loadFilingCabinetAndShow(filingCabinet.getSchema().getId(), selectedDB);

    }

    /**
     * Method for removing cabinet card from DB - also with binary files
     * @param card - card to remove
     * @param selectedDB - actual user DB
     */
    public void removeCabinetCard(CabinetCard card, String selectedDB) {
        String collectionName = filingCabinet.getSchema().getTitle();
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(collectionName);

        BasicDBObject match = new BasicDBObject("_id", card.getId());

        //First, we have to remove files from GridFS if there are any
        BasicDBObject toRemove = (BasicDBObject) collection.findOne(match);
        removeFilesInDocument(toRemove, selectedDB);
        collection.remove(match);

        loadFilingCabinetAndShow(filingCabinet.getSchema().getId(), selectedDB);
    }

    public void copyCabinetCardToEdit(CabinetCard card) {
        editCabinetCard = new CabinetCard(card);
    }

    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
    public DBUtils getDbUtils() {
        return dbUtils;
    }
    
    public void setDbUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }
    
    public SchemaManagerBean getSchemaManager() {
        return schemaManager;
    }
    
    public void setSchemaManager(SchemaManagerBean schemaManager) {
        this.schemaManager = schemaManager;
    }
    
    public FilingCabinet getFilingCabinet() {
        return filingCabinet;
    }
    
    public void setFilingCabinet(FilingCabinet filingCabinet) {
        this.filingCabinet = filingCabinet;
    }
    
    public CabinetCard getNewCabinetCard() {
        return newCabinetCard;
    }
    
    public void setNewCabinetCard(CabinetCard newCabinetCard) {
        this.newCabinetCard = newCabinetCard;
    }
    
    public CabinetCard getEditCabinetCard() {
        return editCabinetCard;
    }
    
    public void setEditCabinetCard(CabinetCard editCabinetCard) {
        this.editCabinetCard = editCabinetCard;
    }
//</editor-fold>
    
    /**
     * Method prepares cabinet card so it has appropriate number of fields according to schema
     * of actual filing cabinet
     */
    private void prepareNewCabinetCard() {
        List<SchemaField> fields = filingCabinet.getSchema().getFields();
        this.newCabinetCard = new CabinetCard();
        for (SchemaField schemaField : fields) {
            if (schemaField.isRepeatable()) {
                this.newCabinetCard.addMultipleData();
            } else {
                this.newCabinetCard.addSimpleData();
            }
        }
    }

    /**
     * Method loads binary files from DB for actual card
     * @param document - object (cabinet card) to load files from
     * @param selectedDB - actual user DB
     * @return List of Map entries - ObjectId of binary file and content whihc can be streamed
     */
    private List<Entry<ObjectId, StreamedContent>> loadBinaryFiles(BasicDBObject document, String selectedDB) {
        Map<ObjectId, StreamedContent> map = new HashMap<>();
        BasicDBList files = (BasicDBList) document.get("Files");
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
        List<Entry<ObjectId, StreamedContent>> result = new ArrayList<>(map.entrySet());
        //now we will sort it by names of the files
        result.sort(new Comparator<Entry<ObjectId, StreamedContent>>() {
            @Override
            public int compare(Entry<ObjectId, StreamedContent> entry1, Entry<ObjectId, StreamedContent> entry2) {
                String str1 = entry1.getValue().getName().toLowerCase();
                String str2 = entry2.getValue().getName().toLowerCase();

                return str1.compareTo(str2);
            }
        });
        return result;
    }
    
    /**
     * Method removes binary files from DB for actual card
     * @param document - object (cabinet card) to delete files from
     * @param selectedDB - actual user DB
     */
    private void removeFilesInDocument(BasicDBObject document, String selectedDB) {
        BasicDBList files = (BasicDBList) document.get("Files");
        //if binaryDataField is defined
        if (files != null){
            //GridFS DB with files
            GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));
            
            for (Object file : files) {
                ObjectId fileId = (ObjectId) file;
                binaryDB.remove(fileId);
            }
        }
    }

}
