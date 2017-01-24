package de.otto.rx.composer.client;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class ServiceClients implements AutoCloseable {

    private static final Logger LOG = getLogger(ServiceClients.class);

    private final ClientConfigRef defaultRef;
    private final ImmutableMap<ClientConfigRef, ServiceClient> serviceClients;

    public static ServiceClients serviceClients(final ClientConfig defaultClientConfig,
                                                final ClientConfig... clientConfigs) {
        final ImmutableMap.Builder<ClientConfigRef, ServiceClient> builder = ImmutableMap.builder();
        if (clientConfigs != null) {
            stream(clientConfigs).forEach(config -> builder.put(config.getRef(), HttpServiceClient.clientFor(config)));
        }
        builder.put(defaultClientConfig.getRef(), HttpServiceClient.clientFor(defaultClientConfig));
        return new ServiceClients(defaultClientConfig.getRef(), builder.build());
    }

    public ServiceClient getBy(final ClientConfigRef ref) {
        final ServiceClient serviceClient = serviceClients.get(ref);
        return serviceClient != null ? serviceClient : serviceClients.get(defaultRef);
    }

    public ServiceClient getDefault() {
        return serviceClients.get(defaultRef);
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

    private ServiceClients(final ClientConfigRef defaultRef,
                           final ImmutableMap<ClientConfigRef, ServiceClient> serviceClients) {
        this.defaultRef= checkNotNull(defaultRef);
        this.serviceClients = checkNotNull(serviceClients);
    }

}
