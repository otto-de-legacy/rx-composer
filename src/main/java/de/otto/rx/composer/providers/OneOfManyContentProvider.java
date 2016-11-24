package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.otto.rx.composer.content.ErrorContent.*;
import static de.otto.rx.composer.content.IndexedContent.*;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * Selects the most appropriate Content returned from one or more ContentProviders.
 * <p>
 *     The content is selected by a Predicate&lt;Content&gt; and ordered by a Comparator&lt;IndexedContent&gt;.
 * </p>
 */
final class OneOfManyContentProvider implements ContentProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OneOfManyContentProvider.class);

    private final ImmutableList<ContentProvider> contentProviders;
    private final ContentMatcher contentMatcher;
    private final Comparator<IndexedContent> comparator;

    /**
     * Selects the most appropriate Content returned from one or more ContentProviders and returns the
     * highest ranked Content.
     *
     * @param contentProviders the ContentProviders used to fetch contents
     * @param contentMatcher the Matcher used to select the possible content
     * @param comparator the Comparator used to order the possible contents.
     */
    OneOfManyContentProvider(final ImmutableList<ContentProvider> contentProviders,
                             final ContentMatcher contentMatcher,
                             final Comparator<IndexedContent> comparator) {
        this.contentMatcher = contentMatcher;
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
                                .map(content -> indexed(content, pos))
                                .doOnError(throwable -> LOG.error(throwable.getMessage(), throwable))
                                .onErrorReturn(throwable -> indexed(errorContent(position, throwable), pos));
                    } catch (final Exception e) {
                        return just(indexed(errorContent(position, e), pos));
                    }
                })
                .collect(Collectors.toList()));
        return mergedContent
                .filter(contentMatcher::test)
                .toSortedList(comparator::compare)
                .flatMap(list -> list.isEmpty()
                        ? noMatchesFor(position)
                        : just(select(list.get(0).getContent())))
                .doOnError((t) -> LOG.error(t.getMessage(), t));
    }

    private Observable<Content> noMatchesFor(final Position position) {
        LOG.trace("No matches: nothing selected for {}", position.name());
        return empty();
    }

    private Content select(final Content content) {
        LOG.trace("Selected first match: {}", content.getSource());
        return content;
    }

}
