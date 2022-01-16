package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LrsConnectorLifecycleManager implements BeanFactoryAware {

    private ConfigurableBeanFactory beanFactory;
    private final Map<LrsConnection, LrsConnector> connectorForConnection = new HashMap<>();

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    public void createConnector(LrsConnection connection) {
        LrsConnector connector = new LrsConnector(connection);
        this.beanFactory.registerSingleton("lrsConnector-" + connection.getConnectionId(), connector);
        this.connectorForConnection.put(connection, connector);
    }

    public void deleteConnector(LrsConnection connection) {
        this.beanFactory.destroyBean("lrsConnector-" + connection.getConnectionId(), this.connectorForConnection.remove(connection));
    }

    public LrsConnector getConnector(LrsConnection connection) {
        return this.connectorForConnection.get(connection);
    }
}
