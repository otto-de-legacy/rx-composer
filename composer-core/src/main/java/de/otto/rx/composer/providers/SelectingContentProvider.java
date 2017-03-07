package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import de.otto.rx.composer.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static de.otto.rx.composer.content.CompositeContent.compositeContent;
import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static de.otto.rx.composer.content.IndexedContent.indexed;
import static de.otto.rx.composer.util.Collectors.toImmutableList;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * Selects the most appropriate Content returned from one or more ContentProviders.
 * <p>
 *     The content is selected by a Predicate&lt;Content&gt; and ordered by a Comparator&lt;IndexedContent&gt;.
 * </p>
 */
final class SelectingContentProvider implements ContentProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SelectingContentProvider.class);

    private final ImmutableList<ContentProvider> contentProviders;
    private final ContentMatcher contentMatcher;
    private final Comparator<IndexedContent> comparator;
    private final int count;

    /**
     * Selects the most appropriate Content returned from one or more ContentProviders and returns the
     * highest ranked Content.
     *
     * @param contentProviders the ContentProviders used to fetch contents
     * @param contentMatcher the Matcher used to select the possible content
     * @param comparator the Comparator used to order the possible contents.
     * @param count the number of items to select from the available contents
     */
    SelectingContentProvider(final ImmutableList<ContentProvider> contentProviders,
                             final ContentMatcher contentMatcher,
                             final Comparator<IndexedContent> comparator,
                             final int count) {
        this.contentMatcher = contentMatcher;
        this.contentProviders = contentProviders;
        this.comparator = comparator;
        this.count = count;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Tracer context,
                                          final Parameters parameters) {
        final AtomicInteger subIndex = new AtomicInteger();
        final long startedTs = System.currentTimeMillis();
        final Observable<IndexedContent> mergedContent = merge(contentProviders
                .stream()
                .map(contentProvider -> {
                    final int pos = subIndex.getAndIncrement();
                    return contentProvider
                            .getContent(position, context, parameters)
                            .map(content -> indexed(content, pos))
                            .doOnError(throwable -> LOG.error(throwable.getMessage(), throwable))
                            .onErrorReturn(throwable -> indexed(errorContent(position, throwable, startedTs), pos));
                })
                .collect(toList()));
        return mergedContent
                .filter(contentMatcher::test)
                .toSortedList(comparator::compare)
                .flatMap(list -> list.isEmpty()
                        ? noMatchesFor(position)
                        : just(select(list, count)))
                .doOnError((t) -> LOG.error(t.getMessage(), t));
    }

    private Observable<Content> noMatchesFor(final Position position) {
        LOG.trace("No matches: nothing selected for {}", position.name());
        return empty();
    }

    private Content select(final List<IndexedContent> contents, final int count) {
        final int numAvailable = contents.size();
        checkState(numAvailable > 0);
        checkArgument(count > 0);
        if (count == 1) {
            return selectSingleContent(contents.get(0));
        } else {
            return selectCompositeContent(contents, count);
        }
    }

    private Content selectSingleContent(final IndexedContent content) {
        LOG.trace("Selected first match: {}", content.getSource());
        return content.getContent();
    }

    private Content selectCompositeContent(final List<IndexedContent> contents, final int count) {
        int numAvailable = contents.size();
        final ImmutableList<Content> selected = contents
                .stream()
                .limit(min(numAvailable, count))
                .map(IndexedContent::getContent)
                .collect(toImmutableList());
        final int numSelected = selected.size();
        LOG.trace("Selected {} matches: {}", numSelected, selected.stream().map(Content::getSource).collect(toList()));
        return selected.size() > 1 ? compositeContent(selected) : selected.get(0);
    }

}
