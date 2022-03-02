package de.tudresden.inf.verdatas.xapitools.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public abstract class TestContainerTest {

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("xapi.dave.selenium-hub-url", TestContainerTest::getSeleniumHubUrl);
        registry.add("spring.data.mongodb.uri", mongoDb::getReplicaSetUrl);
    }

    private static final Map<String, String> seleniumEnv = new HashMap<>() {{
        put("SE_EVENT_BUS_HOST", "selenium-hub");
        put("SE_EVENT_BUS_PUBLISH_PORT", "4442");
        put("SE_EVENT_BUS_SUBSCRIBE_PORT", "4443");
    }};

    private static final Network seleniumNet = Network.newNetwork();

    public static final GenericContainer seleniumHub = new GenericContainer(DockerImageName.parse("selenium/hub:4.1.2-20220217"))
            .withNetwork(seleniumNet)
            .withExposedPorts(4442, 4443, 4444)
            .withNetworkAliases("selenium-hub");

    public static final GenericContainer seleniumNode = new GenericContainer(DockerImageName.parse("selenium/node-docker:4.1.2-20220217"))
            .withNetwork(seleniumNet)
            .withEnv(seleniumEnv)
            .withFileSystemBind("/var/run/docker.sock", "/var/run/docker.sock", BindMode.READ_WRITE)
            .dependsOn(seleniumHub);

    public static final MongoDBContainer mongoDb = new MongoDBContainer(DockerImageName.parse("mongo:4.4.12"));

    static {
        seleniumHub.start();
        seleniumNode.start();
        mongoDb.start();
    }

    public static String getSeleniumHubUrl() {
        return String.format("http://%s:%d/wd/hub", seleniumHub.getContainerIpAddress(), seleniumHub.getMappedPort(4444));
    }

}
