package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 *     The content is selected by a Predicate&lt;Content&gt; and ordered by a Comparator&lt;IndexedContent&gt;.
 * </p>
 */
class FetchOneOfManyContentProvider implements ContentProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FetchOneOfManyContentProvider.class);

    private final ImmutableList<ContentProvider> contentProviders;
    private final Predicate<Content> selector;
    private final Comparator<IndexedContent> comparator;

    /**
     * Selects the most appropriate Content returned from one or more ContentProviders and returns the
     * highest ranked Content.
     *
     * @param contentProviders the ContentProviders used to fetch contents
     * @param selector the Predicate used to select the possible content
     * @param comparator the Comparator used to order the possible contents.
     */
    FetchOneOfManyContentProvider(final ImmutableList<ContentProvider> contentProviders,
                                  final Predicate<Content> selector,
                                  final Comparator<IndexedContent> comparator) {
        this.selector = selector;
        this.contentProviders = contentProviders;
        this.comparator = comparator;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        final AtomicInteger subIndex = new AtomicInteger();
        final Observable<IndexedContent> mergedContent = merge(contentProviders
                .stream()
                .map(contentProvider -> {
                    final int pos = subIndex.getAndIncrement();
                    try {
                        return contentProvider
                                .getContent(position, parameters)
                                .map(content ->  new IndexedContent(content, pos))
                                .doOnError(throwable -> LOG.error(throwable.getMessage(), throwable))
                                .onErrorReturn(throwable -> new IndexedContent(new ErrorContent(position, throwable), pos));
                    } catch (final Exception e) {
                        return just(new IndexedContent(new ErrorContent(position, e), pos));
                    }
                })
                .collect(Collectors.toList()));
        return mergedContent
                .filter(selector::test)
                .toSortedList(comparator::compare)
                .flatMap(list -> list.isEmpty()
                        ? empty()
                        : just(list.get(0).getContent()))
                .doOnError((t) -> LOG.error(t.getMessage(), t));
    }

}
