package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableSet;
import jersey.repackaged.com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static java.util.Optional.ofNullable;

/**
 * Threadsafe container used to gather {@link Content}s when executing a {@link de.otto.rx.composer.Plan}.
 * <p>
 *     Contents are separated by {@link de.otto.rx.composer.content.Content.Availability}.
 * </p>
 */
public final class Contents {

    private final ConcurrentMap<Position, Content> results = new ConcurrentHashMap<>();

    /**
     * Add a content item to the collection of contents if content is available.
     *
     * @param content the added content item.
     */
    public void add(final Content content) {
        if(content.getAvailability().equals(AVAILABLE)) {
                results.put(content.getPosition(), content);
        }
    }

    /**
     * Returns the list of {@link de.otto.rx.composer.content.Content.Availability#AVAILABLE available}
     * {@link Content} items. Only one content per Position is returned.
     *
     * @return Immutable list of available contents.
     */
    public ImmutableList<Content> getContents() {
        return ImmutableList.copyOf(results.values());
    }

    /**
     * Returns the optionally available {@link Content} for the specified {@link Position}.
     *
     * @param position the content position
     * @return optional content
     */
    public Optional<Content> getContent(final Position position) {
        return ofNullable(results.get(position));
    }

}
