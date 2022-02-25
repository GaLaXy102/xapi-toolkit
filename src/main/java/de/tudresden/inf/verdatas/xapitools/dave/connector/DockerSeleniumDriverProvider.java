package de.tudresden.inf.verdatas.xapitools.dave.connector;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@Profile("!dev")
public class DockerSeleniumDriverProvider implements SeleniumWebDriverProvider {
    @Value("xapi.dave.selenium-hub-url")
    private URL seleniumURL;

    @Override
    public WebDriver getWebDriver() {
        return WebDriverManager.chromedriver().remoteAddress(this.seleniumURL).create();
    }
}
