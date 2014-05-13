package cz.muni.fi.macik.kartoteka.entities;

import java.util.List;

/**
 * Class represents data when repeatable constraint is set on particular
 * schema field.
 * @author Marián Macik
 */
public class MultipleData extends Data {

    public MultipleData() {
        this.data.add(new MyString());
    }

    public MultipleData(List<MyString> data){
        for (MyString string : data) {
            this.data.add(new MyString(string.getId(),string.getString()));
        }
    }
    
    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public List<MyString> getData() {
        return data;
    }

    public void setData(List<MyString> data) {
        this.data = data;
    }
    
    public void addData(){
        this.data.add(new MyString());
    }
    
    public void removeData(MyString toRemove){
        this.data.remove(toRemove);
    }
    
}
