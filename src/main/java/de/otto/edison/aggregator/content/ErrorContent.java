package de.otto.edison.aggregator.content;

import de.otto.edison.aggregator.Plan;
import de.otto.edison.aggregator.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static de.otto.edison.aggregator.content.Content.Availability.ERROR;
import static de.otto.edison.aggregator.content.Headers.emptyHeaders;
import static java.time.LocalDateTime.now;

public final class ErrorContent implements Content {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorContent.class);

    private final Position position;
    private final Throwable e;
    private LocalDateTime created = now();

    public ErrorContent(final Position position, final Throwable e) {
        this.position = position;
        this.e = e;
        LOG.error("Created ErrorContent for position " + position + ": " + e.getMessage());
    }

    /**
     * The content position inside of the {@link Plan}
     *
     * @return Position
     */
    @Override
    public Position getPosition() {
        return position;
    }

    /**
     * @return true, if content is available and not empty, false otherwise.
     */
    @Override
    public boolean hasContent() {
        return false;
    }

    /**
     * The body of the content element, as returned from the {@link ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return e.getMessage();
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
        return emptyHeaders();
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
        return created;
    }

    /**
     * The availability of the content.
     *
     * @return availability
     */
    @Override
    public Availability getAvailability() {
        return ERROR;
    }
}
