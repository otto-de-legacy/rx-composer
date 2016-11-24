package de.otto.rx.composer.http;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpClient implements AutoCloseable {

    private static final Logger LOG = getLogger(HttpClient.class);

    private final Client client;

    public HttpClient(final int connectTimeoutMillis, final int readTimeoutMillis) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(CONNECT_TIMEOUT, connectTimeoutMillis);
        clientConfig.property(READ_TIMEOUT, readTimeoutMillis);
        client = newClient(clientConfig);
    }

    public HttpClient(final Configuration configuration) {
        client = newClient(configuration);
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
