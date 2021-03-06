package de.tudresden.inf.verdatas.xapitools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * The Entrypoint to this Application.
 * You should not need to modify this class.
 */
@SpringBootApplication
@EnableCaching
public class DatatoolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatatoolsApplication.class, args);
    }

}
