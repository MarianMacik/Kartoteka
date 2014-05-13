package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import cz.muni.fi.pv168.validator.LetterValidator;
import cz.muni.fi.pv168.validator.NumberAndLetterValidator;
import cz.muni.fi.pv168.validator.NumberValidator;
import cz.muni.fi.pv168.validator.RegexValidator;
import cz.muni.fi.pv168.validator.TrueFalseValidator;
import cz.muni.fi.pv168.validator.Validator;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.bson.types.ObjectId;
import org.primefaces.context.RequestContext;

/**
 * Bean class for managing everything connected with actual Schema of filing cabinet.
 * @author Marián Macik
 */
@Named
@SessionScoped
public class SchemaManagerBean implements Serializable {

    @Inject
    private DBUtils dbUtils;

    private Schema schema;

    private SchemaField newSchemaField = new SchemaField();

    private Schema newSchemaToAdd = new Schema();

    private SchemaField schemaFieldToEdit = new SchemaField();

    private boolean schemaNameEditMode;

    private String newBinaryDataFieldName;

    /**
     * Method for loading schema from DB.
     * @param schemaId - id of the schema in DB
     * @param selectedDB - actual user DB
     * @return loaded Schema
     */
    public Schema loadSchema(ObjectId schemaId, String selectedDB) {
        Schema newSchema = new Schema();

        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        BasicDBObject object = (BasicDBObject) collection.findOne(new BasicDBObject("_id", schemaId));

        newSchema.setId(object.getObjectId("_id"));

        newSchema.setTitle(object.getString("title"));

        newSchema.setBinaryDataFieldName(object.getString("BinaryDataField"));

        BasicDBList fields = (BasicDBList) object.get("fields");

        //If schema has fields, we will load them
        if (fields != null) {
            for (Object obj : fields) {
                BasicDBObject field = (BasicDBObject) obj;
                SchemaField schemaField = new SchemaField();
                schemaField.DBObjectToSchemaField(field);

                newSchema.getFields().add(schemaField);

            }
        }
        return newSchema;
    }

    /**
     * Method for loading schema and navigating browser to show it
     * @param id - id of the schema
     * @param selectedDB - actual user DB
     * @return page to navigate to
     */
    public String setSchemaAndShow(ObjectId id, String selectedDB) {
        this.schema = loadSchema(id, selectedDB);
        //if user forgot to finish editing - on next load it is not in edit mode
        this.schemaNameEditMode = false;
        return "schema.xhtml?faces-redirect=true";
    }

    /**
     * Method for adding schema field to actual schema.
     * @param selectedDB - actual user DB
     * @return null to tell the browser to stay on the page
     */
    public String addSchemaField(String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());

        //Match for concrete schema
        BasicDBObject match = new BasicDBObject("_id", this.schema.getId());

        boolean validationError = false;

        //control if the name of the adding fied is unique
        List<Entry<ObjectId, String>> actualFields = getSchemaFieldsIdName();
        List<String> fieldNames = new ArrayList<>();

        for (Entry<ObjectId, String> entry : actualFields) {
            fieldNames.add(entry.getValue());
        }
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        ObjectId fieldId = newSchemaField.getId();
        String fieldTitle = newSchemaField.getFieldTitle();
        
        //testing constraints for schema field
        if (fieldTitle.equals("")) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldMustBeNamedMessage"), null));
            validationError = true;
        } else if ((fieldNames.contains(fieldTitle) && !actualFields.contains(new AbstractMap.SimpleEntry<>(fieldId, fieldTitle)))) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldMustBeUnique"), null));
            validationError = true;
        } else if (fieldTitle.contains("$") || fieldTitle.contains(".")) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldInvalidCharacters"), null));
            validationError = true;
        } else if (fieldTitle.equals("Files")){
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldReservedName"), null));
            validationError = true;
        }

        //control if the regular expression is valid
        String constraint = newSchemaField.getConstraint();
        //if it is Regex -> then it is regular expression, so we have to get custom regular expression from regex attribute
        boolean validRegex;
        if (constraint.equals("Regex")) {
            validRegex = validateRegex(newSchemaField.getRegex());

            //if it is not valid - error message is shown
            if (!validRegex) {
                ctx.addMessage("regexMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("regexInvalidMessage"), null));
                validationError = true;
            }
        }

        if (validationError) {
            return null;
        }

        BasicDBObject schemaFieldToAdd = newSchemaField.SchemaFieldToDBObject();

        BasicDBObject update = new BasicDBObject("fields", schemaFieldToAdd);

        collection.update(match, new BasicDBObject("$push", update));

        //we will also insert blank fields to the documents in filingCabinet, if filingCabinet has any documents
        if (filingCabinet.count() != 0) {
            filingCabinet.update(new BasicDBObject(), new BasicDBObject("$set", new BasicDBObject(newSchemaField.getFieldTitle(), new BasicDBList())), false, true);
        }

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);

        //We will erase it, so it is clean for next input
        newSchemaField = new SchemaField();

        //Hide the dialog, because submit was succesful
        RequestContext.getCurrentInstance().execute("addSchemaFieldDialog.hide()");

        return null;
    }

    /**
     * Method for updating schema field.
     * @param selectedDB - actual user DB
     * @return null to tell the browser to stay on the page
     */
    public String editSchemaField(String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());
        //Match for concrete schema
        BasicDBObject match = new BasicDBObject();
        match.put("_id", this.schema.getId());
        match.put("fields._id", schemaFieldToEdit.getId());

        boolean validationError = false;

        //control if the name of the adding fied is unique
        List<Entry<ObjectId, String>> actualFields = getSchemaFieldsIdName();
        List<String> fieldNames = new ArrayList<>();

        for (Entry<ObjectId, String> entry : actualFields) {
            fieldNames.add(entry.getValue());
        }
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        ObjectId fieldId = schemaFieldToEdit.getId();
        String fieldTitle = schemaFieldToEdit.getFieldTitle();
        
        if (fieldTitle.equals("")) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldMustBeNamedMessage"), null));
            validationError = true;
        } else if (fieldNames.contains(fieldTitle) && !actualFields.contains(new AbstractMap.SimpleEntry<>(fieldId, fieldTitle))) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldMustBeUnique"), null));
            validationError = true;
        } else if (fieldTitle.contains("$") || fieldTitle.contains(".")) {
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldInvalidCharacters"), null));
            validationError = true;
        } else if (fieldTitle.equals("Files")){
            ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("schemaFieldReservedName"), null));
            validationError = true;
        }

        //control if the regular expression is valid
        String constraint = schemaFieldToEdit.getConstraint();
        //if it is Regex -> then it is regular expression, so we have to get custom regular expression from regex attribute
        boolean validRegex;
        if (constraint.equals("Regex")) {
            validRegex = validateRegex(schemaFieldToEdit.getRegex());

            //if it is not valid - error message is shown
            if (!validRegex) {
                ctx.addMessage("regexMessageEdit", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("regexInvalidMessage"), null));
                validationError = true;
            }
        }

        if (validationError) {
            return null;
        }

        //Constraint validation - if they are changed, we have to check all the records in db
        //checkConstraintValidation will check all the constraints, if it returns true - everything is OK if false - there is a problem
        if (!checkConstraintValidation(selectedDB)) {
            return null;
        }
        //update of fieldNames
        if (filingCabinet.count() != 0) {
            //first we will obtain schemaField which is not edited so far - it is exactly like in the db - it is in schema property of this class
            SchemaField fieldToEditInDB = new SchemaField();
            for (SchemaField field : schema.getFields()) {
                //we will find it by ID
                if (field.getId().equals(schemaFieldToEdit.getId())) {
                    fieldToEditInDB = field;
                }
            }

            //then we will update fieldNames in stored documents in fileCabinet, but only if name was changed
            if (!fieldToEditInDB.getFieldTitle().equals(schemaFieldToEdit.getFieldTitle())) {
                filingCabinet.update(new BasicDBObject(), new BasicDBObject("$rename", new BasicDBObject(fieldToEditInDB.getFieldTitle(), schemaFieldToEdit.getFieldTitle())), false, true);
            }
        }
        BasicDBObject update = new BasicDBObject();

        update.put("fields.$.fieldTitle", schemaFieldToEdit.getFieldTitle());
        update.put("fields.$.mandatory", schemaFieldToEdit.isMandatory());
        //if constraint is regex - we have to save regex, otherwise type of constraint directly
        if(schemaFieldToEdit.getConstraint().equals("Regex")){
            update.put("fields.$.constraint", schemaFieldToEdit.getRegex());
        }else {
            update.put("fields.$.constraint", schemaFieldToEdit.getConstraint());
        }
        update.put("fields.$.repeatable", schemaFieldToEdit.isRepeatable());

        collection.update(match, new BasicDBObject("$set", update));

        //We will erase it, so it is clean for next input and we will go out from editation mode
        schemaFieldToEdit = new SchemaField();

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);

        //Hide the dialog, because submit was succesful
        RequestContext.getCurrentInstance().execute("editSchemaFieldDialog.hide()");

        return null;
    }

    /**
     * Method for removing schema field from actual schema.
     * @param field - field to remove
     * @param selectedDB - actual user DB
     * @return null to tell the browser to stay on the page
     */
    public String removeSchemaField(SchemaField field, String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        //Match for concrete schema
        BasicDBObject match = new BasicDBObject("_id", this.schema.getId());

        BasicDBObject schemaFieldToRemove = field.SchemaFieldToDBObject();

        BasicDBObject update = new BasicDBObject("fields", schemaFieldToRemove);

        collection.update(match, new BasicDBObject("$pull", update));

        //we will also delete all values that are stored under this key in appropriate collection
        collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());
        collection.update(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject(field.getFieldTitle(), 1)), false, true);

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);

        return null;
    }

    /**
     * Method for adding schema to DB.
     * @param schemas - current schemas - ther ids and names - we use it to determine
     * if schema with such name already exists or not
     * @param selectedDB - actual user DB
     */
    public void addSchema(List<Entry<ObjectId, String>> schemas, String selectedDB) {

        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        if (invalidSchemaName(newSchemaToAdd.getTitle(), schemas, false)) {
            return;
        }

        BasicDBObject schemaToAdd = newSchemaToAdd.schemaToDBObject();

        collection.insert(schemaToAdd);

        //We will erase it, so it is clean for next input
        newSchemaToAdd = new Schema();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(SchemaManagerBean.class.getName()).log(Level.SEVERE, "Problem with redirecting", ex);
        }

    }

    /**
     * Method for removing whole schema - it removes also whole associated filing cabinet of course.
     * @param schemaToRemove - schema/filing cabinet to remove
     * @param selectedDB - actual user DB
     */
    public void removeSchema(Map.Entry<ObjectId, String> schemaToRemove, String selectedDB) {

        //first, we get the schema
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        BasicDBObject match = new BasicDBObject("_id", schemaToRemove.getKey());

        DBCollection cabinetToDelete = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schemaToRemove.getValue());

        //First we have to delete files from GridFS
        //if field for binary data is set, we have to delete files from GridFS
        //First we have to load schema to find if binaryDataField is set
        Schema schema = loadSchema(schemaToRemove.getKey(), selectedDB);
        String binaryDataFieldName = schema.getBinaryDataFieldName();
        if (binaryDataFieldName != null) {
            //all documents in cabinet
            DBCursor cursor = cabinetToDelete.find();
            while (cursor.hasNext()) {
                BasicDBObject document = (BasicDBObject) cursor.next();
                removeFilesInDocument(document, selectedDB);
            }
        }
        //now we will delete whole filing cabinet assigned to this schema
        //if there is cabinet, we will delete it

        if (cabinetToDelete != null) {
            cabinetToDelete.drop();
        }

        collection.remove(match);
    }

    /**
     * Method for updating schema name.
     * @param schemas - current schemas - ther ids and names - we use it to determine
     * if schema with such name already exists or not
     * @param selectedDB - actual user DB
     */
    public void updateSchemaName(List<Map.Entry<ObjectId, String>> schemas, String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        //schema must be named
        if (invalidSchemaName(this.schema.getTitle(), schemas, true)) {
            return;
        }

        String oldSchemaName = new String();
        for (Map.Entry<ObjectId, String> entry : schemas) {
            if (entry.getKey().equals(this.schema.getId())) {
                oldSchemaName = entry.getValue();
                break;
            }
        }

        //Match for concrete schema
        BasicDBObject match = new BasicDBObject();
        match.put("_id", this.schema.getId());

        BasicDBObject update = new BasicDBObject();

        update.put("title", this.schema.getTitle());

        collection.update(match, new BasicDBObject("$set", update));

        //we will also rename appropriate filing cabinet
        if (!oldSchemaName.equals(this.schema.getTitle())) {
            DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(oldSchemaName);

            if (dbUtils.getMongoClient().getDB(selectedDB).collectionExists(filingCabinet.getName())) {
                filingCabinet.rename(this.schema.getTitle());
            }
        }
        this.schema = loadSchema(this.schema.getId(), selectedDB);
        schemaNameEditMode = false;
    }

    /**
     * Method to create a deep copy of schema field which is to be modified. So user won't see
     * changes in table, only in dialog.
     * @param field - field to copy
     */
    public void copySchemaFieldToEdit(SchemaField field) {
        schemaFieldToEdit = new SchemaField(field.getId(), field.getFieldTitle(), field.isMandatory(), field.getConstraint(), field.getRegex(), field.isRepeatable(), field.getValidator());
        System.out.println("nieco");
    }

    /**
     * Method for setting field for binary data.
     * @param selectedDB - actual user DB
     */
    public void setSchemaBinaryDataField(String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());

        //Match for concrete schema
        BasicDBObject match = new BasicDBObject("_id", this.schema.getId());

        

        BasicDBObject update = new BasicDBObject();
        update.put("BinaryDataField", "Files");

        //we will insert binary data Field to Schema
        collection.update(match, new BasicDBObject("$set", update));

        //we will also insert blank fields to the documents in filingCabinet, if filingCabinet has any documents
        if (filingCabinet.count() != 0) {
            //newly added field name
                filingCabinet.update(new BasicDBObject(), new BasicDBObject("$set", new BasicDBObject("Files", new BasicDBList())), false, true);    
        }

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);

    }

    /**
     * Method for unsetting field for binary data - it also deletes any binary files
     * on the cards with this field.
     * @param selectedDB - actual user DB
     */
    public void removeSchemaBinaryDataField(String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        //Match for concrete schema
        BasicDBObject match = new BasicDBObject("_id", this.schema.getId());

        //We will also delete whole key BinaryDataFieldName - because it is not set
        collection.update(match, new BasicDBObject("$unset", new BasicDBObject("BinaryDataField", 1)));

        //we will also delete all values that are stored under this key in appropriate collection
        //But first, we have to delete files in GridFS and after that we can delete links (ObjectIds) to them
        collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());
        //all documents in this collection
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            BasicDBObject document = (BasicDBObject) cursor.next();
            removeFilesInDocument(document, selectedDB);
        }
        //now we can remove field with links (ObjectIds) from documents
        collection.update(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject("Files", 1)), false, true);

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);
    }

    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
    public DBUtils getDbUtils() {
        return dbUtils;
    }
    
    public void setDbUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }
    
    public Schema getSchema() {
        return schema;
    }
    
    public void setSchema(Schema schema) {
        this.schema = schema;
    }
    
    public SchemaField getNewSchemaField() {
        return newSchemaField;
    }
    
    public void setNewSchemaField(SchemaField newSchemaField) {
        this.newSchemaField = newSchemaField;
    }
    
    public Schema getNewSchemaToAdd() {
        return newSchemaToAdd;
    }
    
    public void setNewSchemaToAdd(Schema newSchemaToAdd) {
        this.newSchemaToAdd = newSchemaToAdd;
    }
    
    public SchemaField getSchemaFieldToEdit() {
        return schemaFieldToEdit;
    }
    
    public boolean isSchemaNameEditMode() {
        return schemaNameEditMode;
    }
    
    public void setSchemaNameEditMode(boolean schemaNameEditMode) {
        this.schemaNameEditMode = schemaNameEditMode;
    }
    
    public String getNewBinaryDataFieldName() {
        return newBinaryDataFieldName;
    }
    
    public void setNewBinaryDataFieldName(String newBinaryDataFieldName) {
        this.newBinaryDataFieldName = newBinaryDataFieldName;
    }
//</editor-fold>
    
    /**
     * Method for regex validation
     * @param regex - regex to validate
     * @return true if regex is valid, false otherwise
     */
    private boolean validateRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    /**
     * Method to get actual List of schemas - we use it to determine if such schema already exists
     * or not.
     * @return list of schemas
     */
    private List<Map.Entry<ObjectId, String>> getSchemaFieldsIdName() {
        List<SchemaField> fields = this.schema.getFields();

        List<Entry<ObjectId, String>> result = new ArrayList<>();
        for (SchemaField field : fields) {
            result.add(new AbstractMap.SimpleEntry<>(field.getId(), field.getFieldTitle()));
        }

        return result;
    }

    /**
     * Method for checking constraint validation - if it was changed for particular field.
     * Also sets appropriate messages if there is a problem.
     * @param selectedDB - actual user DB
     * @return true if everything is ok, false if not
     */
    private boolean checkConstraintValidation(String selectedDB) {
        //first we will obtain DBCursor for records in filingCabinet
        DBCollection filingCabinet = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());
        DBCursor cursor = filingCabinet.find();

        //first we will obtain schemaField which is not edited so far - it is exactly like in the db - it is in schema property of this class
        SchemaField fieldToEditInDB = new SchemaField();
        for (SchemaField field : schema.getFields()) {
            //we will find it by ID
            if (field.getId().equals(schemaFieldToEdit.getId())) {
                fieldToEditInDB = field;
            }
        }
        
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        while (cursor.hasNext()) {
            BasicDBObject record = (BasicDBObject) cursor.next();
            //name of the field to be changed
            String schemaFieldName = fieldToEditInDB.getFieldTitle();
            BasicDBList data = (BasicDBList) record.get(schemaFieldName);
            //we have to check if we changed not mandatory to mandatory
            if (!fieldToEditInDB.isMandatory() && schemaFieldToEdit.isMandatory()) {
                if (!validMandatory(data)) {
                    ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("mandatoryViolationMessage"), null));
                    return false;
                }
            }

            String oldConstraint = fieldToEditInDB.getConstraint();
            String newConstraint = schemaFieldToEdit.getConstraint();
            //if we changed type of constraint - we have to check also the constraint
            //if we didnt changed type of constraint but changed the regex - we have to check it also
            if (!oldConstraint.equals(newConstraint) 
                    || 
                    (oldConstraint.equals(newConstraint) && !fieldToEditInDB.getRegex().equals(schemaFieldToEdit.getRegex()))) {
                //File cannot be changed (it is forced on page) so we might have only changed True/False, Numbers, Numbers and letters, Letters, Regex to something from these options
                if (!validConstraint(data, newConstraint, schemaFieldToEdit.getRegex())) {
                    ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("constraintViolationMessage"), null));
                    return false;
                }
            }

            //if we changed repeatable to non-repeatable
            if (fieldToEditInDB.isRepeatable() && !schemaFieldToEdit.isRepeatable()) {
                //we have to check on the size of each data list
                if (!validNonRepeatable(data)) {
                    ctx.addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("repeatableViolationMessage"), null));
                    return false;
                }
            }
        }
        //if validation is ok at every point, we reach this statement
        return true;
    }

    /**
     * Method checks if every data in the field is present - so it is valid
     * according to mandatory constraint.
     * @param list - list of data from DB
     * @return true if it is ok, false otherwise
     */
    private boolean validMandatory(BasicDBList list) {
        //if list is null - the field is missing in DB - so it is violation of constraint
        if (list == null) {
            return false;
        }
        //if field is there, but it is empty - still violation of constraint
        if (list.isEmpty()) {
            return false;
        }

        for (Object object : list) {
            BasicDBObject record = (BasicDBObject) object;
            String value = record.getString("value");
            //if one of the values is empty - it is constraint violation
            if (value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method checks if every data in the field is valid accordint to chosen constraint.
     * @param list - list of data from DB
     * @param constraint - type of constraint to check
     * @param regex - regex to use if constraint is Regex
     * @return true if it is ok, false otherwise
     */
    private boolean validConstraint(BasicDBList list, String constraint, String regex) {
        //if list is empty or it is not present - it is ok, it is valid according to any constraint
        if (list == null) {
            return true;
        }
        if (list.isEmpty()) {
            return true;
        }
        //if we have data - we have to choose validator
        Validator validator;
        switch (constraint) {
            case "Numbers":
                validator = new NumberValidator();
                break;
            case "Letters":
                validator = new LetterValidator();
                break;
            case "Numbers and letters":
                validator = new NumberAndLetterValidator();
                break;
            case "True/False":
                validator = new TrueFalseValidator();
                break;
            default:
                validator = new RegexValidator(regex);
                break;
        }
        for (Object object : list) {
            BasicDBObject record = (BasicDBObject) object;
            String value = record.getString("value");
            //if one of the values is not valid - it is constraint violation
            if (!validator.validate(value)) {
                return false;
            }
        }
        //if everything is ok, return true
        return true;
    }

    /**
     * Method checks if every data in the field is present only once - so it is valid
     * according to non repeatable constraint.
     * @param list - list of data from DB
     * @return true if it is ok, false otherwise
     */
    private boolean validNonRepeatable(BasicDBList list) {
        //if list is null or there are only 0 or 1 values - it is ok, because it is still non repeatable
        if (list == null) {
            return true;
        }
        if (list.size() < 2) {
            return true;
        }

        return false;
    }

    /**
     * Method to control if schema name is valid
     * @param title - title of schema
     * @param schemas - list of current schemas
     * @param editMode - flag to tell if it is in edit mode or 
     * @return true if it is invalid, false otherwise
     */
    private boolean invalidSchemaName(String title, List<Map.Entry<ObjectId, String>> schemas, boolean editMode) {

        List<String> schemaNames = new ArrayList<>();

        for (Map.Entry<ObjectId, String> entry : schemas) {
            schemaNames.add(entry.getValue());
        }
        
        //resource budnle for l10n
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);

        if (title.equals("")) {
            ctx.addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetMustBeNamedMessage"), null));
            return true;
            //if name is too long
        } else if (title.getBytes(Charset.forName("UTF-8")).length > 69){
            ctx.addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetNameTooLong"), null));
            return true;
        } else if (title.contains("$") || title.startsWith("system.")) {
            ctx.addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetInvalidCharacters"), null));
            return true;
            //schema must be unique
        } else if (schemaNames.contains(title)) {
            if (editMode) {
                if (!schemas.contains(new AbstractMap.SimpleEntry<>(this.schema.getId(), this.schema.getTitle()))) {
                    ctx.addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetMustBeUnique"), null));
                    return true;
                }
            } else {
                ctx.addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetMustBeUnique"), null));
                return true;
            }
        } else if (title.equals("Schemas") || title.equals("fs.files") || title.equals("fs.chunks")) {
            ctx.addMessage("schemaValidationErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, rb.getString("filingCabinetReservedName"), null));
            return true;
        }

        return false;
    }

    /**
     * Method removes binary files from DB for actual card
     * @param document - object (cabinet card) to delete files from
     * @param selectedDB - actual user DB
     */
    private void removeFilesInDocument(BasicDBObject document, String selectedDB) {
        BasicDBList files = (BasicDBList) document.get("Files");
        //if binaryDataField is defined
        if (files != null) {
            //GridFS DB with files
            GridFS binaryDB = new GridFS(dbUtils.getMongoClient().getDB(selectedDB));

            for (Object file : files) {
                ObjectId fileId = (ObjectId) file;
                binaryDB.remove(fileId);
            }
        }
    }

}
