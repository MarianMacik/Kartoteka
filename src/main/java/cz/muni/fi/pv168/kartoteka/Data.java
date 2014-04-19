/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.kartoteka;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Majo
 */
public abstract class Data {
    
    protected List<MyString> data = new ArrayList<>();
    
    public abstract List<MyString> getData();
    
    public abstract boolean isRepeatable();

}
