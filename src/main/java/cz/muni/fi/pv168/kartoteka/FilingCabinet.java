package cz.muni.fi.pv168.kartoteka;

import java.util.List;

/**
 *
 * @author Majo
 */
public class FilingCabinet {
    
    private Schema schema;
    
    private List<CabinetCard> filingCabinetData;

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
    
}
