package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import cz.muni.fi.pv168.validator.LetterValidator;
import cz.muni.fi.pv168.validator.NumberAndLetterValidator;
import cz.muni.fi.pv168.validator.NumberValidator;
import cz.muni.fi.pv168.validator.RegexValidator;
import cz.muni.fi.pv168.validator.TrueFalseValidator;
import cz.muni.fi.pv168.validator.Validator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import org.bson.types.ObjectId;

/**
 * Class represents one schema field of schema. It has title, mandatory constraint, constraint
 * for content and repeatable constraint.
 * @author Marián Macik
 */
public class SchemaField {

    private ObjectId id = new ObjectId();

    private String fieldTitle = new String();

    private boolean mandatory;
    //constraint for content to be saved
    private String constraint = new String();
    //if constraint is regex then here is the actual regex
    private String regex = new String();

    private boolean repeatable;
    //associated validator for constraint
    private Validator validator;

    SchemaField(ObjectId id, String fieldTitle, boolean mandatory, String constraint, String regex, boolean repeatable, Validator validator) {
        this.id = id;
        this.fieldTitle = fieldTitle;
        this.mandatory = mandatory;
        this.constraint = constraint;
        this.regex = regex;
        this.repeatable = repeatable;
        this.validator = validator;
    }

    SchemaField() {
    }

    /**
     * Method transforms SchemaField to DB representation.
     * @return BasicDBObject which represents SchemaField in DB.
     */
    public BasicDBObject SchemaFieldToDBObject() {
        BasicDBObject obj = new BasicDBObject();
        obj.put("_id", id);
        obj.put("fieldTitle", fieldTitle);
        obj.put("mandatory", mandatory);
        
        //if it is regex - we will save to db only regex - it is sufficient
        if(constraint.equals("Regex")){
            obj.put("constraint", regex);
        } else {
            obj.put("constraint", constraint);
        }
        
        obj.put("repeatable", repeatable);
        return obj;
    }

    /**
     * Method to transform SchemaField in DB to SchemaField object in memory.
     * @param object - object in DB to transform
     */
    public void DBObjectToSchemaField(BasicDBObject object) {
        id = object.getObjectId("_id");
        fieldTitle = object.getString("fieldTitle");
        mandatory = object.getBoolean("mandatory");
        
        //if it is regex - we will save to constraint Regex and to regex - actual regex from db
        String dbConstraint = object.getString("constraint");
        if(!dbConstraint.equals("Numbers") && !dbConstraint.equals("Letters") && !dbConstraint.equals("Numbers and letters") && !dbConstraint.equals("True/False")){
            constraint = "Regex";
            regex = dbConstraint;
        } else {
            constraint = object.getString("constraint");
        }
        repeatable = object.getBoolean("repeatable");

        
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

    }

    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
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
    
    //getter for internationalized version
    public String getMyConstraint() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);
        
        if(!constraint.equals("Numbers") && !constraint.equals("Letters") && !constraint.equals("Numbers and letters") && !constraint.equals("True/False"))
            return regex;
        
        if(!constraint.isEmpty() && constraint != null){
            return rb.getString(constraint);
        }
        
        return "";
    }
    
    public void setMyConstraint(String constraint) {
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
    
    public String getRegex() {
        return regex;
    }
    
    public void setRegex(String regex) {
        this.regex = regex;
    }
//</editor-fold>
}
