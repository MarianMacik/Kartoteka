/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import java.util.List;


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
