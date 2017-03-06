package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.ContentMatcher;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.stream.Collectors;

import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static rx.Observable.merge;

/**
 * Fetches the {@link Content} of the quickest responding {@link ContentProvider} that is available and does
 * not fail with an exception.
 */
final class QuickestWinsContentProvider implements ContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickestWinsContentProvider.class);

    private final ImmutableList<ContentProvider> contentProviders;
    private final ContentMatcher contentMatcher;

    QuickestWinsContentProvider(final ImmutableList<ContentProvider> contentProviders,
                                final ContentMatcher contentMatcher) {
        this.contentProviders = contentProviders;
        this.contentMatcher = contentMatcher;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Tracer context,
                                          final Parameters parameters) {
        final Observable<Content> mergedContent = merge(contentProviders
                .stream()
                .map(contentProvider -> contentProvider
                        .getContent(position, context, parameters)
                        .doOnError(this::traceError)
                        .onErrorReturn((Throwable t) -> errorContent(position, t)))
                .collect(Collectors.toList()));
        return mergedContent
                .filter(contentMatcher::test)
                .take(1)
                .doOnSubscribe(() -> traceSelectQuickest(position))
                .doOnError(this::traceError)
                .doOnNext(this::traceSelected);
    }

    private void traceError(Throwable t) {
        LOG.error(t.getMessage(), t);
    }

    private void traceSelectQuickest(final Position position) {
        LOG.trace("Selecting quickest content for {} from {} providers", position, contentProviders.size());
    }

    private void traceSelected(Content content) {
        LOG.trace("Selected quickest match: {}", content.getSource());
    }

}
