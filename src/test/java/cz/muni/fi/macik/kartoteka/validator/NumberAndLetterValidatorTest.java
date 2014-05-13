package cz.muni.fi.macik.kartoteka.validator;

import cz.muni.fi.macik.kartoteka.validator.ValidatorType;
import cz.muni.fi.macik.kartoteka.validator.NumberAndLetterValidator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marián Macik
 */
public class NumberAndLetterValidatorTest {
    
    public NumberAndLetterValidatorTest() {
    }
    
    /**
     * Test of validate method, of class NumberAndLetterValidator.
     */
    @Test
    public void testValidate() {
        //not numbers and letters - bad
        String badTestValue = "| & @";
        //numbers with bad formats
        String badTestValue2 = "Bad example of number -1. 456  - 3.14";
        
        //good string including unicode characters and numbers
        String goodTestValue = "Dobrý string číslo 1";
        //more numbers are also permitted
        String goodTestValue2 = "Číslo 1.458 a číslo -3.14";
        
        //empty string is also OK, because this validator does not check mandatory constraint
        String goodTestValue3 = "";
        NumberAndLetterValidator validator = new NumberAndLetterValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
        assertEquals(true, validator.validate(goodTestValue3));
    }

    /**
     * Test of getValidatorType method, of class NumberAndLetterValidator.
     */
    @Test
    public void testGetValidatorType() {
        NumberAndLetterValidator validator = new NumberAndLetterValidator();
        ValidatorType expResult = ValidatorType.NUMBER_AND_LETTER;
        ValidatorType result = validator.getValidatorType();
        assertEquals(expResult, result);
    }
    
}
