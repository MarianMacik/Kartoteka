package cz.muni.fi.pv168.validator;

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
        
        //good string including unicode characters and numbers
        String goodTestValue = "Dobrý string číslo 1";
        //empty string is also OK, because this validator does not check mandatory constraint
        String goodTestValue2 = "";
        NumberAndLetterValidator validator = new NumberAndLetterValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
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
