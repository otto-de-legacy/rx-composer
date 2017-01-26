package de.otto.rx.composer.client;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static de.otto.rx.composer.client.DefaultRef.noResiliency;
import static de.otto.rx.composer.client.DefaultRef.noRetries;
import static de.otto.rx.composer.client.DefaultRef.singleRetry;
import static de.otto.rx.composer.client.HttpServiceClient.clientFor;
import static java.util.Arrays.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class ServiceClients implements AutoCloseable {

    private static final Logger LOG = getLogger(ServiceClients.class);

    private static ImmutableMap<String,ClientConfig> defaultConfigs = ImmutableMap.of(
            singleRetry.name(), singleRetry(),
            noRetries.name(), noRetries(),
            noResiliency.name(), noResiliency()
    );
    private final Ref defaultRef;
    private final ImmutableMap<Ref, ServiceClient> serviceClients;

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
        final ImmutableMap.Builder<Ref, ServiceClient> builder = ImmutableMap.builder();
        defaultConfigs.values().forEach(config -> builder.put(config.getRef(), clientFor(config)));

        if (!defaultConfigs.containsKey(defaultClientConfig.getRef().name())) {
            builder.put(defaultClientConfig.getRef(), clientFor(defaultClientConfig));
        }

        return new ServiceClients(defaultClientConfig.getRef(), builder.build());
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

    // TODO: Die default-config sollte verwendet werden, wenn der ref nicht bekannt ist.
    // In diesem Fall muss jedoch der ClientConfig Key auf den übergebenen ref geändert werden, damit Hystrix korrekt
    // mit den commandKeys umgeht!
    public ServiceClient getBy(final Ref ref) {
        final ServiceClient serviceClient = serviceClients.get(ref);
        if (serviceClient == null) {
            throw new IllegalArgumentException("Unknown ref");
        } else {
            return serviceClient;
        }
    }

    public ServiceClient getDefault() {
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
                           final ImmutableMap<Ref, ServiceClient> serviceClients) {
        this.defaultRef = checkNotNull(defaultKey);
        this.serviceClients = checkNotNull(serviceClients);
    }

}
