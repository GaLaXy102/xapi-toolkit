package de.tudresden.inf.verdatas.xapitools.dave.connector;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("seleniumDriverProvider")
@Profile("dev")
public class LocalSeleniumDriverProvider {
    public LocalSeleniumDriverProvider() {
        WebDriverManager.chromedriver().setup();
    }
}
