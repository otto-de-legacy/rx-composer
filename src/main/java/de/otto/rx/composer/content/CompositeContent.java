package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.StringJoiner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static java.time.LocalDateTime.now;

/**
 * A composite withAll of two ore more available {@link Content} items.
 * <p>
 *     All items of the Composite must have the same content {@link #getPosition() position}.
 * </p>
 *
 * {@inheritDoc}
 */
public final class CompositeContent implements Content {

    private final LocalDateTime created = now();
    private final Headers headers;
    private final ImmutableList<Content> contents;

    private CompositeContent(final Headers headers, final ImmutableList<Content> contents) {
        checkNotNull(headers, "Parameter 'headers' must not be null");
        checkArgument(contents.size() > 1, "A composite must at least have two content items.");
        final Position expectedPosition = contents.get(0).getPosition();
        checkArgument(contents.stream().allMatch(content -> content.getPosition().equals(expectedPosition)), "All contents of a Composite must have the same position");
        this.contents = contents;
        this.headers = headers;
    }

    public static CompositeContent compositeContent(final ImmutableList<Content> contents) {
        return new CompositeContent(emptyHeaders(), contents);
    }

    public static CompositeContent compositeContent(final Headers headers, final ImmutableList<Content> contents) {
        return new CompositeContent(headers, contents);
    }

    /**
     * {@inheritDoc}
     */
    public Position getPosition() {
        return contents.get(0).getPosition();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation is returning a comma-separated list of the sources returned by the composite items.
     */
    @Override
    public String getSource() {
        final StringJoiner joiner = new StringJoiner(",");
        contents.forEach(c->joiner.add(c.getSource()));
        return joiner.toString();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation returns true, if at least a single composite item is available.
     */
    public boolean isAvailable() {
        return contents.stream().anyMatch(Content::isAvailable);
    }

    /**
     * {@inheritDoc}
     *
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation is concatenating the bodies of the composite items.
     * An newline char is used to separate the bodies.
     */
    public String getBody() {
        final StringJoiner joiner = new StringJoiner("\n");
        contents.forEach(c->joiner.add(c.getBody()));
        return joiner.toString();
    }

    /**
     * {@inheritDoc}
     */
    public LocalDateTime getCreated() {
        return created;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComposite() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompositeContent asComposite() {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * This implementation will always throw an IllegalStateException, as this is not a SingleContent.
     */
    @Override
    public SingleContent asSingle() {
        throw new IllegalStateException("Not a SingleContent");
    }

    /**
     * Returns the items of the composite.
     *
     * @return immutable list of contents
     */
    public ImmutableList<Content> getContents() {
        return contents;
    }

}
