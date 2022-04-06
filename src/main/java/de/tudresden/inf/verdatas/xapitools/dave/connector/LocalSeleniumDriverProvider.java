package de.tudresden.inf.verdatas.xapitools.dave.connector;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Handle initialization of {@link WebDriver} for Selenium using a local Chrome Engine.
 * <p>
 * Used in dev mode.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Component
@Profile("dev")
public class LocalSeleniumDriverProvider implements SeleniumWebDriverProvider {
    public LocalSeleniumDriverProvider() {
        WebDriverManager.chromedriver().setup();
    }

    /**
     * Spawn a headless {@link WebDriver} using a locally installed Chrome.
     */
    @Override
    public WebDriver getWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        return new ChromeDriver(options);
    }
}
