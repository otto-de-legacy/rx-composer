package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.providers.ContentProvider;

/**
 * IndexedContent is {@link Content} with an additional index or position in
 * a list of contents. It is used by ContentProviders 
 * in order to preserve an ordering of several possible contents from
 * a list of possible {@link ContentProvider}s.
 * <p>
 *     IndexedContent is a delegate to a Content item.
 * </p>
 */
public final class IndexedContent implements Content {
    private final Content content;
    private final int index;

    private IndexedContent(final Content content, final int index) {
        this.content = content;
        this.index = index;
    }

    public static IndexedContent indexed(final Content content, final int index) {
        return new IndexedContent(content, index);
    }

    public Content getContent() {
        return content;
    }

    /**
     * Returns the source of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique source key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     *
     * @return source identifier
     */
    @Override
    public String getSource() {
        return "'" + index + ":" + content.getSource() + "'";
    }

    /**
     * The content position inside of the {@link Page}
     *
     * @return Position
     */
    @Override
    public Position getPosition() {
        return content.getPosition();
    }

    /**
     * @return true, if content is available and not empty, false otherwise.
     */
    @Override
    public boolean isAvailable() {
        return content.isAvailable();
    }

    /**
     * The body of the content element, as returned from the {@link ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return content.getBody();
    }

    /**
     * Return meta-information about the content returned from a {@link ContentProvider}.
     * <p>
     * For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    @Override
    public Headers getHeaders() {
        return content.getHeaders();
    }

    @Override
    public long getStartedTs() {
        return content.getStartedTs();
    }

    @Override
    public long getCompletedTs() {
        return content.getCompletedTs();
    }

    /**
     * Returns whether or not this instance is a composite, withAll of more than one valid contents.
     *
     * @return boolean
     */
    @Override
    public boolean isComposite() {
        return content.isComposite();
    }

    @Override
    public CompositeContent asComposite() {
        return content.asComposite();
    }

    @Override
    public SingleContent asSingle() {
        return content.asSingle();
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexedContent that = (IndexedContent) o;

        if (index != that.index) return false;
        return content != null ? content.equals(that.content) : that.content == null;

    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + index;
        return result;
    }

    @Override
    public String toString() {
        return "IndexedContent{" +
                "content=" + content +
                ", index=" + index +
                '}';
    }
}
