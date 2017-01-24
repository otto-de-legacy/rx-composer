package de.otto.rx.composer.client;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class ServiceClients implements AutoCloseable {

    private static final Logger LOG = getLogger(ServiceClients.class);

    private final String defaultKey;
    private final ImmutableMap<String, ServiceClient> serviceClients;

    public static ServiceClients serviceClients(final ClientConfig defaultClientConfig,
                                                final ClientConfig... clientConfigs) {
        final ImmutableMap.Builder<String, ServiceClient> builder = ImmutableMap.builder();
        if (clientConfigs != null) {
            stream(clientConfigs).forEach(config -> builder.put(config.getKey(), HttpServiceClient.clientFor(config)));
        }
        builder.put(defaultClientConfig.getKey(), HttpServiceClient.clientFor(defaultClientConfig));
        return new ServiceClients(defaultClientConfig.getKey(), builder.build());
    }

    public ServiceClient getBy(final String ref) {
        final ServiceClient serviceClient = serviceClients.get(ref);
        return serviceClient != null ? serviceClient : serviceClients.get(defaultKey);
    }

    public ServiceClient getDefault() {
        return serviceClients.get(defaultKey);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation closes all {@link ServiceClient} instances created by the serviceClients.
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        serviceClients.values().forEach((serviceClient) -> {
            try {
                serviceClient.close();
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private ServiceClients(final String defaultKey,
                           final ImmutableMap<String, ServiceClient> serviceClients) {
        this.defaultKey = checkNotNull(defaultKey);
        this.serviceClients = checkNotNull(serviceClients);
    }

}
