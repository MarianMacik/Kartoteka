/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Majo
 */
public class CabinetCard {
    
    private ObjectId id;
    
    private List<Data> cardData = new ArrayList<>();

    private List<StreamedContent> files = new ArrayList<>();
    
    public CabinetCard() {
    }

    public CabinetCard(ObjectId id, List<Data> data, List<StreamedContent> files) {
        this.id = id;
        this.cardData = data;
        this.files = files;
    }
    //copy constructor
    public CabinetCard(CabinetCard copy){
        List<Data> newCardData = new ArrayList<>();
        
        for (Data data : copy.cardData) {
            if(data.isRepeatable()){
                newCardData.add(new MultipleData(data.getData()));
            }else{
                if(data.getData().isEmpty()){
                    newCardData.add(new SimpleData());
                }else{
                    newCardData.add(new SimpleData(data.getData().get(0)));
                }
            }
        }
        
        this.id = new ObjectId(copy.id.toString());
        this.cardData = newCardData;
    }
    
    public MyString getTest(int index){
        return cardData.get(index).getData().get(0);
    }
    
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<Data> getCardData() {
        return cardData;
    }

    public void setCardData(List<Data> data) {
        this.cardData = data;
    }
    
    public void addSimpleData(){
        this.cardData.add(new SimpleData(new MyString()));
    }
    
    public void addMultipleData(){
        this.cardData.add(new MultipleData());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.cardData);
        return hash;
    }

    public List<StreamedContent> getFiles() {
        return files;
    }

    public void setFiles(List<StreamedContent> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CabinetCard other = (CabinetCard) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.cardData, other.cardData)) {
            return false;
        }
        return true;
    }
    
    
}
