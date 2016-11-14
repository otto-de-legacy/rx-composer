package de.otto.edison.aggregator.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.HttpContent;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final String uri,
                                              final MediaType accept) {
        return (position, index, parameters) -> get(httpClient, uri, accept, position, index);
    }

    public static ContentProvider httpContent(final HttpClient httpClient,
                                              final UriTemplate uriTemplate,
                                              final MediaType accept) {
        return (position, index, parameters) -> {
            final String uri = uriTemplate.expand(parameters.asImmutableMap());
            final String[] missingTemplateVariables = fromTemplate(uri).getVariables();
            if (missingTemplateVariables != null && missingTemplateVariables.length > 0) {
                throw new IllegalArgumentException("Missing URI template variables in parameters. Unable to resolve " + Arrays.toString(missingTemplateVariables));
            }
            return get(httpClient, uri, accept, position, index);
        };
    }

    private static Observable<Content> get(final HttpClient httpClient,
                                           final String uri,
                                           final MediaType accept,
                                           final Position position,
                                           final int index) {
        return httpClient
                .get(uri, accept)
                .doOnNext(c -> LOG.info("Next: " + uri))
                .doOnError(t -> LOG.error(t.getMessage()))
                .doOnUnsubscribe(() -> {
                    LOG.info("Unsubscribed request to " + uri);
                })
                .map(response -> new HttpContent(position, index, response));
    }

}
