package cz.muni.fi.pv168.kartoteka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.primefaces.model.StreamedContent;

/**
 * Class represents one card in filing cabinet 
 * @author Marián Macik
 */
public class CabinetCard {
    
    private ObjectId id;
    
    private List<Data> cardData = new ArrayList<>();
    //references on binary files
    private List<Entry<ObjectId,StreamedContent>> files = new ArrayList<>();
    
    public CabinetCard() {
    }

    public CabinetCard(ObjectId id, List<Data> data, List<Entry<ObjectId,StreamedContent>> files) {
        this.id = id;
        this.cardData = data;
        this.files = files;
    }
    
    /**
     * Copy constructor is doing deep copy of the whole object
     * It is used when we want to edit card in filing cabinet, so we dont change
     * data in table but only in edit dialog
     * @param copy - object which we want to copy
     */
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
    
    
    //<editor-fold defaultstate="collapsed" desc="GETTERS/SETTERS/EQUALS AND HASHCODE">
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
    
    public List<Entry<ObjectId,StreamedContent>> getFiles() {
        return files;
    }
    
    public void setFiles(List<Entry<ObjectId,StreamedContent>> files) {
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
//</editor-fold>
    
    
}
