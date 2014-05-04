package cz.muni.fi.pv168.validator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marián Macik
 */
public class LetterValidatorTest {
    
    public LetterValidatorTest() {
    }

    /**
     * Test of validate method, of class LetterValidator.
     */
    @Test
    public void testValidate() {
        //only numbers - bad
        String badTestValue = "1458";
        //numbers and letters - bad
        String badTestValue2 = "Test case 1";
        //Unicode alphabet characters - bad
        String badTestValue3 = "Testovací string číslo 2";
        //some other characters - bad
        String badTestValue4 = "$ € Ł";
        
        //good string including unicode characters
        String goodTestValue = "Dobrý string číslo jeden";
        //empty string is also OK, because this validator does not check mandatory constraint
        String goodTestValue2 = "";
        LetterValidator validator = new LetterValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        assertEquals(false, validator.validate(badTestValue4));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
        
    }

    /**
     * Test of getValidatorType method, of class LetterValidator.
     */
    @Test
    public void testGetValidatorType() {
        LetterValidator validator = new LetterValidator();
        ValidatorType expResult = ValidatorType.LETTER;
        ValidatorType result = validator.getValidatorType();
        assertEquals(expResult, result);
    }
    
}
