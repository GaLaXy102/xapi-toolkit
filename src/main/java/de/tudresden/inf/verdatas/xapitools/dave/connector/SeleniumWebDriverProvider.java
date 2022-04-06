package de.tudresden.inf.verdatas.xapitools.dave.connector;

import org.openqa.selenium.WebDriver;

/**
 * Interface representing a helper to initialize the {@link WebDriver} needed by Selenium
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public interface SeleniumWebDriverProvider {
    WebDriver getWebDriver();
}
