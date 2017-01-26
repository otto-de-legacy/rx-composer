package de.otto.rx.composer.client;

import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.ClientProperties.ASYNC_THREADPOOL_SIZE;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpServiceClient implements ServiceClient {

    private static final int DEFAULT_HTTP_THREADPOOL_SIZE = 8;

    private static final Logger LOG = getLogger(HttpServiceClient.class);

    private final Client client;
    private final ClientConfig clientConfig;

    private HttpServiceClient(final ClientConfig config) {
        final org.glassfish.jersey.client.ClientConfig jerseyConfig = new org.glassfish.jersey.client.ClientConfig();
        jerseyConfig.property(ASYNC_THREADPOOL_SIZE, DEFAULT_HTTP_THREADPOOL_SIZE);
        jerseyConfig.property(CONNECT_TIMEOUT, config.getConnectTimeout());
        jerseyConfig.property(READ_TIMEOUT, config.getReadTimeout());
        jerseyConfig.property(FOLLOW_REDIRECTS, true);
        client = newClient(jerseyConfig);
        this.clientConfig = config;
        LOG.info("Client created with {}", clientConfig);
    }

    /**
     * Returns a ServiceClient to access HTTP microservices protected by a circuit breaker, with a single retry on error.
     * <p>
     *     The timeouts are configured as:
     * </p>
     * <ul>
     *      <li>{@code connectTimeout:} 1000ms</li>
     *      <li>{@code readTimeout:} 500ms</li>
     * </ul>
     *
     * @return HTTP ServiceClient
     */
    public static ServiceClient singleRetryClient() {
        return new HttpServiceClient(singleRetry());
    }

    public static ServiceClient singleRetryClient(final Ref key, final int connectTimeout, final int readTimeout) {
        return new HttpServiceClient(singleRetry(key, connectTimeout, readTimeout));
    }

    /**
     * Returns a ServiceClient to access HTTP microservices protected by a circuit breaker, but not doing retries on error.
     * <p>
     *     The timeouts are configured as:
     * </p>
     * <ul>
     *      <li>{@code connectTimeout:} 1000ms</li>
     *      <li>{@code readTimeout:} 500ms</li>
     * </ul>
     *
     * @return HTTP ServiceClient
     */
    public static ServiceClient noRetriesClient() {
        return new HttpServiceClient(noRetries());
    }

    public static ServiceClient noRetriesClient(final Ref key, final int connectTimeout, final int readTimeout) {
        return new HttpServiceClient(noRetries(key, connectTimeout, readTimeout));
    }

    /**
     * Returns a ServiceClient to access HTTP microservices that is not protected by a circuit breaker and not doing retries on error.
     * <p>
     *     The timeouts are configured as:
     * </p>
     * <ul>
     *      <li>{@code connectTimeout:} 1000ms</li>
     *      <li>{@code readTimeout:} 500ms</li>
     * </ul>
     *
     * @return HTTP ServiceClient
     */
    public static ServiceClient noResiliencyClient() {
        return new HttpServiceClient(noResiliency());
    }

    public static ServiceClient noResiliencyClient(final Ref key, final int connectTimeout, final int readTimeout) {
        return new HttpServiceClient(noResiliency(key, connectTimeout, readTimeout));
    }

    public static HttpServiceClient clientFor(final ClientConfig config) {
        return new HttpServiceClient(config);
    }

    @Override
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public Observable<Response> get(final String uri,
                                    final MediaType accept) {
        return RxObservable.from(client)
                .target(uri)
                .request()
                .accept(accept)
                .rx()
                .get();
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing HTTP client '{}'", clientConfig.getRef());
        client.close();
    }
}
