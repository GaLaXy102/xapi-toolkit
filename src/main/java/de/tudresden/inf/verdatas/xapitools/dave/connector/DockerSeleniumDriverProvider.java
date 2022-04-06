package de.tudresden.inf.verdatas.xapitools.dave.connector;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Handle initialization of {@link WebDriver} using Selenium Hub to manage sessions.
 * <p>
 * Used when not in dev mode.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Component
@Profile("!dev")
public class DockerSeleniumDriverProvider implements SeleniumWebDriverProvider {
    @Value("${xapi.dave.selenium-hub-url}")
    private URL seleniumURL;

    /**
     * Create a {@link RemoteWebDriver} (Chrome) using the Selenium Hub at ${xapi.dave.selenium-hub-url}.
     * A file detector is also added for commodity.
     */
    @Override
    public WebDriver getWebDriver() {
        RemoteWebDriver driver = (RemoteWebDriver) WebDriverManager.chromedriver().remoteAddress(this.seleniumURL).create();
        driver.setFileDetector(new LocalFileDetector());
        return driver;
    }
}
