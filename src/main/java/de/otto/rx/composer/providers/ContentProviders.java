package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.HttpContent;
import de.otto.rx.composer.content.IndexedContent;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static java.util.Comparator.comparingInt;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    public static ContentProvider fetchViaHttpGet(final HttpClient httpClient,
                                                  final String uri,
                                                  final MediaType accept) {
        return (position, parameters) -> get(httpClient, uri, accept, position);
    }

    public static ContentProvider fetchViaHttpGet(final HttpClient httpClient,
                                                  final UriTemplate uriTemplate,
                                                  final MediaType accept) {
        return (position, parameters) -> {
            final String uri = uriTemplate.expand(parameters.asImmutableMap());
            final String[] missingTemplateVariables = fromTemplate(uri).getVariables();
            if (missingTemplateVariables != null && missingTemplateVariables.length > 0) {
                throw new IllegalArgumentException("Missing URI template variables in parameters. Unable to resolve " + Arrays.toString(missingTemplateVariables));
            }
            return get(httpClient, uri, accept, position);
        };
    }

    /**
     * Fetch the content from the quickest ContentProvider.
     * <p>
     *     This method can be used to implement a "Fan Out and Quickest Wins" pattern, if there
     *     are multiple possible providers and 'best performance' is most important.
     * </p>
     * @param contentProviders the ContentProviders.
     * @return Step
     */
    public static ContentProvider fetchQuickest(final ImmutableList<ContentProvider> contentProviders) {
        return new QuickestWinsContentProvider(contentProviders);
    }

    public static ContentProvider fetchFirst(final ImmutableList<ContentProvider> contentProviders) {
        return new FetchOneOfManyContentProvider(
                contentProviders,
                Content::hasContent,
                comparingInt(IndexedContent::getIndex));
    }

    private static Observable<Content> get(final HttpClient httpClient,
                                           final String uri,
                                           final MediaType accept,
                                           final Position position) {
        return httpClient
                .get(uri, accept)
                .doOnNext(c -> LOG.info("Next: " + uri))
                .doOnError(t -> LOG.error(t.getMessage()))
                .doOnUnsubscribe(() -> {
                    LOG.info("Unsubscribed request to " + uri);
                })
                .map(response -> new HttpContent(position, response));
    }

}
