package de.otto.rx.composer.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.EMPTY;
import static de.otto.rx.composer.content.Content.Availability.ERROR;
import static java.time.LocalDateTime.now;

public final class HttpContent implements Content {

    private static final Logger LOG = LoggerFactory.getLogger(HttpContent.class);

    private final String source;
    private final Position position;
    private final String body;
    private final Availability availability;
    private final Headers headers;
    private final LocalDateTime created = now();

    /**
     * Create a HttpContent element, representing {@link Content} retrieved from a (micro)service.
     *
     * @param source The URI of the requested service
     * @param position The content position inside of the {@link de.otto.rx.composer.Plan}.
     * @param response The HTTP response returned from a different (micro)service.
     */
    public HttpContent(final String source,
                       final Position position,
                       final Response response) {
        this.source = source;
        this.position = position;
        this.body = response.readEntity(String.class);
        this.availability = response.getStatus() > 399
                ? ERROR
                : body == null || body.isEmpty() ? EMPTY : AVAILABLE;
        this.headers = Headers.of(response.getHeaders());
        LOG.trace("{} content pos={} status={} source={}", availability, position, response.getStatus(), source);
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
        return source;
    }

    /**
     * The content position inside of the {@link de.otto.rx.composer.Plan}
     *
     * @return Position
     */
    @Override
    public Position getPosition() {
        return position;
    }

    /**
     *
     * @return true, if content is available and not empty, false otherwise.
     */
    @Override
    public boolean hasContent() {
        return availability == AVAILABLE && !getBody().isEmpty();
    }

    /**
     * The body of the content element, as returned from the {@link de.otto.rx.composer.providers.ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return body != null ? body : "";
    }

    /**
     * Return meta-information about the content.
     * <p>
     *     For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    @Override
    public Headers getHeaders() {
        return headers;
    }

    /**
     * The creation time stamp of the content element.
     * <p>
     *     Primarily used for logging purposes.
     * </p>
     *
     * @return created ts
     */
    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    /**
     * The availability of the content.
     *
     * @return availability
     */
    public Availability getAvailability() {
        return this.availability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpContent that = (HttpContent) o;

        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (availability != that.availability) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        return created != null ? created.equals(that.created) : that.created == null;

    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (availability != null ? availability.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HttpContent{" +
                "position=" + position +
                ", body='" + body + '\'' +
                ", status=" + availability +
                ", headers=" + headers +
                ", created=" + created +
                '}';
    }
}
