package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.uniqueIndex;
import static de.otto.rx.composer.content.EmptyContent.emptyContent;

/**
 * Threadsafe container used to gather {@link Content}s when executing a {@link de.otto.rx.composer.Plan}.
 */
public final class Contents {

    public static class Builder {

        private final Queue<Content> results = new ConcurrentLinkedQueue<>();

        /**
         * Add a content item to the collection of contents if content is available.
         *
         * @param content the added content item.
         */
        public Builder add(final Content content) {
            if (content.isAvailable()) {
                results.add(content);
            }
            return this;
        }

        public Contents build() {
            return new Contents(
                uniqueIndex(results, Content::getPosition)
            );
        }
    }

    private final ImmutableMap<Position, Content> results;

    private Contents(final ImmutableMap<Position, Content> results) {
        this.results = results;
    }

    public static Contents.Builder contentsBuilder() {
        return new Builder();
    }

    /**
     * Returns the collection of {@link Content#isAvailable() available}
     * {@link Content} items. Only one content per Position is returned.
     *
     * @return Immutable collection of available contents.
     */
    public ImmutableCollection<Content> getAll() {
        return copyOf(results.values());
    }

    /**
     * Returns the {@link Content} for the specified {@link Position} if it {@link Content#isAvailable() has content},
     * or {@link EmptyContent empty content} if nothing is available.
     *
     * @param position the content position
     * @return possibly empty content
     */
    public Content get(final Position position) {
        final Content content = results.get(position);
        return content != null ? content : emptyContent(position);
    }

    /**
     * Returns the body of the {@link Content} for the specified {@link Position} if it {@link Content#isAvailable() has content},
     * or {@link EmptyContent empty content} if nothing is available.
     *
     * @param position the content position
     * @return body or empty string
     */
    public String getBody(final Position position) {
        return get(position).getBody();
    }

}
