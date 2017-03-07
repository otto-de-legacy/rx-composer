package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.tracer.Stats;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.uniqueIndex;
import static de.otto.rx.composer.content.MissingContent.missingContent;

/**
 * Threadsafe container used to gather {@link Content}s when executing a {@link Page}.
 */
public final class Contents {

    public static class Builder {

        private final Queue<Content> results = new ConcurrentLinkedQueue<>();
        private Stats stats;

        /**
         * Add a content item to the collection of contents if content is available.
         *
         * @param content the added content item.
         * @return this
         */
        public Builder add(final Content content) {
            if (content.isAvailable()) {
                results.add(content);
            }
            return this;
        }

        public Builder setStats(Stats stats) {
            this.stats = stats;
            return this;
        }

        public Contents build() {
            return new Contents(
                    stats,
                    uniqueIndex(results, (Content c) -> c.getPosition().name())
            );
        }

    }

    private final Stats stats;
    private final ImmutableMap<String, Content> results;

    private Contents(final Stats stats, final ImmutableMap<String, Content> results) {
        this.stats = stats != null ? stats : Stats.emptyStats();
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
     * or {@link MissingContent empty content} if nothing is available.
     *
     * @param position the content position
     * @return possibly empty content
     */
    public Content get(final Position position) {
        final Content content = results.get(position.name());
        return content != null ? content : missingContent(position, stats.getStartedTs());
    }

    public Content get(final String position) {
        final Content content = results.get(position);
        return content != null ? content : missingContent(() -> position, stats.getStartedTs());
    }

    /**
     * Returns the body of the {@link Content} for the specified {@link Position} if it {@link Content#isAvailable() has content},
     * or {@link MissingContent empty content} if nothing is available.
     *
     * @param position the content position
     * @return body or empty string
     */
    public String getBody(final Position position) {
        return get(position).getBody();
    }

    /**
     * Returns execution-time statistics of the whole page.
     *
     * @return Stats
     */
    public Stats getStats() {
        return stats;
    }

}
