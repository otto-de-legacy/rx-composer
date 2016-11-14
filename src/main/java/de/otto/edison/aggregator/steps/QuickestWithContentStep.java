package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.ErrorContent;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * Fetches the {@link Content} of the quickest responding {@link ContentProvider} that is available and does
 * not fail with an exception.
 */
class QuickestWithContentStep implements Step {

    private static final Logger LOG = LoggerFactory.getLogger(QuickestWithContentStep.class);

    private final ImmutableList<ContentProvider> contentProviders;
    private final Position position;

    QuickestWithContentStep(final Position position,
                            final ImmutableList<ContentProvider> contentProviders) {
        this.position = position;
        this.contentProviders = contentProviders;
    }

    @Override
    public Observable<Content> execute(final Parameters parameters) {
        final AtomicInteger index = new AtomicInteger();
        return merge(contentProviders
                .stream()
                .map(contentProvider -> {
                    final int pos = index.getAndIncrement();
                    try {
                        return contentProvider
                                .getContent(position, pos, parameters)
                                .doOnError((t) -> LOG.error(t.getMessage(), t))
                                .onErrorReturn((e) -> new ErrorContent(position, pos, e));
                    } catch (final Exception e) {
                        return just(new ErrorContent(position, pos, e));
                    }
                })
                .collect(Collectors.toList()))
                .takeUntil(c -> c != null && c.hasContent())
                .doOnError((t) -> LOG.error(t.getMessage(), t))
                .last();
    }

    @Override
    public Position getPosition() {
        return position;
    }

}
