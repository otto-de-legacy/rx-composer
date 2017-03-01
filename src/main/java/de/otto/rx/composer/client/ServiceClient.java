package de.otto.rx.composer.client;

import rx.Observable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A client used to fetch content from some remote service.
 * <p>
 *     ServiceClients should be closed on shutdown in order to release system resources.
 * </p>
 */
public interface ServiceClient extends AutoCloseable {

    /**
     * The {@link ClientConfig} associated with the service client.
     *
     * @return ClientConfig
     */
    ClientConfig getClientConfig();

    /**
     * Fetches content from the specified uri.
     *
     * @param uri the URI of the content
     * @param accept the accepted media type
     * @return oberservable response. Must not be null.
     */
    Observable<Response> get(final String uri, final MediaType accept);

    /**
     * {@inheritDoc}
     */
    @Override
    void close();
}
