/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.validator;

/**
 *
 * @author Majo
 */
public interface Validator {
    public boolean validate(String value);
    
    public ValidatorType getValidatorType();
}
