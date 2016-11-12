package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import rx.Observable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static rx.Observable.amb;

/**
 * A single fetch in a Plan to retrieve content.
 */
class QuickestWithContentStep implements Step {

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
        return amb(contentProviders
                .stream()
                .map((contentProvider) -> {
                    final int pos = index.getAndIncrement();
                    try {
                        return contentProvider.getContent(position, pos, parameters);
                    } catch (final Exception e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList()))
                .takeFirst(Content::hasContent);
    }

    @Override
    public Position getPosition() {
        return position;
    }

}
