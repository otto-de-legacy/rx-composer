package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static java.time.LocalDateTime.now;

public final class EmptyContent extends SingleContent {

    private static final Logger LOG = LoggerFactory.getLogger(EmptyContent.class);

    private final Position position;
    private final String source;
    private LocalDateTime created = now();

    private EmptyContent(final Position position, final String source) {
        this.position = position;
        this.source = source;
        LOG.info("Created EmptyContent for position {} from source {}", position, source);
    }

    public static EmptyContent emptyContent(final Position position) {
        return new EmptyContent(position, position.name());
    }

    public static EmptyContent emptyContent(final Position position, final String source) {
        return new EmptyContent(position, source);
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
        return source;
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
