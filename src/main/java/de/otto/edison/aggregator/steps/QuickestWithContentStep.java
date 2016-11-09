package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import rx.Observable;

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
    public Observable<? extends Content> execute(final Parameters parameters) {
        final AtomicInteger index = new AtomicInteger();
        return amb(contentProviders
                .stream()
                .map((cp) -> cp.getContent(position, index.getAndIncrement(), parameters))
                .collect(toList()))
                .takeFirst((c) -> c.getStatus().equals(Content.Status.OK));
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
