package de.otto.edison.aggregator.providers;

import de.otto.edison.aggregator.content.HttpContent;
import de.otto.edison.aggregator.http.HttpClient;

import javax.ws.rs.core.MediaType;

public final class ContentProviders {

    private ContentProviders() {}

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final String uri,
                                              final MediaType accept) {
        return (position, index, parameters) -> httpClient
                .get(uri, accept)
                .doOnNext((c) -> System.out.println("Next: " + uri))
                .doOnUnsubscribe(() -> {
                    System.out.println("Unsubscribed request to " + uri);
                })
                .map(response -> new HttpContent(position, index, response));
    }

}
