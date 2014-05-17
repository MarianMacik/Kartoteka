package cz.muni.fi.macik.kartoteka.validator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marián Macik
 */
public class TrueFalseValidatorTest {
    
    public TrueFalseValidatorTest() {
    }

    /**
     * Test of validate method, of class TrueFalseValidator.
     */
    @Test
    public void testValidate() {
        //numbers and letters - bad
        String badTestValue = "Test case 1";
        //Unicode alphabet characters - bad
        String badTestValue2 = "Testovací string číslo 2";
        //some other characters - bad
        String badTestValue3 = "$ € Ł";
        //bad form of true/false
        String badTestValue4 = "true";
        String badTestValue5 = "false";
        
        //good string
        String goodTestValue = "True";
        //good string
        String goodTestValue2 = "False";
        //empty string is also OK, because original TrueFalseValidator does not check mandatory constraint
        String goodTestValue3 = "";
        TrueFalseValidator validator = new TrueFalseValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        assertEquals(false, validator.validate(badTestValue4));
        assertEquals(false, validator.validate(badTestValue5));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
        assertEquals(true, validator.validate(goodTestValue3));
    }

    /**
     * Test of getValidatorType method, of class TrueFalseValidator.
     */
    @Test
    public void testGetValidatorType() {
        TrueFalseValidator validator = new TrueFalseValidator();
        ValidatorType expResult = ValidatorType.TRUE_FALSE;
        ValidatorType result = validator.getValidatorType();
        assertEquals(expResult, result);
    }
    
}
