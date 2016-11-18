package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableSet;
import jersey.repackaged.com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Optional.ofNullable;

/**
 * Threadsafe container used to gather {@link Content}s when executing a {@link de.otto.rx.composer.Plan}.
 * <p>
 *     Contents are separated by {@link de.otto.rx.composer.content.Content.Availability}.
 * </p>
 */
public final class Contents {

    private final ConcurrentMap<Position, Content> results = new ConcurrentHashMap<>();
    private final Set<Position> empty = newConcurrentHashSet();
    private final Set<Position> errors = newConcurrentHashSet();

    /**
     * Add a content item to the collection of contents.
     *
     * @param content the added content item.
     */
    public void add(final Content content) {
        switch (content.getAvailability()) {
            case AVAILABLE:
                results.put(content.getPosition(), content);
                break;
            case EMPTY:
                empty.add(content.getPosition());
                break;
            case ERROR:
                errors.add(content.getPosition());
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

    /**
     * Returns the set of positions having empty content. This typically means, that the requested service was
     * unable to select relevant content for the specified position.
     * <p>
     *     Depending on the executed {@link de.otto.rx.composer.Plan} it might happen that there are
     *     empty positions contained in the returned set, while other content for the position is returned
     *     by {@link #getContent(Position)}. This could be an indicator that the usage of Position items inside
     *     the Plan is not perfect (multiple {@link de.otto.rx.composer.providers.ContentProvider}s for
     *     the same Position).
     * </p>
     * @return immutable set of positions without content
     */
    public ImmutableSet<Position> getEmpty() {
        return copyOf(empty);
    }

    /**
     * Returns the set of positions having error content. This typically means, that the requested service was
     * unable to select relevant content because of client- or server-errors.
     * <p>
     *     Depending on the executed {@link de.otto.rx.composer.Plan} it might happen that there are
     *     empty positions contained in the returned set, while other content for the position is returned
     *     by {@link #getContent(Position)}. This could be an indicator that the usage of Position items inside
     *     the Plan is not perfect (multiple {@link de.otto.rx.composer.providers.ContentProvider}s for
     *     the same Position).
     * </p>
     * @return immutable set of positions having error contents.
     */
    public ImmutableSet<Position> getErrors() {
        return copyOf(errors);
    }

    /**
     * Returns true if at least one of the requested services returned with an error response.
     *
     * @return boolean
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
