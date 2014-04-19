/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import java.util.List;


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
