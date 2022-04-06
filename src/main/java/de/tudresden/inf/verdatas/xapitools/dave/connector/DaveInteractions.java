package de.tudresden.inf.verdatas.xapitools.dave.connector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Class to provide access to functionality of DAVE by using Selenium
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
class DaveInteractions {

    /**
     * Start new Selenium Session
     *
     * @param daveEndpoint      URL to reach the DAVE framework
     * @param getDriverFunction {@link WebDriver} for Selenium
     */
    static WebDriver startNewSession(URL daveEndpoint, Supplier<WebDriver> getDriverFunction) {
        WebDriver driver = getDriverFunction.get();
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);
        driver.get(daveEndpoint.toString());
        return driver;
    }

    /**
     * Initialize DAVE Connection for an existing {@link de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection}
     *
     * @param driver needed by Selenium
     * @param name   Title of corresponding LRS Connection
     * @param url    URL of corresponding LRS Connection
     * @param key    Client key of corresponding LRS Connection
     * @param secret Client secret of corresponding LRS Connection
     * @throws InterruptedException when an Interrupt occurred
     */
    static void initializeDave(WebDriver driver, String name, String url, String key, String secret) throws InterruptedException {
        createWorkbook(driver);
        connectLRS(driver, name, url, key, secret);
        createAnalysis(driver);
    }

    /**
     * Execute the given Analysis. Uses files and uploads them to DAVE to ensure the correctness of the Analysis description
     *
     * @param driver    needed by Selenium
     * @param queryPath Path, where the Query description can be found
     * @param graphPath Path, where the Graph Description can be found
     * @return result of Analysis as diagram
     * @throws InterruptedException                                                                         when an Interrupt occurred
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.AnalysisExecutionException when there's an error during the execution
     */
    static String executeAnalysis(WebDriver driver, String queryPath, String graphPath) throws InterruptedException {
        Optional<String> queryError = addDescriptionToAnalysis(driver, queryPath, AnalysisDescription.QUERY);
        Optional<String> graphError = addDescriptionToAnalysis(driver, graphPath, AnalysisDescription.GRAPH);
        if (queryError.isPresent()) {
            throw new DaveExceptions.AnalysisExecutionException("Error during parsing of query description.");
        } else if (graphError.isPresent()) {
            throw new DaveExceptions.AnalysisExecutionException("Error during parsing of graph description.");
        }
        return getVisOfAnalysis(driver);
    }

    /**
     * Get the execution results of the given Analysis.
     * This is the information, which was filtered by the corresponding Query and is used to create the visualisation.
     *
     * @param driver    needed by Selenium
     * @param queryPath Path, where the Query description can be found
     * @param graphPath Path, where the Graph Description can be found
     * @throws InterruptedException when an Interrupt occurred
     */
    static String getAnalysisResult(WebDriver driver, String queryPath, String graphPath) throws InterruptedException {
        executeAnalysis(driver, queryPath, graphPath);
        return getResultsForAnalysis(driver);
    }

    /**
     * Check if DAVE Connection is alive
     *
     * @param driver needed by Selenium
     */
    static Boolean checkForLogo(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector("#homeicon"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Check if DAVE Connection is alive and initialized
     *
     * @param driver needed by Selenium
     */
    static Boolean checkForInitSuccess(WebDriver driver) {
        try {
            driver.findElement(By.cssSelector(".page.question"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Create a Workbook, which is needed to add and execute Analyses in DAVE
     *
     * @param driver needed by Selenium
     * @throws InterruptedException when an Interrupt occurred
     */
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

    /**
     * Initialize the DAVE Connection for validation of Analyses
     *
     * @param driver needed by Selenium
     * @throws InterruptedException when an Interrupt occurred
     */
    public static void initializeTestSession(WebDriver driver) throws InterruptedException {
        driver.findElement(By.cssSelector(".workbookinfo")).click();

        driver.manage().timeouts().implicitlyWait(2, TimeUnit.MINUTES);
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress:not(.mdc-linear-progress--closed)"));
        driver.findElement(By.cssSelector(".testdatasetblock .mdc-linear-progress--closed"));
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);
        createAnalysis(driver);
    }

    /**
     * Add an LRS Connection
     *
     * @param driver needed by Selenium
     * @param name   Title of corresponding LRS Connection
     * @param url    URL of corresponding LRS Connection
     * @param key    Client key of corresponding LRS Connection
     * @param secret Client secret of corresponding LRS Connection
     * @throws InterruptedException when an Interrupt occurred
     */
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

    /**
     * Prepare the execution of an Analysis
     *
     * @param driver needed by Selenium
     * @throws InterruptedException when an Interrupt occurred
     */
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

    /**
     * Upload an Analysis description to be executed
     *
     * @param driver      needed by Selenium
     * @param Path        Path to find the file, which stores the description
     * @param description indicates whether the Query or Graph Description is provided
     * @return error message if the descriptions aren't valid
     */
    public static Optional<String> addDescriptionToAnalysis(WebDriver driver, String Path, DaveInteractions.AnalysisDescription description) {
        if (description.equals(AnalysisDescription.QUERY)) {
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

    /**
     * Execute an Analysis and return the resulting diagram
     *
     * @param driver needed by Selenium
     * @throws InterruptedException when an Interrupt occurred
     */
    private static String getVisOfAnalysis(WebDriver driver) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(150);
        driver.findElement(By.cssSelector(".header-button")).click();

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.findElement(By.cssSelector(".mdc-snackbar.dave-snackbar.mdc-snackbar--active"));
        driver.findElement(By.cssSelector(".mdc-snackbar.dave-snackbar:not(.mdc-snackbar--active)"));
        driver.manage().timeouts().implicitlyWait(750, TimeUnit.MILLISECONDS);

        String svg = driver.findElement(By.cssSelector(".dave-vega-container svg")).getAttribute("outerHTML");
        return svg.replaceFirst("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
    }

    /**
     * Execute an Analysis and return the execution results.
     * This is the information, which was filtered by the corresponding Query and is used to create the visualisation.
     *
     * @param driver needed by Selenium
     */
    private static String getResultsForAnalysis(WebDriver driver) {
        return driver.findElement(By.cssSelector(".analysis-grid .analysis-inner .analysis-inner .cell-6 .result pre")).getText();
    }

    /**
     * UI helper enum, controls insertion of analysis description
     */
    public enum AnalysisDescription {
        QUERY,
        GRAPH
    }
}
