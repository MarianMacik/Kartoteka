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

    public Schema loadSchema(ObjectId schemaId, String selectedDB) {
        Schema newSchema = new Schema();

        DBCollection collection = dbUtils.getMongoClient().getDB(selectedDB).getCollection("Schemas");

        BasicDBObject object = (BasicDBObject) collection.findOne(new BasicDBObject("_id", schemaId));

        newSchema.setId(object.getObjectId("_id"));

        newSchema.setTitle(object.getString("title"));

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
        } else if (fieldNames.contains(newSchemaField.getFieldTitle()) && !actualFields.contains(new AbstractMap.SimpleEntry<>(newSchemaField.getId(), newSchemaField.getFieldTitle()))) {
            FacesContext.getCurrentInstance().addMessage("schemaFieldTitleMessageAdd", new FacesMessage(FacesMessage.SEVERITY_WARN, "SchemaField must be unique!", null));
            validationError = true;
        } else if(newSchemaField.getFieldTitle().contains("$") || newSchemaField.getFieldTitle().contains(".")){
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
        } else if(schemaFieldToEdit.getFieldTitle().contains("$") || schemaFieldToEdit.getFieldTitle().contains(".")){
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
        } else if (newSchemaToAdd.getTitle().contains("$") || newSchemaToAdd.getTitle().startsWith("system.")) {
            FacesContext.getCurrentInstance().addMessage("schemaValidationErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must not contain $ or starts with 'system.' prefix!", null));
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

        //now we will delete whole filing cabinet assigned to this schema
        //if there is cabinet, we will delete it
        DBCollection cabinetToDelete = dbUtils.getMongoClient().getDB(selectedDB).getCollection(schemaToRemove.getValue());
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
        schemaFieldToEdit = new SchemaField(field.getId(), field.getFieldTitle(), field.isMandatory(), field.getConstraint(), field.isRepeatable());
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

    private boolean invalidSchemaName(String title, List<Map.Entry<ObjectId, String>> schemas, boolean editMode) {

        List<String> schemaNames = new ArrayList<>();

        for (Map.Entry<ObjectId, String> entry : schemas) {
            schemaNames.add(entry.getValue());
        }

        if (title.equals("")) {
            FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema must be named!", null));
            return true;
        } else if (title.contains("$") || title.startsWith("system.")) {
            FacesContext.getCurrentInstance().addMessage("schemaNameErrorMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Schema name must not contain $ or starts with 'system.' prefix!", null));
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

}
