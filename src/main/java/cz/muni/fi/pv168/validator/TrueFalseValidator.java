package cz.muni.fi.pv168.validator;

/**
 * Validator for True/False. It accepts also blank value because
 * blank value is only controlled when mandatory constraint is set.
 * @author Marián Macik
 */
public class TrueFalseValidator implements Validator {

    @Override
    public boolean validate(String value) {
        return value.matches("True|False|");
    }

    @Override
    public ValidatorType getValidatorType() {
        return ValidatorType.TRUE_FALSE;
    }
    
}
