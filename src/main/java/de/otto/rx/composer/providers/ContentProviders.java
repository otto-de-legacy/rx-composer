package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.IndexedContent;
import de.otto.rx.composer.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import static de.otto.rx.composer.content.ContentMatcher.contentMatcher;
import static java.util.Comparator.comparingInt;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    public static ContentProvider fetchViaHttpGet(final HttpClient httpClient,
                                                  final String url,
                                                  final MediaType accept) {
        return new HttpGetContentProvider(httpClient, url, accept);
    }

    public static ContentProvider fetchViaHttpGet(final HttpClient httpClient,
                                                  final UriTemplate uriTemplate,
                                                  final MediaType accept) {
        return new HttpGetContentProvider(httpClient, uriTemplate, accept);
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
        return new QuickestWinsContentProvider(
                contentProviders,
                contentMatcher(Content::hasContent, "Not content available"));
    }

    public static ContentProvider fetchFirst(final ImmutableList<ContentProvider> contentProviders) {
        return new OneOfManyContentProvider(
                contentProviders,
                contentMatcher(Content::hasContent, "Not content available"),
                comparingInt(IndexedContent::getIndex));
    }


}
