package cz.muni.fi.pv168.seleniumtests;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IntegrationTest {

    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        driver = new ChromeDriver();
        baseUrl = "http://localhost:8080/";
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    }

    
    /**
     * Test for simple Google login
     * @throws Exception 
     */
    @Test
    public void testGoogleLogin() throws Exception {
        driver.get(baseUrl + "/Kartoteka");
        driver.findElement(By.id("j_idt18:google")).click();
        driver.findElement(By.id("Email")).clear();
        driver.findElement(By.id("Email")).sendKeys("kartoteka.test");
        driver.findElement(By.id("Passwd")).clear();
        driver.findElement(By.id("Passwd")).sendKeys("Kartoteka");
        driver.findElement(By.id("signIn")).click();
        WebElement userNameElement = driver.findElement(By.xpath("//span[text()='Adam Tester']"));
        WebElement emailElement = driver.findElement(By.xpath("//span[text()='(kartoteka.test@gmail.com)']"));
        String username = "Adam Tester";
        String email = "(kartoteka.test@gmail.com)";
        assertEquals(username, userNameElement.getText());
        assertEquals(email, emailElement.getText());

    }

    
    /**
     * Test for simple Facebook login
     * @throws Exception 
     */
    @Test
    public void testFacebookLogin() throws Exception {
        driver.get(baseUrl + "/Kartoteka/welcome.xhtml");
        driver.findElement(By.id("j_idt18:facebook")).click();
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("kartoteka.test@gmail.com");
        driver.findElement(By.id("pass")).clear();
        driver.findElement(By.id("pass")).sendKeys("Kartoteka");
        driver.findElement(By.id("u_0_1")).click();

        WebElement userNameElement = driver.findElement(By.xpath("//span[text()='Adam Tester']"));
        WebElement emailElement = driver.findElement(By.xpath("//span[text()='(kartoteka.test@gmail.com)']"));
        String username = "Adam Tester";
        String email = "(kartoteka.test@gmail.com)";
        assertEquals(username, userNameElement.getText());
        assertEquals(email, emailElement.getText());

    }
    
    /**
     * Test creates filing cabinet, checks if it exists. Then adds one shcemaField with constraints,
     * then it checks for these constraints and finally it inserts one card with desired value
     * and checks if the card with this value is present.
     * Finally, it deletes whole filing cabinet.
     * @throws Exception 
     */
    @Test
    public void testCreateFilingCabinetAndCard() throws Exception {
        driver.get(baseUrl + "/Kartoteka/welcome.xhtml");
        driver.findElement(By.id("j_idt18:google")).click();
        driver.findElement(By.id("Email")).clear();
        driver.findElement(By.id("Email")).sendKeys("kartoteka.test");
        driver.findElement(By.id("Passwd")).clear();
        driver.findElement(By.id("Passwd")).sendKeys("Kartoteka");
        driver.findElement(By.id("signIn")).click();
        //now logged in

        //adding filing cabinet named test
        driver.findElement(By.id("j_idt56:j_idt57")).click();
        WebDriverWait wait = new WebDriverWait(driver, 7);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt58:schemaNameInput")));
        driver.findElement(By.id("j_idt58:schemaNameInput")).clear();
        driver.findElement(By.id("j_idt58:schemaNameInput")).sendKeys("Test");
        driver.findElement(By.id("j_idt58:j_idt62")).click();

        //now we will test if cabinet Test is present
        WebElement filingCabinetTitle = driver.findElement(By.xpath("//td/div[contains(text(),'Test')]"));
        assertEquals("Test", filingCabinetTitle.getText());

        //now we will add schemaField
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt38")).click();
        driver.findElement(By.id("tableForm:j_idt57:j_idt58")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt69:fieldTitle")));
        driver.findElement(By.id("j_idt69:fieldTitle")).clear();
        driver.findElement(By.id("j_idt69:fieldTitle")).sendKeys("TestField");
        driver.findElement(By.xpath("//div[@id='j_idt69:mandatory']/div[2]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-triangle-1-s")).click();
        driver.findElement(By.xpath("//div[@id='j_idt69:constraint_panel']/div/ul/li[3]")).click();

        //we have to "wait" - this is one of official workarounds to wait for element to be present
        try {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));
            addFieldButton.click();
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));

            addFieldButton.click();
        }
        //check title
        WebElement schemaFieldTitle = driver.findElement(By.xpath("//td[text()='TestField']"));
        assertEquals("TestField", schemaFieldTitle.getText());
        //check mandatory
        WebElement mandatoryTrueImage = driver.findElement(By.xpath("//img[@id='tableForm:table:0:j_idt39']"));
        assertEquals("http://localhost:8080/Kartoteka/javax.faces.resource/checkBoxTrue.png.xhtml?ln=images", mandatoryTrueImage.getAttribute("src"));
        //check constraint
        WebElement schemaFieldConstraint = driver.findElement(By.xpath("//td[text()='Letters']"));
        assertEquals("Letters", schemaFieldConstraint.getText());
        //check repeatable
        WebElement constraintFalseImage = driver.findElement(By.xpath("//*[@id=\"tableForm:table:0:j_idt47\"]"));
        assertEquals("http://localhost:8080/Kartoteka/javax.faces.resource/checkBoxFalse.png.xhtml?ln=images", constraintFalseImage.getAttribute("src"));

        //now we will go to view data section and add a card
        driver.findElement(By.cssSelector("span.ui-menuitem-text")).click();
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt53")).click();
        driver.findElement(By.id("j_idt29:j_idt30")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")));
        driver.findElement(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")).clear();
        driver.findElement(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")).sendKeys("Some text");
        driver.findElement(By.id("addForm:j_idt83")).click();

        //check added card - that it has desired field and desired value
        WebElement schemaFieldCardTitle = driver.findElement(By.xpath("//span[text()='TestField:']"));
        assertEquals("TestField:", schemaFieldCardTitle.getText());

        WebElement cardValue = driver.findElement(By.xpath("//li[contains(text(),'Some text')]"));
        assertEquals("Some text", cardValue.getText());

        //code for delete whole filing cabinet
        driver.findElement(By.cssSelector("span.ui-menuitem-text")).click();
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt43")).click();
        //confirm yes to delete whole filing cabinet
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt46")).click();
    }
    
    
    /**
     * Test creates filing cabinet, then it tries to create another one with the same name.
     * This is forbidden so message should be shown. Then it adds one schema field. Afterwards,
     * it tries to create one more shema field with the same name and with no name.
     * In both cases - it checks that appropriate messages are shown.
     * Finally. it tries to create one card with empty value (again, message is shown because
     * of mandatory constraint) and then rewriting the input to number 123 - again, it is forbidden
     * because of letters constraint. After it checks both messages, it deletes created cabinet.
     * @throws Exception 
     */
    @Test
    public void testCheckMessages() throws Exception {
        driver.get(baseUrl + "/Kartoteka/welcome.xhtml");
        driver.findElement(By.id("j_idt18:google")).click();
        driver.findElement(By.id("Email")).clear();
        driver.findElement(By.id("Email")).sendKeys("kartoteka.test");
        driver.findElement(By.id("Passwd")).clear();
        driver.findElement(By.id("Passwd")).sendKeys("Kartoteka");
        driver.findElement(By.id("signIn")).click();
        //now logged in

        //adding filing cabinet named test
        driver.findElement(By.id("j_idt56:j_idt57")).click();
        WebDriverWait wait = new WebDriverWait(driver, 7);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt58:schemaNameInput")));
        driver.findElement(By.id("j_idt58:schemaNameInput")).clear();
        driver.findElement(By.id("j_idt58:schemaNameInput")).sendKeys("Test");
        driver.findElement(By.id("j_idt58:j_idt62")).click();

        //now we will test if cabinet Test is present
        WebElement filingCabinetTitle = driver.findElement(By.xpath("//td/div[contains(text(),'Test')]"));
        assertEquals("Test", filingCabinetTitle.getText());

        //now we will try to add filing cabinet with same name as the previous one
        driver.findElement(By.id("j_idt56:j_idt57")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt58:schemaNameInput")));
        driver.findElement(By.id("j_idt58:schemaNameInput")).clear();
        driver.findElement(By.id("j_idt58:schemaNameInput")).sendKeys("Test");
        driver.findElement(By.id("j_idt58:j_idt62")).click();
        
        //now the message should be shown
        WebElement mustBeUniqueMessage = driver.findElement(By.xpath("//span[text()='Filing cabinet must be unique!']"));
        assertEquals("Filing cabinet must be unique!", mustBeUniqueMessage.getText());
        
        //hide the dialog - we dont want to add another cabinet
        driver.findElement(By.xpath("//*[@id='dialogForSchema']/div[1]/a")).click();
        
        
        //now we will add schemaField
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt38")).click();
        driver.findElement(By.id("tableForm:j_idt57:j_idt58")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt69:fieldTitle")));
        driver.findElement(By.id("j_idt69:fieldTitle")).clear();
        driver.findElement(By.id("j_idt69:fieldTitle")).sendKeys("TestField");
        driver.findElement(By.xpath("//div[@id='j_idt69:mandatory']/div[2]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-triangle-1-s")).click();
        driver.findElement(By.xpath("//div[@id='j_idt69:constraint_panel']/div/ul/li[3]")).click();

        //we have to "wait" - this is one of official workarounds to wait for element to be present
        try {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));
            addFieldButton.click();
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));

            addFieldButton.click();
        }
        
        //now we will try to add same schema field
        driver.findElement(By.id("tableForm:j_idt57:j_idt58")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("j_idt69:fieldTitle")));
        driver.findElement(By.id("j_idt69:fieldTitle")).clear();
        driver.findElement(By.id("j_idt69:fieldTitle")).sendKeys("TestField");
        driver.findElement(By.xpath("//div[@id='j_idt69:mandatory']/div[2]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-triangle-1-s")).click();
        driver.findElement(By.xpath("//div[@id='j_idt69:constraint_panel']/div/ul/li[3]")).click();
        
        //we have to "wait" - this is one of official workarounds to wait for element to be present
        try {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));
            addFieldButton.click();
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));

            addFieldButton.click();
        }
        //now the message should be shown
        mustBeUniqueMessage = driver.findElement(By.xpath("//span[text()='Schema field must be unique!']"));
        assertEquals("Schema field must be unique!", mustBeUniqueMessage.getText());
        
        //now we will try to save schema field with empty name - message should be shown
        driver.findElement(By.id("j_idt69:fieldTitle")).clear();
        //we have to "wait" - this is one of official workarounds to wait for element to be present
        try {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));
            addFieldButton.click();
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            WebElement addFieldButton = driver.findElement(By.id("j_idt69:j_idt96"));

            addFieldButton.click();
        }
        //now the message should be shown
        mustBeUniqueMessage = driver.findElement(By.xpath("//span[text()='Schema field must be named!']"));
        assertEquals("Schema field must be named!", mustBeUniqueMessage.getText());
        //hide dialog
        driver.findElement(By.xpath("//*[@id='dialogForSchemaFieldAdd']/div[1]/a")).click();

        //now we will go to view data section and add a card
        driver.findElement(By.cssSelector("span.ui-menuitem-text")).click();
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt53")).click();
        driver.findElement(By.id("j_idt29:j_idt30")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")));
        driver.findElement(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")).clear();
        driver.findElement(By.id("addForm:j_idt83")).click();
        
        //we haven't entered any value, but there is a mandatory constraint so message should be shown
        WebElement isRequiredMessage = driver.findElement(By.xpath("//span[text()='Field TestField is required!']"));
        assertEquals("Field TestField is required!", isRequiredMessage.getText());
        
        //now we try to put number as the value - but we have chosen letters constraint, so it should show the message
        driver.findElement(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")).clear();
        driver.findElement(By.id("addForm:dataTable:0:j_idt69:0:j_idt72")).sendKeys("123");
        driver.findElement(By.id("addForm:j_idt83")).click();
        
        WebElement constraintMessage = driver.findElement(By.xpath("//span[text()='Field TestField has to consist only of letters!']"));
        assertEquals("Field TestField has to consist only of letters!", constraintMessage.getText());
        
        //now we will close the dialog and end test - delete filing cabinet that we've just created
        driver.findElement(By.xpath("//*[@id='j_idt63']/div[1]/a")).click();
        
        //code for delete whole filing cabinet
        driver.findElement(By.cssSelector("span.ui-menuitem-text")).click();
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt43")).click();
        //confirm yes to delete whole filing cabinet
        driver.findElement(By.id("j_idt28:j_idt29:0:j_idt46")).click();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }
}
