package de.otto.rx.composer.client;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.client.HttpServiceClient.clientFor;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

// TODO: Verwendung dieser Klasse ist noch nicht so klar...
public class ServiceClients implements AutoCloseable {

    private static final Logger LOG = getLogger(ServiceClients.class);

    private final String defaultKey;
    private final ImmutableMap<String, ServiceClient> serviceClients;

    /**
     * Creates a ServiceClients instance with a default ClientConfig and zero or more additional ClientConfigs.
     * <p>
     *     Every configured ClientConfig must have a unique {@link ClientConfig#getKey() key}, otherwise an exception
     *     is thrown.
     * </p>
     * <p>
     *     The created instance should be {@link #close() closed} on shutdown to release resources properly.
     * </p>
     * <p>
     *     For every ClientConfig, a {@link ServiceClient} is created (and {@link ServiceClient#close() closed} again,
     *     if the ServiceClients instance is closed).
     * </p>
     *
     * @param defaultClientConfig ClientConfig used by default
     * @param clientConfigs other ClientConfigs
     * @return ServiceClients
     */
    public static ServiceClients serviceClients(final ClientConfig defaultClientConfig,
                                                final ClientConfig... clientConfigs) {
        final ImmutableMap.Builder<String, ServiceClient> builder = ImmutableMap.builder();
        builder.put(defaultClientConfig.getKey(), clientFor(defaultClientConfig));
        if (clientConfigs != null) {
            stream(clientConfigs).forEach(config -> builder.put(config.getKey(), clientFor(config)));
        }
        return new ServiceClients(defaultClientConfig.getKey(), builder.build());
    }


    // TODO: Die default-config sollte verwendet werden, wenn der key nicht bekannt ist.
    // In diesem Fall muss jedoch der ClientConfig Key auf den übergebenen key geändert werden, damit Hystrix korrekt
    // mit den commandKeys umgeht!
    public ServiceClient getBy(final String key) {
        final ServiceClient serviceClient = serviceClients.get(key);
        if (serviceClient == null) {
            throw new IllegalArgumentException("Unknown key");
        } else {
            return serviceClient;
        }
    }

    public ServiceClient getDefault() {
        return serviceClients.get(defaultKey);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation closes all {@link ServiceClient} instances created by the serviceClients.
     *
     */
    @Override
    public void close() {
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
