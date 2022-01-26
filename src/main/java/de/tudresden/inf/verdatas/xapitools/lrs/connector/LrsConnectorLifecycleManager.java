package de.tudresden.inf.verdatas.xapitools.lrs.connector;

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
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Magic piece of Java that can spawn and despawn Beans.
 * Here it is used to control the Lifecycle of {@link LrsConnector}s.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LrsConnectorLifecycleManager implements BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;
    private final ScheduledAnnotationBeanPostProcessor schedulingRegistrar;

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
     * Create an {@link LrsConnector} for the given {@link LrsConnection}.
     */
    public void createConnector(LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        // Add to Application Context
        this.beanFactory.registerSingleton(targetBeanName, new LrsConnector(connection));
        // Enable Scheduling. This is what @EnableScheduling would normally do.
        this.schedulingRegistrar.postProcessAfterInitialization(this.beanFactory.getBean("lrsConnector-" + connection.getConnectionId()), targetBeanName);
    }

    /**
     * Get the {@link LrsConnector} for the given active {@link LrsConnection}.
     */
    public LrsConnector getConnector(@Active LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        return this.beanFactory.getBean(targetBeanName, LrsConnector.class);
    }

    /**
     * Destroy the {@link LrsConnector} for the given {@link LrsConnection}.
     */
    public void deleteConnector(LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        // Disable Scheduling
        LrsConnector targetBean = this.beanFactory.getBean(targetBeanName, LrsConnector.class);
        this.schedulingRegistrar.postProcessBeforeDestruction(targetBean, targetBeanName);
        // Remove from Application Context
        this.beanFactory.destroySingleton(targetBeanName);
    }
}
