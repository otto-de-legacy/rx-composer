package de.otto.edison.aggregator;

import de.otto.edison.aggregator.http.HttpClient;

import javax.ws.rs.core.MediaType;

public class HttpContentProvider {

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final String uri,
                                              final MediaType accept) {
        return new ContentProvider((slotId) -> httpClient
                .get(uri, accept)
                .map(response -> new Content(slotId, response))
        );
    }

}
