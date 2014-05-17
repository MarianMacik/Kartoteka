package cz.muni.fi.macik.kartoteka.validator;

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
        //negative number with space
        String badTestValue5 = "-1. 45";
        //negative number with two decimal points
        String badTestValue6 = "-1.45.4";
        
        //good string - only one number without spaces
        String goodTestValue = "123";
        //empty string is also OK, because this validator does not check mandatory constraint
        String goodTestValue2 = "";
        //negative number with decimal point
        String goodTestValue3 = "-3.14";
        //negative number without decimal point
        String goodTestValue4 = "-3";
        
        NumberValidator validator = new NumberValidator();
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        assertEquals(false, validator.validate(badTestValue4));
        assertEquals(false, validator.validate(badTestValue5));
        assertEquals(false, validator.validate(badTestValue6));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
        assertEquals(true, validator.validate(goodTestValue3));
        assertEquals(true, validator.validate(goodTestValue4));
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
