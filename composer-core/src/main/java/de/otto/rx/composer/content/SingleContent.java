package de.otto.rx.composer.content;

/**
 * A Content that is <em>not</em> a composite, but only a single content item.
 *
 * {@inheritDoc}
 */
public abstract class SingleContent implements Content {

    /**
     * {@inheritDoc}
     *
     * This implementation will throw an IllegalStateException, as a SingleContent is not a CompositeContent.
     */
    @Override
    public final CompositeContent asComposite() {
        throw new IllegalStateException("Not a CompositeContent");
    }

    @Override
    public final SingleContent asSingle() {
        return this;
    }

}
