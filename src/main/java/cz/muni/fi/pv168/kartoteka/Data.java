package cz.muni.fi.pv168.kartoteka;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing data for one schema field in filing cabinet
 * Data is stored in list because multiple values can be saved
 * for one schema field
 * @author Marián Macik
 */
public abstract class Data {
    
    protected List<MyString> data = new ArrayList<>();
    
    public abstract List<MyString> getData();
    
    /**
     * Method to determine if it is MultipleData object or SimpleData object.
     * @return true if MultipleData, false if SimpleData
     */
    public abstract boolean isRepeatable();

}
