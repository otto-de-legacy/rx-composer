package de.otto.rx.composer.client;

import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        final org.glassfish.jersey.client.ClientConfig clientConfig = new org.glassfish.jersey.client.ClientConfig();
        clientConfig.property(ASYNC_THREADPOOL_SIZE, DEFAULT_HTTP_THREADPOOL_SIZE);
        clientConfig.property(CONNECT_TIMEOUT, config.getConnectTimeout());
        clientConfig.property(READ_TIMEOUT, config.getReadTimeout());
        clientConfig.property(FOLLOW_REDIRECTS, true);
        client = newClient(clientConfig);
        this.clientConfig = config;
    }

    public static HttpServiceClient singleRetryClient() {
        return new HttpServiceClient(singleRetry());
    }

    public static HttpServiceClient noRetriesClient() {
        return new HttpServiceClient(noRetries());
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
        client.close();
    }
}
