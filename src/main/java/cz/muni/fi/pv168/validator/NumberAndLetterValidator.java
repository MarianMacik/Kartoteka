package cz.muni.fi.pv168.validator;

/**
 * Validator for numers and letters. It accepts also unicode letters
 * and space between characters.
 * @author Marián Macik
 */
public class NumberAndLetterValidator implements Validator {

    @Override
    public boolean validate(String value) {
        return value.matches("[\\p{L} 0-9]*");
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.NUMBER_AND_LETTER;
    }
    
}
