package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static java.time.LocalDateTime.now;

public final class ErrorContent extends SingleContent {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorContent.class);

    private final Position position;
    private final Throwable e;
    private LocalDateTime created = now();

    private ErrorContent(final Position position, final Throwable e) {
        this.position = position;
        this.e = e;
        LOG.error("Created ErrorContent for position " + position + ": " + e.getMessage());
    }

    public static ErrorContent errorContent(final Position position, final Throwable e) {
        return new ErrorContent(position, e);
    }

    /**
     * Returns the source of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique source key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     * <p>
     *     ErrorContent does not know about the 'real' source, position.name() is used instead.
     * </p>
     * @return source identifier
     */
    @Override
    public String getSource() {
        return position.name();
    }

    /**
     * The content position inside of the {@link Page}
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
    public boolean isAvailable() {
        return false;
    }

    /**
     * The body of the content element, as returned from the {@link ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return "";
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

}
