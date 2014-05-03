/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.validateregex;

import cz.muni.fi.pv168.kartoteka.SchemaManagerBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SchemaManagerBeanTest {

    public SchemaManagerBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidateRegex() {
        try {
            String badTestValue = "[]";
            //missing ]
            String badTestValue2 = "[0-9";
            //missing )
            String badTestValue3 = "([0-9 A-Z]+";

            String goodTestValue = "[\\p{L} 0-9]*";
            String goodTestValue2 = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";

            SchemaManagerBean schemaManager = new SchemaManagerBean();
            Method validateRegexMethod = SchemaManagerBean.class.getDeclaredMethod("validateRegex", String.class);
            validateRegexMethod.setAccessible(true);

            assertEquals(false, validateRegexMethod.invoke(schemaManager, badTestValue));
            assertEquals(false, validateRegexMethod.invoke(schemaManager, badTestValue2));
            assertEquals(false, validateRegexMethod.invoke(schemaManager, badTestValue3));

            assertEquals(true, validateRegexMethod.invoke(schemaManager, goodTestValue));
            assertEquals(true, validateRegexMethod.invoke(schemaManager, goodTestValue2));

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(SchemaManagerBeanTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
