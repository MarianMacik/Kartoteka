/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.validator;


public class RegexValidator implements Validator {

    private String regex;

    public RegexValidator() {
    }

    public RegexValidator(String regex) {
        this.regex = regex;
    }
    
    @Override
    public boolean validate(String value) {
        return value.matches(regex);
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.REGEX;
    }
    
}
