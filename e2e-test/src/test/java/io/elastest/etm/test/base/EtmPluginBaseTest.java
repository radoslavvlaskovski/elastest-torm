package io.elastest.etm.test.base;

import static java.lang.System.getProperty;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EtmPluginBaseTest extends EtmBaseTest {

    protected String jenkinsPluginManagerAd = "/pluginManager/advanced";
    protected String jenkinsRestartRelPath = "/restart";
    protected String jenkinsCIUrl = "http://172.17.0.1:8080";
    protected String pluginPath = "/home/ubuntu/workspace/elastest-torm/e2e-test-with-plugin/elastest-plugin/target/elastest.hpi";
    protected String pluginSettings = "/configureTools/";
    protected String jenkinsUser = "elastest";
    protected String jenkinsPass = "elastest";

    @BeforeEach
    void pluginSetup() {

        // Setup Jenkins URL
        String jenkinsCIUrl = getProperty("ciUrl");
        if (jenkinsCIUrl != null) {
            this.jenkinsCIUrl = jenkinsCIUrl;
        }

        String pluginPath = getProperty("ePluginPath");
        if (pluginPath != null) {
            this.pluginPath = pluginPath;
        }

        jenkinsPluginManagerAd = this.jenkinsCIUrl + jenkinsPluginManagerAd;
        pluginSettings = this.jenkinsCIUrl + pluginSettings;
        jenkinsRestartRelPath = this.jenkinsCIUrl + jenkinsRestartRelPath;

        // Setup Jenkins credentials
        String jenkinsUser = getProperty("ciUser");
        if (jenkinsUser != null) {
            this.jenkinsUser = jenkinsUser;
        }

        String jenkinsPass = getProperty("ciPass");
        if (jenkinsPass != null) {
            this.jenkinsPass = jenkinsPass;
        }
    }

    protected void installElasTestPlugin(WebDriver webDriver)
            throws IOException {
        WebDriverWait waitService = new WebDriverWait(driver, 60);

        // Install plugin
        log.info("Installing plugin from: {}", pluginPath);
        By inputFileName = By.name("name");
        WebElement uploadFile = webDriver.findElement(inputFileName);
        ((RemoteWebElement) uploadFile).setFileDetector(new LocalFileDetector()); 
        uploadFile.sendKeys(pluginPath);
        By uploadButton = By.xpath("//button[contains(string(), 'Upload')]");
        webDriver.findElement(uploadButton).click();

        // Check the plugin installation is ok
        log.info("Checking installation status");
        boolean pluginAlreadyInstalled = false;
        By installationStatus = By
                .xpath("//table/tbody/tr/td[contains(string(), 'Success')]");
        try {
            waitService.until(visibilityOfElementLocated(installationStatus));
        } catch (TimeoutException te) {
            By alreadyInstalledMessage = By
                    .xpath("//table/tbody/tr/td[contains(string(), 'elastest plugin is already installed')]");
            waitService.until(visibilityOfElementLocated(alreadyInstalledMessage));
            pluginAlreadyInstalled = true;
        }
        
        if (pluginAlreadyInstalled) {
            navigateTo(webDriver, jenkinsRestartRelPath);
            By yesButton = By.xpath("//button[contains(string(), 'Yes')]");
            webDriver.findElement(yesButton).click();
            WebDriverWait waitForLogin = new WebDriverWait(driver, 180);
            //wait for login form
            By userField = By.id("j_username");
            waitForLogin.until(visibilityOfElementLocated(userField));
            loginOnJenkins(webDriver);
        }
        log.info("Plugin installation finished");

        log.info("Navigate to main page");
        By homeLink = By.linkText("Jenkins");
        webDriver.findElement(homeLink).click();
    }

    protected void pluginConfiguration(WebDriver driver) {
        WebDriverWait waitService = new WebDriverWait(driver, 10);

        // Fill configuration
        log.info("Filling the configuration");
        driver.findElement(By.name("_.elasTestUrl")).clear();
        driver.findElement(By.name("_.elasTestUrl")).sendKeys(tormUrl);
        if (eUser != null && ePassword != null) {
            driver.findElement(By.name("_.username")).clear();
            driver.findElement(By.name("_.username")).sendKeys(eUser);
            driver.findElement(By.name("_.password")).clear();
            driver.findElement(By.name("_.password")).sendKeys(ePassword);
        }

        // Test connection
        driver.findElement(
                By.xpath("//button[contains(string(), 'Test Connection')]"))
                .click();
        By testConnectionResult = By
                .xpath("//div[contains(string(), 'Success')]");
        waitService.until(visibilityOfElementLocated(testConnectionResult));
        log.info("Successfull conection");

        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
    }

    protected void createFreestyleJob(WebDriver driver, String jobName) {
        WebDriverWait waitService = new WebDriverWait(driver, 60);

        log.info("Creating a Freestyle Job");
        driver.findElement(By.id("name")).sendKeys(jobName);

        log.info("Select the Job's type");
        driver.findElement(
                By.xpath("//li[contains(string(), 'Freestyle project')]"))
                .click();
        driver.findElement(By.id("ok-button")).click();

        driver.findElement(By.xpath(
                "//input[@type='radio'][following-sibling::text()[position()=1][contains(string(), 'Git')]]"))
                .click();
        driver.findElement(By.xpath(
                "//*[@id=\"main-panel\"]/div/div/div/form/table/tbody/tr[133]/td[3]/div/div[1]/table/tbody/tr[1]/td[3]/input"))
                .sendKeys("https://github.com/elastest/demo-projects.git");

        WebElement myelement = driver.findElement(By.xpath(
                "//div/span/span/button[contains(string(), 'Add build step')]"));
        JavascriptExecutor jse2 = (JavascriptExecutor) driver;
        jse2.executeScript("arguments[0].scrollIntoView()", myelement);

        By byAddStepButton = By.xpath(
                "//div/span/span/button[contains(string(), 'Add build step')]");
        waitService.until(
                ExpectedConditions.elementToBeClickable(byAddStepButton));
        driver.findElement(byAddStepButton).click();

        By byItemShell = By
                .xpath("//ul/li/a[contains(string(), 'Execute shell')]");
        waitService.until(visibilityOfElementLocated(byItemShell));
        driver.findElement(byItemShell).click();

        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
        driver.findElement(By.linkText("Configure")).click();

        driver.findElement(By.xpath(
                "//*[contains(@id, 'yui-gen')]/table/tbody/tr[3]/td[3]/textarea"))
                .sendKeys("cd unit-java-test; mvn test");
        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();

    }
    
    protected boolean isJobCreated(String jobName) {
        try {
            driver.findElement(By.linkText(jobName));
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    protected void createPipelineJob(WebDriver driver, String jobName,
            String script) {
        log.info("Creating a Pipeline Job");
        driver.findElement(By.id("name")).sendKeys(jobName);

        log.info("Select the Job's type");
        driver.findElement(By.xpath("//li[contains(string(), 'Pipeline')]"))
                .click();
        driver.findElement(By.id("ok-button")).click();

        driver.findElement(By.xpath("//*[@id=\"workflow-editor-1\"]/textarea"))
                .sendKeys(script);

        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
    }

    protected void loginOnJenkins(WebDriver driver) {
        log.info("Login on Jenkins.");
        driver.findElement(By.name("j_username")).sendKeys(jenkinsUser);
        driver.findElement(By.name("j_password")).sendKeys(jenkinsPass);
        driver.findElement(By.name("Submit")).click();
    }
    
    protected void executeJob(WebDriver driver) throws InterruptedException {
        log.info("Run Job");
        long historySize = driver
                .findElements(
                        By.xpath("//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr"))
                .size();
        driver.findElement(By.xpath("//a[contains(string(), 'Build Now')]"))
                .click();
        while (driver
                .findElements(
                        By.xpath("//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr"))
                .size() == historySize) {
            log.info("history table size {}", driver
                .findElements(
                        By.xpath("//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr"))
                .size());
        }
        
        log.info("Waiting for the start of Job execution");
        By newBuildHistory = By
                .xpath("//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr[2]");
        WebDriverWait waitService = new WebDriverWait(driver, 10);
        waitService.until(visibilityOfElementLocated(newBuildHistory));
        Thread.sleep(2000);
        driver.findElement(By.xpath(
                "//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr[2]/td/div[1]/div/a"))
                .click();
    }

}
