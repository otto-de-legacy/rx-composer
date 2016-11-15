package de.otto.edison.aggregator.providers;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.ErrorContent;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.stream.Collectors;

import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * Fetches the {@link Content} of the quickest responding {@link ContentProvider} that is available and does
 * not fail with an exception.
 */
class QuickestWinsContentProvider implements ContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickestWinsContentProvider.class);

    private final ImmutableList<ContentProvider> contentProviders;

    QuickestWinsContentProvider(final ImmutableList<ContentProvider> contentProviders) {
        this.contentProviders = contentProviders;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        return merge(contentProviders
                .stream()
                .map(contentProvider -> {
                    try {
                        return contentProvider
                                .getContent(position, parameters)
                                .doOnError((t) -> LOG.error(t.getMessage(), t))
                                .onErrorReturn((e) -> new ErrorContent(position, e));
                    } catch (final Exception e) {
                        return just(new ErrorContent(position, e));
                    }
                })
                .collect(Collectors.toList()))
                .takeUntil(c -> c != null && c.hasContent())
                .doOnError((t) -> LOG.error(t.getMessage(), t))
                .last();
    }


}
