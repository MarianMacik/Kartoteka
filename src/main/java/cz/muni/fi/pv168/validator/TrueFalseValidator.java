/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.validator;


public class TrueFalseValidator implements Validator {

    @Override
    public boolean validate(String value) {
        return value.matches("True|False");
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.TRUE_FALSE;
    }
    
}
