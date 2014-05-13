package cz.muni.fi.macik.kartoteka.entities;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import org.bson.types.ObjectId;

/**
 * Class acts as a wrapper class for String data type because when JSF wants to set
 * a value to String - it is looking for setter, thus we have to provide getter
 * and setter.
 * @author Marián Macik
 */
public class MyString {
    
    private ObjectId id = new ObjectId();
    
    private String string = new String();

    public MyString(ObjectId id, String string) {
        this.id = id;
        this.string = string;
    }

    public MyString() {
    }

    /**
     * Method gets localized value of True/False in a card.
     * It is only used where True/False constraint is selected.
     * @return appropriate translation of True/False
     */
    public String getLocalizedString() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Locale locale = ctx.getViewRoot().getLocale();
        ResourceBundle rb = ResourceBundle.getBundle("language", locale);
        
        if(string.equals("True"))
            return rb.getString("trueLabel");
        else{
            return rb.getString("falseLabel");
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="GETTERS/SETTERS/EQUALS AND HASHCODE">
    public String getString() {
        return string;
    }
    
    public void setString(String string) {
        this.string = string;
    }
    
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.id);
        hash = 73 * hash + Objects.hashCode(this.string);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyString other = (MyString) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.string, other.string)) {
            return false;
        }
        return true;
    }
//</editor-fold>
    
    
}
