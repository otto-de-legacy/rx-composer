package de.otto.rx.composer.client;

import rx.Observable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface ServiceClient extends AutoCloseable {

    public ClientConfig getClientConfig();

    public Observable<Response> get(final String uri, final MediaType accept);

    @Override
    public void close() throws Exception;
}
