package cz.muni.fi.pv168.validator;

/**
 * Validator for numbers. It doesn't accepts any spaces
 * between digits.
 * @author Marián Macik
 */
public class NumberValidator implements Validator {

    public NumberValidator() {
    }

    @Override
    public boolean validate(String value) {
        return value.matches("[0-9]*");
    }
    
    @Override
    public ValidatorType getValidatorType(){
        return ValidatorType.NUMBER;
    }
}
