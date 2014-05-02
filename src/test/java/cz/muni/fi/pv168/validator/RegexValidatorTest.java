/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.validator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Majo
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
        RegexValidator validator = new RegexValidator("[\\p{L} 0-9]*");
        
        //not numbers and letters - bad
        String badTestValue = "| & @";
        
        //good string including unicode characters and numbers
        String goodTestValue = "Dobrý string číslo 1";
        //empty string is also OK, because original NumberAndLetterValidator does not check mandatory constraint
        String goodTestValue2 = "";
        
        assertEquals(false, validator.validate(badTestValue));
        
        assertEquals(true, validator.validate(goodTestValue));
        assertEquals(true, validator.validate(goodTestValue2));
    }
    
    /**
     * Test of validate method, of class RegexValidator.
     * This test is trying to do exact same validation as NumberValidator
     * but using the RegexValidator with given regex.
     */
    @Test
    public void testValidateNumberWithRegex() {
        RegexValidator validator = new RegexValidator("[0-9]*");
        
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
        //empty string is also OK, because original NumberValidator does not check mandatory constraint
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
