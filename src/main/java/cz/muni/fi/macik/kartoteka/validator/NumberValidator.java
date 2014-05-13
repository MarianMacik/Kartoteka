package cz.muni.fi.macik.kartoteka.validator;

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
        return value.matches("(-?\\d+(\\.[\\d]+)?)?");
    }
    
    @Override
    public ValidatorType getValidatorType(){
        return ValidatorType.NUMBER;
    }
}
