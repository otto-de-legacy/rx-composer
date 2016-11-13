package de.otto.edison.aggregator.providers;

import de.otto.edison.aggregator.content.HttpContent;
import de.otto.edison.aggregator.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final String uri,
                                              final MediaType accept) {
        return (position, index, parameters) -> httpClient
                .get(uri, accept)
                .doOnNext((c) -> LOG.info("Next: " + uri))
                .doOnUnsubscribe(() -> {
                    LOG.info("Unsubscribed request to " + uri);
                })
                .map(response -> new HttpContent(position, index, response));
    }

}
