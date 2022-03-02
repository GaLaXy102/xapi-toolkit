package de.tudresden.inf.verdatas.xapitools.dave.connector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class DaveInteractions {

    /**
     * UI helper enum, controls insertion of analysis description
     */
    public enum AnalysisDescription {
        QUERY,
        GRAPH
    }

    static WebDriver startNewSession(URL daveEndpoint, Supplier<WebDriver> getDriverFunction) {
        WebDriver driver = getDriverFunction.get();
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);
        driver.get(daveEndpoint.toString());
        return driver;
    }

    static void initializeDave(WebDriver driver, String name, String url, String key, String secret) throws InterruptedException {
        createWorkbook(driver);
        connectLRS(driver, name, url, key, secret);
        createAnalysis(driver);
    }

    static String executeAnalysis(WebDriver driver, String queryPath, String graphPath) throws InterruptedException {
        Optional<String> queryError = addDescriptionToAnalysis(driver, queryPath, AnalysisDescription.QUERY);
        Optional<String> graphError = addDescriptionToAnalysis(driver, graphPath, AnalysisDescription.GRAPH);
        if (queryError.isPresent()) {
            throw new IllegalStateException("Error during parsing of query description.");
        } else if (graphError.isPresent()) {
            throw new IllegalStateException("Error during parsing of graph description.");
        }
        return getVisOfAnalysis(driver);
    }

    static String getAnalysisResult(WebDriver driver, String queryPath, String graphPath) throws InterruptedException {
        executeAnalysis(driver, queryPath, graphPath);
        return getResultsForAnalysis(driver);
    }

    private static void createWorkbook(WebDriver driver) throws InterruptedException {
        driver.findElement(By.cssSelector(".majorbutton")).click();

        TimeUnit.MILLISECONDS.sleep(150);
        List<WebElement> workbookForm = driver.findElements(By.cssSelector(".mdc-text-field__input"));
        TimeUnit.MILLISECONDS.sleep(75);
        workbookForm.get(0).sendKeys("Workbook");
        TimeUnit.MILLISECONDS.sleep(75);
        workbookForm.get(1).sendKeys("Description");
        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".wizardfooter :nth-child(2)")).click();
    }

    public static void initializeTestSession(WebDriver driver) throws InterruptedException {
        driver.findElement(By.cssSelector(".workbookinfo")).click();

        driver.manage().timeouts().implicitlyWait(2, TimeUnit.MINUTES);
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress:not(.mdc-linear-progress--closed)"));
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress--closed"));
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);
        createAnalysis(driver);
    }

    private static void connectLRS(WebDriver driver, String name, String url, String key, String secret) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".testdatasetblock .minorbutton")).click();
        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".wizardfooter :nth-child(2)")).click();

        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".picker.active ul :nth-child(3)")).click();

        TimeUnit.MILLISECONDS.sleep(150);
        List<WebElement> lrsForm = driver.findElements(By.cssSelector(".mdc-text-field__input"));
        TimeUnit.MILLISECONDS.sleep(75);
        lrsForm.get(0).sendKeys(name);
        TimeUnit.MILLISECONDS.sleep(75);
        lrsForm.get(1).sendKeys(url);
        TimeUnit.MILLISECONDS.sleep(75);
        lrsForm.get(2).sendKeys(key);
        TimeUnit.MILLISECONDS.sleep(75);
        lrsForm.get(3).sendKeys(secret);
        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".wizardfooter :nth-child(2)")).click();
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress:not(.mdc-linear-progress--closed)"));
        // Wait until loading of statements from LRS has stopped
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MINUTES);
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress--closed"));
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);
    }

    private static void createAnalysis(WebDriver driver) throws InterruptedException {
        driver.findElement(By.cssSelector(".majorbutton.newblock")).click();

        TimeUnit.MILLISECONDS.sleep(150);
        driver.findElement(By.cssSelector(".mdc-text-field__input")).sendKeys("Analysis");
        TimeUnit.MILLISECONDS.sleep(75);
        driver.findElement(By.cssSelector(".wizardfooter :nth-child(2)")).click();

        TimeUnit.MILLISECONDS.sleep(75);
        List<WebElement> sub_buttons = driver.findElements(By.cssSelector(".minimalbutton"));
        sub_buttons.get(4).click();
    }

    public static Optional<String> addDescriptionToAnalysis(WebDriver driver, String Path, DaveInteractions.AnalysisDescription decider) {
        if (decider.equals(AnalysisDescription.QUERY)) {
            WebElement query_upload = driver.findElement(By.cssSelector("#query-input-file"));
            query_upload.sendKeys(Path);
            // empty String if query is valid (no error occurred)
            String errorMessage = driver.findElements(By.cssSelector(".error")).get(0).getText();
            if (!errorMessage.isEmpty()) {
                return Optional.of(errorMessage);
            }
        } else {
            WebElement description_upload = driver.findElement(By.cssSelector("#vega-input-file"));
            description_upload.sendKeys(Path);
            // empty String if description is valid (no error occurred)
            String errorMessage = driver.findElements(By.cssSelector(".error")).get(1).getText();
            if (!errorMessage.isEmpty()) {
                return Optional.of(errorMessage);
            }
        }
        return Optional.empty();
    }

    private static String getVisOfAnalysis(WebDriver driver) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(150);
        driver.findElement(By.cssSelector(".header-button")).click();

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.findElement(By.cssSelector(".mdc-snackbar.dave-snackbar.mdc-snackbar--active"));
        driver.findElement(By.cssSelector(".mdc-snackbar.dave-snackbar:not(.mdc-snackbar--active)"));
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);

        return driver.findElement(By.cssSelector(".dave-vega-container svg")).getAttribute("outerHTML");
    }

    private static String getResultsForAnalysis(WebDriver driver) {
        return driver.findElement(By.cssSelector(".analysis-grid .analysis-inner .analysis-inner .cell-6 .result pre")).getText();
    }
}
