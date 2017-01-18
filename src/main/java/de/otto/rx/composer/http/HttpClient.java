package de.otto.rx.composer.http;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.ClientProperties.ASYNC_THREADPOOL_SIZE;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpClient implements AutoCloseable {

    private static final int DEFAULT_HTTP_THREADPOOL_SIZE = 8;

    private static final Logger LOG = getLogger(HttpClient.class);

    private final Client client;
    private final int timeoutMillis;

    public HttpClient(final int connectTimeoutMillis, final int readTimeoutMillis) {
        this(DEFAULT_HTTP_THREADPOOL_SIZE, connectTimeoutMillis, readTimeoutMillis);
    }

    public HttpClient(final int httpThreadpoolSize, final int connectTimeoutMillis, final int readTimeoutMillis) {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ASYNC_THREADPOOL_SIZE, httpThreadpoolSize);
        clientConfig.property(CONNECT_TIMEOUT, connectTimeoutMillis);
        clientConfig.property(READ_TIMEOUT, readTimeoutMillis);
        clientConfig.property(FOLLOW_REDIRECTS, true);
        client = newClient(clientConfig);
        this.timeoutMillis = readTimeoutMillis;
    }

    public HttpClient(final Configuration configuration) {
        client = newClient(configuration);
        this.timeoutMillis = (int) configuration.getProperty(READ_TIMEOUT);
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
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
