/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.SessionScoped; 
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
 
@Named
@SessionScoped
public class LanguageBean implements Serializable{
 
        private String localeCode = "en";
 
        private static Map<String,Object> countries;
    
        static{
                countries = new LinkedHashMap<>();
                countries.put("English", new Locale("en")); //label, value
                countries.put("Slovenčina", new Locale("sk"));
        }
 
        public Map<String, Object> getCountriesInMap() {
                return countries;
        }
 
        public String getLocaleCode() {
        return localeCode;
        }
 
 
        public void setLocaleCode(String localeCode) {
                this.localeCode = localeCode;
        }
 
        //value change event listener
        public void countryLocaleCodeChanged(ValueChangeEvent e) { 
                String newLocaleValue = e.getNewValue().toString();
 
                //loop country map to compare the locale code
        for (Map.Entry<String, Object> entry : countries.entrySet()) { 
            if (entry.getValue().toString().equals(newLocaleValue)) {
                        FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale)entry.getValue());
                }
        }
        }
 
}