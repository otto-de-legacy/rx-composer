package de.otto.rx.composer.client;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static de.otto.rx.composer.client.HttpServiceClient.clientFor;
import static de.otto.rx.composer.util.Collectors.toImmutableMap;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class ServiceClients implements AutoCloseable {

    private static final Logger LOG = getLogger(ServiceClients.class);

    private final String defaultRef;
    private final ImmutableMap<String, ServiceClient> serviceClients;

    /**
     * Creates a ServiceClients instance with a {@link ClientConfig#singleRetry()} as default client configuration, and
     * {@link ClientConfig#noRetries()} and {@link ClientConfig#noResiliency()} as optional configurations.
     *
     * <p>
     *     For every ClientConfig, a {@link ServiceClient} is created (and {@link ServiceClient#close() closed} again,
     *     if the ServiceClients instance is closed).
     * </p>
     *
     * <p>
     *     The created instance should be {@link #close() closed} on shutdown to release resources properly.
     * </p>
     *
     * @return ServiceClients
     */
    public static ServiceClients defaultClients() {
        return serviceClients(singleRetry(), noRetries(), noResiliency());
    }

    /**
     * Creates a ServiceClients instance configured like {@link #defaultClients()}, but with a different
     * {@code defaultClientConfig}. The defaultClientConfig can either be one of the default configs, or some
     * other ClientConfig.
     *
     * <p>
     *     For every ClientConfig, a {@link ServiceClient} is created (and {@link ServiceClient#close() closed} again,
     *     if the ServiceClients instance is closed).
     * </p>
     *
     * <p>
     *     The created instance should be {@link #close() closed} on shutdown to release resources properly.
     * </p>
     *
     * @param defaultClientConfig ClientConfig used by default
     * @return ServiceClients
     */
    public static ServiceClients defaultClientsWith(final ClientConfig defaultClientConfig) {
        final Map<Ref, ServiceClient> map = new HashMap<>();
        asList(singleRetry(), noRetries(), noResiliency()).forEach(config -> map.put(config.getRef(), clientFor(config)));
        // add defaultClientConfig if it's not already in the standard configurations:
        if (stream(DefaultRef.values())
                .map(Enum::name)
                .noneMatch(n->n.equals(defaultClientConfig.getRef().name()))) {
            map.put(defaultClientConfig.getRef(), clientFor(defaultClientConfig));
        }

        return new ServiceClients(defaultClientConfig.getRef(), map);
    }

    /**
     * Creates a ServiceClients instance with a default ClientConfig and zero or more additional ClientConfigs.
     *
     * <p>
     *     Every configured ClientConfig must have a unique {@link ClientConfig#getRef() key}, otherwise an exception
     *     is thrown.
     * </p>
     *
     * <p>
     *     For every ClientConfig, a {@link ServiceClient} is created (and {@link ServiceClient#close() closed} again,
     *     if the ServiceClients instance is closed).
     * </p>
     *
     * <p>
     *     The created instance should be {@link #close() closed} on shutdown to release resources properly.
     * </p>
     *
     * @param defaultClientConfig ClientConfig used by default
     * @param clientConfigs other ClientConfigs
     * @return ServiceClients
     */
    public static ServiceClients serviceClients(final ClientConfig defaultClientConfig,
                                                final ClientConfig... clientConfigs) {
        final ImmutableMap.Builder<Ref, ServiceClient> builder = ImmutableMap.builder();
        builder.put(defaultClientConfig.getRef(), clientFor(defaultClientConfig));
        if (clientConfigs != null) {
            stream(clientConfigs).forEach(config -> builder.put(config.getRef(), clientFor(config)));
        }
        return new ServiceClients(defaultClientConfig.getRef(), builder.build());
    }

    /**
     * Returns the ServiceClient with the {@link ClientConfig} identified by {@code ref}.
     *
     * @param ref reference of the client configuration
     * @return ServiceClient
     * @throws IllegalArgumentException if the refered ServiceClient is not configured.
     */
    public ServiceClient getBy(final Ref ref) {
        final ServiceClient serviceClient = serviceClients.get(ref.name());
        if (serviceClient == null) {
            throw new IllegalArgumentException("Unknown ref");
        } else {
            return serviceClient;
        }
    }

    /**
     * Returns the default ServiceClient
     *
     * @return ServiceClient
     */
    public ServiceClient get() {
        return serviceClients.get(defaultRef);
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

    private ServiceClients(final Ref defaultKey,
                           final Map<Ref, ServiceClient> serviceClients) {
        this.defaultRef = checkNotNull(defaultKey).name();
        this.serviceClients = checkNotNull(serviceClients).entrySet().stream().collect(toImmutableMap((e)->e.getKey().name(), Map.Entry::getValue));
    }

}
