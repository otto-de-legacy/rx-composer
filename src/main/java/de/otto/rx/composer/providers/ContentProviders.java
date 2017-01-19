package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.IndexedContent;
import de.otto.rx.composer.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static de.otto.rx.composer.content.ContentMatcher.contentMatcher;
import static java.util.Comparator.comparingInt;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    public static ContentProvider contentFrom(final HttpClient httpClient,
                                              final String url,
                                              final String accept) {
        return new HttpGetContentProvider(httpClient, url, accept);
    }

    public static ContentProvider contentFrom(final HttpClient httpClient,
                                              final UriTemplate uriTemplate,
                                              final String accept) {
        return new HttpGetContentProvider(httpClient, uriTemplate, accept);
    }

    public static ContentProvider withResilient(final ContentProvider delegate,
                                            final int timeoutMillis,
                                            final String commandKey) {
        return new CircuitBreakingContentProvider(delegate, timeoutMillis, commandKey);
    }

    public static ContentProvider withSingle(final ContentProvider contentProvider) {
        return contentProvider;
    }

    /**
     * Fetch the quickest {@link Content#isAvailable() available and non-empty} content from the given ContentProviders.
     * <p>
     *     This method can be used to implement a "Fan Out and Quickest Wins" pattern, if there
     *     are multiple possible providers and 'best performance' is most important.
     * </p>
     *
     * @param contentProviders the ContentProviders.
     * @return QuickestWinsContentProvider
     */
    public static ContentProvider withQuickest(final ImmutableList<ContentProvider> contentProviders) {
        return new QuickestWinsContentProvider(
                contentProviders,
                contentMatcher(Content::isAvailable, "No content available"));
    }

    /**
     * Fetch the first {@link Content#isAvailable() available and non-empty} content from the given ContentProviders.
     * <p>
     *     Use this, if you need a prioritized list of providers, where you would prefer the content from the first
     *     provider over content coming from the second, and so on.
     * </p>
     *
     * @param contentProviders list of content providers, where the first entries are more important than following entries.
     * @return OneOfManyContentProvider
     */
    public static ContentProvider withFirst(final ImmutableList<ContentProvider> contentProviders) {
        return new SelectingContentProvider(
                contentProviders,
                contentMatcher(Content::isAvailable, "No content available"),
                comparingInt(IndexedContent::getIndex),
                1);
    }

    /**
     * Fetch the first {@link Content#isAvailable() available and non-empty} content from the given ContentProviders
     * that is matching the specified predicate.
     * <p>
     *     Use this, if you need a prioritized list of providers, where you would prefer the content from the first
     *     provider over content coming from the second, and so on.
     * </p>
     *
     * @param predicate the predicate used to match the contents
     * @param contentProviders list of content providers, where the first entries are more important than following entries.
     * @return OneOfManyContentProvider
     */
    public static ContentProvider withFirstMatching(final Predicate<Content> predicate,
                                                    final ImmutableList<ContentProvider> contentProviders) {
        return new SelectingContentProvider(
                contentProviders,
                contentMatcher(predicate.and(Content::isAvailable), "No content available"),
                comparingInt(IndexedContent::getIndex),
                1);
    }

    /**
     * Fetch contents from all the given providers for a single position. The {@link Content} returned by this
     * provider is a composite Content, withAll of all single Contents from the providers in the same order as
     * specified.
     *
     * @param contentProviders the providers used to generate the composite Content. The ordering of the content
     *                         providers is used to order the composite contents.
     * @return ContentProvider
     */
    public static ContentProvider withAll(final ImmutableList<ContentProvider> contentProviders) {
        return new SelectingContentProvider(
                contentProviders,
                contentMatcher(Content::isAvailable, "No content available"),
                comparingInt(IndexedContent::getIndex),
                contentProviders.size());
    }

    /**
     * Fetch all contents matching the given predicate from all the given providers for a single position.
     * The {@link Content} returned by this provider is a composite Content, withAll of all single Contents
     * from the providers in the same order as specified.
     *
     * @param predicate the predicate used to match the contents
     * @param contentProviders the providers used to generate the composite Content. The ordering of the content
     *                         providers is used to order the composite contents.
     * @return ContentProvider
     */
    public static ContentProvider withAllMatching(final Predicate<Content> predicate, final ImmutableList<ContentProvider> contentProviders) {
        return new SelectingContentProvider(
                contentProviders,
                contentMatcher(predicate.and(Content::isAvailable), "No content available"),
                comparingInt(IndexedContent::getIndex),
                contentProviders.size());
    }


}
