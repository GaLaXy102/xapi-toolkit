package de.tudresden.inf.rn.xapi.datatools.lrs.connector;

import de.tudresden.inf.rn.xapi.datatools.lrs.LrsConnection;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class LrsConnectorLifecycleManager implements BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;
    private final ScheduledAnnotationBeanPostProcessor schedulingRegistrar;

    public LrsConnectorLifecycleManager(ScheduledAnnotationBeanPostProcessor schedulingRegistrar) {
        this.schedulingRegistrar = schedulingRegistrar;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public void createConnector(LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        // Add to Application Context
        this.beanFactory.registerSingleton(targetBeanName, new LrsConnector(connection));
        // Enable Scheduling. This is what @EnableScheduling would normally do.
        this.schedulingRegistrar.postProcessAfterInitialization(this.beanFactory.getBean("lrsConnector-" + connection.getConnectionId()), targetBeanName);
    }

    public LrsConnector getConnector(LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        return this.beanFactory.getBean(targetBeanName, LrsConnector.class);
    }

    public void deleteConnector(LrsConnection connection) {
        String targetBeanName = "lrsConnector-" + connection.getConnectionId();
        // Disable Scheduling
        LrsConnector targetBean = this.beanFactory.getBean(targetBeanName, LrsConnector.class);
        this.schedulingRegistrar.postProcessBeforeDestruction(targetBean, targetBeanName);
        // Remove from Application Context
        this.beanFactory.destroySingleton(targetBeanName);
    }
}
