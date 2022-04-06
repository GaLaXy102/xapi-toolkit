package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

/**
 * Magic piece of Java that can spawn and despawn Beans.
 * Here it is used to control the Lifecycle of {@link DaveConnector}s.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Component
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveConnectorLifecycleManager implements BeanFactoryAware {
    private final ScheduledAnnotationBeanPostProcessor schedulingRegistrar;
    private final TaskExecutor taskExecutor;
    private final SeleniumWebDriverProvider webDriverProvider;
    private DefaultListableBeanFactory beanFactory;
    @Value("${xapi.dave.backend-base-url}")
    private URL daveEndpoint;

    /**
     * Callback that supplies the owning factory to a bean instance.
     * <p>Invoked after the population of normal bean properties
     * but before an initialization callback such as
     * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
     *
     * @param beanFactory owning BeanFactory (never {@code null}).
     *                    The bean can immediately call methods on the factory.
     * @throws BeansException in case of initialization errors
     * @see BeanInitializationException
     */
    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    /**
     * Create an {@link DaveConnector} for the given {@link LrsConnection}.
     */
    public void createConnector(@Active LrsConnection connection) {
        String targetBeanName = "DaveConnector-" + connection.getConnectionId();
        // Add to Application Context
        DaveConnector connector = new DaveConnector(daveEndpoint, connection, this.webDriverProvider::getWebDriver,
                taskExecutor);
        this.beanFactory.registerSingleton(targetBeanName, connector);
        this.beanFactory.registerDisposableBean(targetBeanName, connector);
        // Enable Scheduling. This is what @EnableScheduling would normally do.
        this.schedulingRegistrar.postProcessAfterInitialization(this.beanFactory.getBean("DaveConnector-" + connection.getConnectionId()), targetBeanName);
        this.taskExecutor.execute(connector::initialize);
    }

    /**
     * Create an additional {@link DaveConnector}, which is used to validate Analyses descriptions
     */
    public void createTestConnector() {
        String targetBeanName = "DaveConnector-Test";
        DaveConnector connector = new DaveConnector(daveEndpoint, this.webDriverProvider::getWebDriver, taskExecutor);
        this.beanFactory.registerSingleton(targetBeanName, connector);
        this.beanFactory.registerDisposableBean(targetBeanName, connector);
        this.schedulingRegistrar.postProcessAfterInitialization(this.beanFactory.getBean("DaveConnector-Test"), targetBeanName);
        this.taskExecutor.execute(connector::startTestSession);
    }

    /**
     * Get the {@link DaveConnector} for the given active {@link LrsConnection}.
     */
    public DaveConnector getConnector(@Active LrsConnection connection) {
        String targetBeanName = "DaveConnector-" + connection.getConnectionId();
        return this.beanFactory.getBean(targetBeanName, DaveConnector.class);
    }

    /**
     * Get the additional Test-{@link DaveConnector}
     */
    public DaveConnector getTestConnector() {
        String targetBeanName = "DaveConnector-Test";
        return this.beanFactory.getBean(targetBeanName, DaveConnector.class);
    }

    /**
     * Destroy the {@link DaveConnector} for the given {@link LrsConnection}.
     */
    public void deleteConnector(LrsConnection connection) {
        String targetBeanName = "DaveConnector-" + connection.getConnectionId();
        // Disable Scheduling
        DaveConnector targetBean = this.beanFactory.getBean(targetBeanName, DaveConnector.class);
        this.schedulingRegistrar.postProcessBeforeDestruction(targetBean, targetBeanName);
        // Remove from Application Context
        this.beanFactory.destroySingleton(targetBeanName);
    }

    /**
     * Destroy the additional Test-{@link DaveConnector}
     */
    public void deleteTestConnector() {
        String targetBeanName = "DaveConnector-Test";
        // Disable Scheduling
        DaveConnector targetBean = this.beanFactory.getBean(targetBeanName, DaveConnector.class);
        this.schedulingRegistrar.postProcessBeforeDestruction(targetBean, targetBeanName);
        // Remove from Application Context
        this.beanFactory.destroySingleton(targetBeanName);
    }
}
