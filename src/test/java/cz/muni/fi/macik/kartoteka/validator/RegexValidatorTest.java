package cz.muni.fi.macik.kartoteka.validator;

import cz.muni.fi.macik.kartoteka.validator.ValidatorType;
import cz.muni.fi.macik.kartoteka.validator.RegexValidator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marián Macik
 */
public class RegexValidatorTest {
    
    public RegexValidatorTest() {
    }

    /**
     * Test of validate method, of class RegexValidator.
     * This test is trying to do exact same validation as LetterValidator
     * but using the RegexValidator with given regex.
     */
    @Test
    public void testValidateLetterWithRegex() {
        RegexValidator validator = new RegexValidator("[\\p{L} ]*");
        
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
        //empty string is also OK, because original LetterValidator does not check mandatory constraint
        String goodTestValue2 = "";
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        assertEquals(false, validator.validate(badTestValue4));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
    }
    
    /**
     * Test of validate method, of class RegexValidator.
     * This test is trying to do exact same validation as NumberAndLetterValidator
     * but using the RegexValidator with given regex.
     */
    @Test
    public void testValidateNumberAndLetterWithRegex() {
        RegexValidator validator = new RegexValidator("([\\p{L} ]*(-?\\d+(\\.[\\d]+)?)*)*");
        
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
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
        assertEquals(true, validator.validate(goodTestValue3));
    }
    
    /**
     * Test of validate method, of class RegexValidator.
     * This test is trying to do exact same validation as NumberValidator
     * but using the RegexValidator with given regex.
     */
    @Test
    public void testValidateNumberWithRegex() {
        RegexValidator validator = new RegexValidator("(-?\\d+(\\.[\\d]+)?)?");
        
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
     * Test of validate method, of class RegexValidator.
     * This test is trying to do exact same validation as TrueFalseValidator
     * but using the RegexValidator with given regex.
     */
    @Test
    public void testValidateTrueFalseWithRegex() {
        RegexValidator validator = new RegexValidator("True|False|");
        
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
     * Test of validate method, of class RegexValidator.
     * This test is trying to do email validation using given regex.
     */
    @Test
    public void testValidateCustomWithRegex() {
        RegexValidator validator = new RegexValidator("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$");
        
        //two @
        String badTestValue = "macik@marian@post.com";
        //two dost side by side
        String badTestValue2 = "marian..macik@centrum.com";
        //missing @
        String badTestValue3 = "macik.marian_mails.com";
        
        //good string
        String goodTestValue = "me@home.com";
        //good string
        String goodTestValue2 = "you.and.me@server.edu";
        
        assertEquals(false, validator.validate(badTestValue));
        assertEquals(false, validator.validate(badTestValue2));
        assertEquals(false, validator.validate(badTestValue3));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
    }

    /**
     * Test of getValidatorType method, of class RegexValidator.
     */
    @Test
    public void testGetValidatorType() {
        RegexValidator validator = new RegexValidator();
        ValidatorType expResult = ValidatorType.REGEX;
        ValidatorType result = validator.getValidatorType();
        assertEquals(expResult, result);
    }
    
}
