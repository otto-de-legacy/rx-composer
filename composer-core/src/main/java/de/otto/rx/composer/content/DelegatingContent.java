package de.otto.rx.composer.content;

/**
 * Implementation of a Content that is delegating every call to another content instance (the delegate).
 * <p>
 *     This implementation is sometimes useful, if you want to override one or more methods of some content.
 * </p>
 */
public abstract class DelegatingContent implements Content {
    private final Content delegate;

    protected DelegatingContent(final Content delegate) {
        this.delegate = delegate;
    }

    @Override
    public Position getPosition() {
        return delegate.getPosition();
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public String getBody() {
        return delegate.getBody();
    }

    @Override
    public Headers getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public long getStartedTs() {
        return delegate.getStartedTs();
    }

    @Override
    public long getCompletedTs() {
        return delegate.getCompletedTs();
    }

    @Override
    public boolean isComposite() {
        return delegate.isComposite();
    }

    @Override
    public CompositeContent asComposite() {
        return delegate.asComposite();
    }

    @Override
    public SingleContent asSingle() {
        return delegate.asSingle();
    }

    @Override
    public long getTotalRuntime() {
        return delegate.getTotalRuntime();
    }

    @Override
    public long getAvgRuntime() {
        return delegate.getAvgRuntime();
    }

    @Override
    public boolean isErrorContent() {
        return delegate.isErrorContent();
    }

    @Override
    public ErrorContent asErrorContent() {
        return delegate.asErrorContent();
    }

}
