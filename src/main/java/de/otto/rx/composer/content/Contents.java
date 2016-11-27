package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableCollection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.ImmutableList.copyOf;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.EmptyContent.emptyContent;

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
     * Returns the collection of {@link de.otto.rx.composer.content.Content.Availability#AVAILABLE available}
     * {@link Content} items. Only one content per Position is returned.
     *
     * @return Immutable collection of available contents.
     */
    public ImmutableCollection<Content> getAll() {
        return copyOf(results.values());
    }

    /**
     * Returns the {@link Content} for the specified {@link Position} if it {@link Content#hasContent() has content},
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
     * Returns the body of the {@link Content} for the specified {@link Position} if it {@link Content#hasContent() has content},
     * or {@link EmptyContent empty content} if nothing is available.
     *
     * @param position the content position
     * @return body or empty string
     */
    public String getBody(final Position position) {
        return get(position).getBody();
    }

}
