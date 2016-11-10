package de.otto.edison.aggregator.content;

import java.time.LocalDateTime;

/**
 * Possible Content fragment for a {@link Position} on a page.
 * <p>
 *     If multiple content items are available for a position, the {@link de.otto.edison.aggregator.steps.Step}
 *     that is fetching the content will decide, which content is selected for the position. In this case,
 *     the {@link #getIndex() index} of the content is used to distinguish between the different contents for a
 *     position.
 * </p>
 * <p>
 *     Content is retrieved using {@link de.otto.edison.aggregator.providers.ContentProvider}s.
 * </p>
 *
 */
public interface Content {

    /**
     * Content availability
     */
    public enum Availability {
        /** Content is available and not empty. */
        AVAILABLE,
        /** Content is empty, but no error occured. */
        EMPTY,
        /** An error occured. The content {@link #getBody() body} may contain an error response. */
        ERROR
    }

    /**
     * The content position inside of the {@link de.otto.edison.aggregator.Plan}
     *
     * @return Position
     */
    Position getPosition();

    /**
     * The index of the content, if multiple contents are applicable to the same position.
     *
     * @return index
     */
    int getIndex();

    /**
     *
     * @return true, if content is available and not-empty, false otherwise.
     */
    boolean hasContent();

    /**
     * The body of the content element, as returned from the {@link de.otto.edison.aggregator.providers.ContentProvider}
     *
     * @return body or empty String
     */
    String getBody();

    /**
     * Return meta-information about the content returned from a {@link de.otto.edison.aggregator.providers.ContentProvider}.
     * <p>
     *     For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    Headers getHeaders();

    /**
     * The creation time stamp of the content element.
     * <p>
     *     Primarily used for logging purposes.
     * </p>
     *
     * @return created ts
     */
    LocalDateTime getCreated();

    /**
     * The availability of the content.
     *
     * @return availability
     */
    Availability getAvailability();
}
