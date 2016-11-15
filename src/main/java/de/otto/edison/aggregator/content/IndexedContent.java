package de.otto.edison.aggregator.content;

import de.otto.edison.aggregator.Plan;
import de.otto.edison.aggregator.providers.ContentProvider;

import java.time.LocalDateTime;

public final class IndexedContent implements Content {
    private final Content content;
    private final int index;

    public IndexedContent(final Content content, final int index) {
        this.content = content;
        this.index = index;
    }

    public Content getContent() {
        return content;
    }

    /**
     * The content position inside of the {@link Plan}
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
    public boolean hasContent() {
        return content.hasContent();
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

    /**
     * The creation time stamp of the content element.
     * <p>
     * Primarily used for logging purposes.
     * </p>
     *
     * @return created ts
     */
    @Override
    public LocalDateTime getCreated() {
        return content.getCreated();
    }

    /**
     * The availability of the content.
     *
     * @return availability
     */
    @Override
    public Availability getAvailability() {
        return content.getAvailability();
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
