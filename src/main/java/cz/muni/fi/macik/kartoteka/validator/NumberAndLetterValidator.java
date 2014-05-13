package cz.muni.fi.macik.kartoteka.validator;

/**
 * Validator for numers and letters. It accepts also unicode letters
 * and space between characters.
 * @author Marián Macik
 */
public class NumberAndLetterValidator implements Validator {

    @Override
    public boolean validate(String value) {
        return value.matches("([\\p{L} ]*(-?\\d+(\\.[\\d]+)?)*)*");
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.NUMBER_AND_LETTER;
    }
    
}
