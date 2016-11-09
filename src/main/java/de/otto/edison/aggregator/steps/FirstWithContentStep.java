package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import rx.Observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * A single fetch in a Plan to retrieve content.
 */
class FirstWithContentStep implements Step {

    private final ImmutableList<ContentProvider> contentProviders;
    private final Position position;

    FirstWithContentStep(final Position position,
                         final ImmutableList<ContentProvider> contentProviders) {
        this.position = position;
        this.contentProviders = contentProviders;
    }

    @Override
    public Observable<? extends Content> execute(final Parameters parameters) {
        final AtomicInteger index = new AtomicInteger();
        final Observable<? extends Content> mergedContent = merge(contentProviders
                .stream()
                .map(contentProvider -> contentProvider.getContent(position, index.getAndIncrement(), parameters))
                .collect(Collectors.toList()));
        return mergedContent
                .filter(Content::hasContent)
                .toSortedList(FirstWithContentStep::sortByPosition)
                .flatMap(list -> list.isEmpty()
                        ? empty()
                        : just(list.get(0)));
    }

    private static Integer sortByPosition(final Content first, final Content second) {
        return compare(first.getIndex(), second.getIndex());
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
