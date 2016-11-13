package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.ErrorContent;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import rx.Observable;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * Selects the most appropriate Content returned from one or more ContentProviders.
 */
class FetchOneOfManyStep implements Step {

    private final ImmutableList<ContentProvider> contentProviders;
    private final Predicate<Content> selector;
    private final Position position;
    private final Comparator<Content> comparator;

    /**
     * Selects the most appropriate Content returned from one or more ContentProviders and returns the highest ranked Content.
     *
     * @param position the position of the returned content.
     * @param contentProviders the ContentProviders used to fetch contents
     * @param selector the Predicate used to select the possible content
     * @param comparator the Comparator used to order the possible contents.
     */
    FetchOneOfManyStep(final Position position,
                       final ImmutableList<ContentProvider> contentProviders,
                       final Predicate<Content> selector,
                       final Comparator<Content> comparator) {
        this.position = position;
        this.selector = selector;
        this.contentProviders = contentProviders;
        this.comparator = comparator;
    }

    @Override
    public Observable<Content> execute(final Parameters parameters) {
        final AtomicInteger index = new AtomicInteger();
        final Observable<Content> mergedContent = merge(contentProviders
                .stream()
                .map(contentProvider -> {
                    final int pos = index.getAndIncrement();
                    try {
                        return contentProvider
                                .getContent(position, pos, parameters)
                                .onErrorReturn((e) -> new ErrorContent(position, pos, e));
                    } catch (final Exception e) {
                        return just(new ErrorContent(position, pos, e));
                    }
                })
                .collect(Collectors.toList()));
        return mergedContent
                .filter(selector::test)
                .toSortedList(comparator::compare)
                .flatMap(list -> list.isEmpty()
                        ? empty()
                        : just(list.get(0)));
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
