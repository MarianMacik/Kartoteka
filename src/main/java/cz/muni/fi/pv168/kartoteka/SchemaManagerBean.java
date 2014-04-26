/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import cz.muni.fi.pv168.validator.LetterValidator;
import cz.muni.fi.pv168.validator.NumberAndLetterValidator;
import cz.muni.fi.pv168.validator.NumberValidator;
import cz.muni.fi.pv168.validator.RegexValidator;
import cz.muni.fi.pv168.validator.TrueFalseValidator;
import cz.muni.fi.pv168.validator.Validator;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
 *
 * @author Majo
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

    public Schema loadSchema(ObjectId schemaId, String selectedDB) {
        Schema newSchema = new Schema();

        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        BasicDBObject object = (BasicDBObject) collection.findOne(new BasicDBObject("_id", schemaId));

        newSchema.setId(object.getObjectId("_id"));

        newSchema.setTitle(object.getString("title"));

        newSchema.setBinaryDataFieldName(object.getString("BinaryDataFieldName"));

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

    public String setSchemaAndShow(ObjectId id, String selectedDB) {
        this.schema = loadSchema(id, selectedDB);
        return "schema.xhtml?faces-redirect=true";
    }

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

        if (newSchemaField.getFieldTitle().equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be named!", null));
            validationError = true;
        } else if ((fieldNames.contains(newSchemaField.getFieldTitle()) && !actualFields.contains(new AbstractMap.SimpleEntry<>(newSchemaField.getId(), newSchemaField.getFieldTitle())))
                || newSchemaField.getFieldTitle().equals(schema.getBinaryDataFieldName())) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be unique!", null));
            validationError = true;
        } else if (newSchemaField.getFieldTitle().contains("$") || newSchemaField.getFieldTitle().contains(".")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must not contain '.' or '$'!", null));
            validationError = true;
        }

        //control if the regular expression is valid
        String constraint = newSchemaField.getConstraint();
        //if it is not equal to these constraints -> then it is regular expression
        boolean validRegex = false;
        if (!constraint.equals("True/False") && !constraint.equals("Numbers") && !constraint.equals("Numbers and letters") && !constraint.equals("Letters")) {
            validRegex = validateRegex(constraint);

            //if it is not valid - error message is shown
            if (!validRegex) {
                FacesContext.getCurrentInstance().addMessage("regexMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "Your regular expression is not valid!", null));
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

        if (schemaFieldToEdit.getFieldTitle().equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be named!", null));
            validationError = true;
        } else if (fieldNames.contains(schemaFieldToEdit.getFieldTitle()) && !actualFields.contains(new AbstractMap.SimpleEntry<>(schemaFieldToEdit.getId(), schemaFieldToEdit.getFieldTitle()))) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be unique!", null));
            validationError = true;
        } else if (schemaFieldToEdit.getFieldTitle().contains("$") || schemaFieldToEdit.getFieldTitle().contains(".")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must not contain '.' or '$'!", null));
            validationError = true;
        }

        //control if the regular expression is valid
        String constraint = schemaFieldToEdit.getConstraint();
        //if it is not equal to these constraints -> then it is regular expression
        boolean validRegex = false;
        if (!constraint.equals("True/False") && !constraint.equals("Numbers") && !constraint.equals("Numbers and letters") && !constraint.equals("Letters")) {
            validRegex = validateRegex(constraint);

            //if it is not valid - error message is shown
            if (!validRegex) {
                FacesContext.getCurrentInstance().addMessage("regexMessageEdit", new FacesMessage(FacesMessage.SEVERITY_WARN, "Your regular expression is not valid!", null));
                //refresh, so we have actual data even when we partially changed regex, but we realized it was non-valid
                this.schema = loadSchema(schema.getId(), selectedDB);
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
        update.put("fields.$.constraint", schemaFieldToEdit.getConstraint());
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

    public void addSchema(List<Entry<ObjectId, String>> schemas, String selectedDB) {

        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        if (invalidSchemaName(newSchemaToAdd.getTitle(), schemas, false)) {
            return;
        }

        List<String> schemaNames = new ArrayList<>();

        for (Entry<ObjectId, String> entry : schemas) {
            schemaNames.add(entry.getValue());
        }

        //schema must be named
        if (newSchemaToAdd.getTitle().equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaValidationErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema must be named!", null));
            return;
        } else if (newSchemaToAdd.getTitle().contains("$") || newSchemaToAdd.getTitle().startsWith("system.") || newSchemaToAdd.getTitle().equals("fs.files") || newSchemaToAdd.getTitle().equals("fs.chunks")) {
            FacesContext.getCurrentInstance().addMessage("schemaValidationErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must not contain $ or starts with 'system.' prefix or be 'fs.files' or 'fs.chunks'!", null));
            return;
            //schema must be unique
        } else if (schemaNames.contains(newSchemaToAdd.getTitle())) {
            FacesContext.getCurrentInstance().addMessage("schemaValidationErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must be unique!", null));
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
                removeFilesInDocument(document, binaryDataFieldName, selectedDB);
            }
        }
        //now we will delete whole filing cabinet assigned to this schema
        //if there is cabinet, we will delete it

        if (cabinetToDelete != null) {
            cabinetToDelete.drop();
        }

        collection.remove(match);
    }

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
            filingCabinet.rename(this.schema.getTitle());
        }
        this.schema = loadSchema(this.schema.getId(), selectedDB);
        schemaNameEditMode = false;
    }

    private boolean validateRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    private List<Map.Entry<ObjectId, String>> getSchemaFieldsIdName() {
        List<SchemaField> fields = this.schema.getFields();

        List<Entry<ObjectId, String>> result = new ArrayList<>();
        for (SchemaField field : fields) {
            result.add(new AbstractMap.SimpleEntry<>(field.getId(), field.getFieldTitle()));
        }

        return result;
    }

    public void copySchemaFieldToEdit(SchemaField field) {
        schemaFieldToEdit = new SchemaField(field.getId(), field.getFieldTitle(), field.isMandatory(), field.getConstraint(), field.isRepeatable(), field.getValidator());
    }

    public void copySchemaBinaryDataFieldName(String fieldName) {
        newBinaryDataFieldName = new String(fieldName);
    }

    public void setSchemaBinaryDataFieldName(String selectedDB) {
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

        if (newBinaryDataFieldName.equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be named!", null));
            validationError = true;
        } else if (fieldNames.contains(newBinaryDataFieldName)) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be unique!", null));
            validationError = true;
        } else if (newBinaryDataFieldName.contains("$") || newBinaryDataFieldName.contains(".")) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must not contain '.' or '$'!", null));
            validationError = true;
        }

        if (validationError) {
            return;
        }

        BasicDBObject update = new BasicDBObject();
        update.put("BinaryDataFieldName", newBinaryDataFieldName);

        //we will update name of the field in schema collection, if it is not present, it is created
        collection.update(match, new BasicDBObject("$set", update));

        //we will also insert blank fields to the documents in filingCabinet, if filingCabinet has any documents
        if (filingCabinet.count() != 0) {
            //newly added field name
            if (!newBinaryDataFieldName.equals(this.schema.getBinaryDataFieldName()) && this.schema.getBinaryDataFieldName() == null) {
                filingCabinet.update(new BasicDBObject(), new BasicDBObject("$set", new BasicDBObject(newBinaryDataFieldName, new BasicDBList())), false, true);
            }

            //if new name is different and the field was set previously -> old name is not null -> we have to rename it
            if (!newBinaryDataFieldName.equals(this.schema.getBinaryDataFieldName()) && this.schema.getBinaryDataFieldName() != null) {
                filingCabinet.update(new BasicDBObject(), new BasicDBObject("$rename", new BasicDBObject(this.schema.getBinaryDataFieldName(), newBinaryDataFieldName)), false, true);
            }
        }

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);

        //Hide the dialog, because submit was succesful
        RequestContext.getCurrentInstance().execute("editBinaryDataFieldNameDialog.hide()");
    }

    public void removeSchemaBinaryDataField(String selectedDB) {
        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");
        //Match for concrete schema
        BasicDBObject match = new BasicDBObject("_id", this.schema.getId());

        //We will also delete whole key BinaryDataFieldName - because it is not set
        collection.update(match, new BasicDBObject("$unset", new BasicDBObject("BinaryDataFieldName", 1)));

        //we will also delete all values that are stored under this key in appropriate collection
        //But first, we have to delete files in GridFS and after that we can delete links (ObjectIds) to them
        collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schema.getTitle());
        //all documents in this collection
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            BasicDBObject document = (BasicDBObject) cursor.next();
            removeFilesInDocument(document, this.schema.getBinaryDataFieldName(), selectedDB);
        }
        //now we can remove field with links (ObjectIds) from documents
        collection.update(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject(this.schema.getBinaryDataFieldName(), 1)), false, true);

        //actualisation of table in browser
        this.schema = loadSchema(schema.getId(), selectedDB);
    }

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

        while (cursor.hasNext()) {
            BasicDBObject record = (BasicDBObject) cursor.next();
            //name of the field to be changed
            String schemaFieldName = fieldToEditInDB.getFieldTitle();
            BasicDBList data = (BasicDBList) record.get(schemaFieldName);
            //we have to check if we changed not mandatory to mandatory
            if (!fieldToEditInDB.isMandatory() && schemaFieldToEdit.isMandatory()) {
                if (!validMandatory(data)) {
                    FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "Some of the documents don't have data - mandatory violation", null));
                    return false;
                }
            }

            //if we changed constraint - we have to check also the constraint
            if (!fieldToEditInDB.getConstraint().equals(schemaFieldToEdit.getConstraint())) {
                //File cannot be changed (it is forced on page) so we might have only changed True/False, Numbers, Numbers and letters, Letters, Regex to something from these options
                if (!validConstraint(data, schemaFieldToEdit.getConstraint())) {
                    FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "Some of the documents don't match desired constraint", null));
                    return false;
                }
            }

            //if we changed repeatable to non-repeatable
            if (fieldToEditInDB.isRepeatable() && !schemaFieldToEdit.isRepeatable()) {
                //we have to check on the size of each data list
                if (!validNonRepeatable(data)) {
                    FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "Some of the documents contain repeatable data", null));
                    return false;
                }
            }
        }
        //if validation is ok at every point, we reach this statement
        return true;
    }

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

    private boolean validConstraint(BasicDBList list, String constraint) {
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
                validator = new RegexValidator(constraint);
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

    private boolean invalidSchemaName(String title, List<Map.Entry<ObjectId, String>> schemas, boolean editMode) {

        List<String> schemaNames = new ArrayList<>();

        for (Map.Entry<ObjectId, String> entry : schemas) {
            schemaNames.add(entry.getValue());
        }

        if (title.equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema must be named!", null));
            return true;
        } else if (title.contains("$") || title.startsWith("system.") || title.equals("fs.files") || title.equals("fs.chunks")) {
            FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must not contain $ or starts with 'system.' prefix or be 'fs.files' or 'fs.chunks'!", null));
            return true;
            //schema must be unique
        } else if (schemaNames.contains(title)) {
            if (editMode) {
                if (!schemas.contains(new AbstractMap.SimpleEntry<>(this.schema.getId(), this.schema.getTitle()))) {
                    FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must be unique!", null));
                    return true;
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must be unique!", null));
                return true;
            }
        }

        return false;
    }

    public void removeFilesInDocument(BasicDBObject document, String binaryDataFieldName, String selectedDB) {
        BasicDBList files = (BasicDBList) document.get(binaryDataFieldName);
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
