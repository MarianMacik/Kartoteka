package cz.muni.fi.macik.kartoteka.validator;

/**
 * Interface for all validators.
 * @author Marián Macik
 */
public interface Validator {
    /**
     * Method to perform validation.
     * @param value - value to validate
     * @return true if validation is OK, false otherwise
     */
    public boolean validate(String value);
    
    /**
     * Method to get know which validator is currently present.
     * @return ValidatorType constant
     */
    public ValidatorType getValidatorType();
}
