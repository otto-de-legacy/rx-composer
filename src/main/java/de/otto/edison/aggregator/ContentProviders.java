package de.otto.edison.aggregator;

import de.otto.edison.aggregator.http.HttpClient;

import javax.ws.rs.core.MediaType;

public final class ContentProviders {

    private ContentProviders() {}

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final String uri,
                                              final MediaType accept) {
        return (position, parameters) -> httpClient
                .get(uri, accept)
                .map(response -> new Content(position, response));
    }

}
