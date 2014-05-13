package cz.muni.fi.macik.kartoteka.validator;

/**
 * Validator for regex. It validates according to a given regex.
 * @author Marián Macik
 */
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
