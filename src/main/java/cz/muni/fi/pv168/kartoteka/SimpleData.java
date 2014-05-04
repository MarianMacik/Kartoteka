package cz.muni.fi.pv168.kartoteka;

import java.util.List;

/**
 * Class represents data when repeatable constraint is not set on particular
 * schema field.
 * @author Marián Macik
 */
public class SimpleData extends Data {

    public SimpleData() {
        this.data.clear();
        
    }
    
    public SimpleData(MyString data) {
        this.data.clear();
        this.data.add(new MyString(data.getId(),data.getString()));
    }
    
    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public List<MyString> getData() {
        return data;
    }

    public void setData(List<MyString> data) {
        this.data.clear();
        if(!data.isEmpty()){
            this.data.add(new MyString(data.get(0).getId(),data.get(0).getString()));
        }
        
    }
}
