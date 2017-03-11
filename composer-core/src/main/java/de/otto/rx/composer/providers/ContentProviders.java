package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.IndexedContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.content.ContentMatcher.contentMatcher;
import static java.util.Comparator.comparingInt;
import static rx.Observable.just;

public final class ContentProviders {

    private static final Logger LOG = LoggerFactory.getLogger(ContentProviders.class);

    private ContentProviders() {}

    /**
     * Creates a {@link HttpContentProvider} that is using the specified {@code serviceClient} to fetch the content
     * from the specified {@code url}.
     *
     * @param serviceClient ServiceClient used to get content
     * @param url the URL of the requested service
     * @param accept media type of the accepted content.
     * @return ContentProvider
     */
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final String url,
                                              final String accept) {
        return new HttpContentProvider(serviceClient, url, accept, null);
    }

    /**
     * Creates a {@link HttpContentProvider} that is using the specified {@code serviceClient} to fetch the content
     * from the specified {@code url}, falling back to the content returned by the {@code fallback} provider, if
     * the primary content is not available.
     *
     * @param serviceClient ServiceClient used to get content
     * @param url the URL of the requested service
     * @param accept media type of the accepted content.
     * @param fallback ContentProvider used as a fallback, if execution is failing with an exception of HTTP server error.
     * @return ContentProvider
     */
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final String url,
                                              final String accept,
                                              final ContentProvider fallback) {
        return new HttpContentProvider(serviceClient, url, accept, fallback);
    }

    /**
     * Creates a {@link HttpContentProvider} that is using the specified {@code serviceClient} to fetch the content
     * from an URL that is created from {@code uriTemplate} and {@link Parameters}.
     *
     * @param serviceClient ServiceClient used to get content
     * @param uriTemplate the URI template used to create the service URL. The {@link de.otto.rx.composer.content.Parameters}
     *                    param of {@link ContentProvider#getContent(Position, Tracer, Parameters)} is used to fill the
     *                    template variables.
     * @param accept media type of the accepted content.
     * @return ContentProvider
     */
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final UriTemplate uriTemplate,
                                              final String accept) {
        return new HttpContentProvider(serviceClient, uriTemplate, accept, null);
    }

    /**
     * Creates a {@link HttpContentProvider} that is using the specified {@code serviceClient} to fetch the content
     * from an URL that is created from {@code uriTemplate} and {@link Parameters}.
     *
     * @param serviceClient ServiceClient used to get content
     * @param uriTemplate the URI template used to create the service URL. The {@link de.otto.rx.composer.content.Parameters}
     *                    param of {@link ContentProvider#getContent(Position, Tracer, Parameters)} is used to fill the
     *                    template variables.
     * @param accept media type of the accepted content.
     * @param fallback ContentProvider used as a fallback, if execution is failing with an exception of HTTP server error.
     * @return ContentProvider
     */
    public static ContentProvider contentFrom(final ServiceClient serviceClient,
                                              final UriTemplate uriTemplate,
                                              final String accept,
                                              final ContentProvider fallback) {
        return new HttpContentProvider(serviceClient, uriTemplate, accept, fallback);
    }

    /**
     * Semantic sugar to make explicit, that the specified provider is used as a fallback for another provider.
     * <p>
     *     Example:
     * </p>
     * <pre><code>
     *     contentFrom(client, "http://example.com/someContent", TEXT_PLAIN,
     *          fallbackTo(
     *              contentFrom(fallbackClient, "http://example.com/someFallbackContent", TEXT_PLAIN)
     *          )
     *     )
     * </code></pre>
     * <p>
     *     which is exactly equivalent to:
     * </p>
     * <pre><code>
     *     contentFrom(client, "http://example.com/someContent", TEXT_PLAIN,
     *          contentFrom(fallbackClient, "http://example.com/someFallbackContent", TEXT_PLAIN)
     *     )
     * </code></pre>
     *
     * @param fallbackProvider the fallback provider
     * @return ContentProvider
     */
    public static ContentProvider fallbackTo(final ContentProvider fallbackProvider) {
        return fallbackProvider;
    }

    /**
     * Semantic sugar to make explicit, that the specified Observable&lt;Content&gt; is used as a fallback
     * for another provider.
     * <p>
     *     Example:
     * </p>
     * <pre><code>
     *     contentFrom(client, "http://example.com/someContent", TEXT_PLAIN,
     *          fallbackTo(
     *              Observable.just(someFallbackContent())
     *          )
     *     )
     * </code></pre>
     * <p>
     *     which is exactly equivalent to:
     * </p>
     * <pre><code>
     *     contentFrom(client, "http://example.com/someContent", TEXT_PLAIN,
     *          Observable.just(someFallbackContent())
     *     )
     * </code></pre>
     *
     * @param observable the observable content used as fallback
     * @return ContentProvider
     */
    public static ContentProvider fallbackTo(final Observable<Content> observable) {
        return (position, requestContext, parameters) -> observable;
    }

    /**
     * Semantic sugar to make explicit, that the specified {@link Content} is used as a fallback value
     * for another provider.
     * <p>
     *     Example:
     * </p>
     * <pre><code>
     *     contentFrom(client, "http://example.com/someContent", TEXT_PLAIN,
     *          fallbackTo(someFallbackContent())
     *     )
     * </code></pre>
     *
     * @param fallbackContent the content used as fallback
     * @return ContentProvider
     */
    public static ContentProvider fallbackTo(final Content fallbackContent) {
        return (position, requestContext, parameters) -> just(fallbackContent);
    }

    /**
     * Semantic sugar to make explicit, that only a single content provider is used for a fragment.
     * <p>
     *     Example:
     * </p>
     * <pre><code>
     *     fragment(X,
     *          withSingle(
     *                  contentFrom(client, "http://example.com/someContent", TEXT_PLAIN)
     *          )
     *     )
     * </code></pre>
     *
     * @param contentProvider the single content provider
     * @return contentProvider
     */
    public static ContentProvider withSingle(final ContentProvider contentProvider) {
        return contentProvider;
    }

    /**
     * Fetch the {@link Content#isAvailable() available and non-empty} content from the quickest-responding
     * ContentProviders.
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
     * provider is a composite Content, consisting of all single Contents from the providers in the same order as
     * specified.
     *
     * @param contentProviders the providers used to generate the composite Content. The ordering of the content
     *                         providers is used to order the composite contents.
     * @return ContentProvider
     */
    public static ContentProvider withAll(final ContentProvider... contentProviders) {
        checkNotNull(contentProviders, "Parameter must not be null");
        return withAll(ImmutableList.copyOf(contentProviders));
    }

    /**
     * Fetch contents from all the given providers for a single position. The {@link Content} returned by this
     * provider is a composite Content, consisting of all single Contents from the providers in the same order as
     * specified.
     * <p>
     *     All ContentProviders must provide content for the same position, otherwise fetching the contents will
     *     fail with an IllegalArgumentException!
     * </p>
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
     * The {@link Content} returned by this provider is a composite Content, consisting of all single Contents
     * from the providers in the same order as specified.
     * <p>
     *     All ContentProviders must provide content for the same position, otherwise fetching the contents will
     *     fail with an IllegalArgumentException!
     * </p>
     *
     * @param predicate the predicate used to match the contents
     * @param contentProviders the providers used to generate the composite Content. The ordering of the content
     *                         providers is used to order the composite contents.
     * @return ContentProvider
     */
    public static ContentProvider withAllMatching(final Predicate<Content> predicate,
                                                  final ImmutableList<ContentProvider> contentProviders) {
        return new SelectingContentProvider(
                contentProviders,
                contentMatcher(predicate.and(Content::isAvailable), "No content available"),
                comparingInt(IndexedContent::getIndex),
                contentProviders.size());
    }


}
