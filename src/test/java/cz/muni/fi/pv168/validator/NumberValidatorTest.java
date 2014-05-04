package cz.muni.fi.pv168.validator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marián Macik
 */
public class NumberValidatorTest {
    
    public NumberValidatorTest() {
    }
    
    /**
     * Test of validate method, of class NumberValidator.
     */
    @Test
    public void testValidate() {
        //numbers and letters - bad
        String badTestValue = "Test case 1";
        //Unicode alphabet characters - bad
        String badTestValue2 = "Testovací string číslo 2";
        //some other characters - bad
        String badTestValue3 = "$ € Ł";
        //two numbers separated by space - bad because for more numbers user can choose repeatable constraint to true
        String badTestValue4 = "123 456";
        
        //good string - only one number without spaces
        String goodTestValue = "123";
        //empty string is also OK, because this validator does not check mandatory constraint
        String goodTestValue2 = "";
        NumberValidator validator = new NumberValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        assertEquals(false, validator.validate(badTestValue4));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
    }

    /**
     * Test of getValidatorType method, of class NumberValidator.
     */
    @Test
    public void testGetValidatorType() {
        NumberValidator validator = new NumberValidator();
        ValidatorType expResult = ValidatorType.NUMBER;
        ValidatorType result = validator.getValidatorType();
        assertEquals(expResult, result);
    }
    
}
