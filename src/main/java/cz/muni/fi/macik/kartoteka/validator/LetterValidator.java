package cz.muni.fi.macik.kartoteka.validator;

/**
 * Validator for letters. It accepts also unicode letters
 * and space between characters.
 * @author Marián Macik
 */
public class LetterValidator implements Validator {

    @Override
    public boolean validate(String value) {
        return value.matches("[\\p{L} ]*");
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.LETTER;
    }
    
}
