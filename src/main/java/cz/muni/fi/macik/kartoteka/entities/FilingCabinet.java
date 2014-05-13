package cz.muni.fi.macik.kartoteka.entities;

import java.util.List;

/**
 * Class represents filing cabinet - list of cabinet cards which hold the data
 * @author Marián Macik
 */
public class FilingCabinet {
    
    private Schema schema;
    
    private List<CabinetCard> filingCabinetData;

    //<editor-fold defaultstate="collapsed" desc="GETTERS AND SETTERS">
    public Schema getSchema() {
        return schema;
    }
    
    public void setSchema(Schema schema) {
        this.schema = schema;
    }
    
    public List<CabinetCard> getFilingCabinetData() {
        return filingCabinetData;
    }
    
    public void setFilingCabinetData(List<CabinetCard> filingCabinetData) {
        this.filingCabinetData = filingCabinetData;
    }
//</editor-fold>
    
}
