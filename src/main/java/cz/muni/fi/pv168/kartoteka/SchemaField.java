/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import cz.muni.fi.pv168.validator.LetterValidator;
import cz.muni.fi.pv168.validator.NumberAndLetterValidator;
import cz.muni.fi.pv168.validator.NumberValidator;
import cz.muni.fi.pv168.validator.RegexValidator;
import cz.muni.fi.pv168.validator.Validator;
import org.bson.types.ObjectId;

/**
 *
 * @author Majo
 */
public class SchemaField {

    private ObjectId id = new ObjectId();

    private String fieldTitle = new String();

    private boolean mandatory;

    private String constraint = new String();

    private boolean repeatable;

    private Validator validator;

    SchemaField(ObjectId id, String fieldTitle, boolean mandatory, String constraint, boolean repeatable) {
        this.id = id;
        this.fieldTitle = fieldTitle;
        this.mandatory = mandatory;
        this.constraint = constraint;
        this.repeatable = repeatable;
    }

    SchemaField() {
    }

    public BasicDBObject SchemaFieldToDBObject() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("_id", id);
        obj.put("fieldTitle", fieldTitle);
        obj.put("mandatory", mandatory);
        obj.put("constraint", constraint);
        obj.put("repeatable", repeatable);
        return obj;
    }

    public void DBObjectToSchemaField(BasicDBObject object) {
        id = object.getObjectId("_id");
        fieldTitle = object.getString("fieldTitle");
        mandatory = object.getBoolean("mandatory");
        constraint = object.getString("constraint");
        repeatable = object.getBoolean("repeatable");

        //if it is not True/False - we have to insert validator
        if (!constraint.equals("True/False")) {
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
                default:
                    validator = new RegexValidator(constraint);
                    break;
            }
        }

    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

}
