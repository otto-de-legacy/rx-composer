package de.otto.rx.composer.content;

import java.time.LocalDateTime;

/**
 * A Content fragment for a {@link Position} on a page.
 * <p>
 *     Content is retrieved using {@link de.otto.rx.composer.providers.ContentProvider}s. Depending on the specific
 *     provider, the content may either be a {@link SingleContent} or a {@link CompositeContent}.
 * </p>
 * <p>
 *     If multiple content items are available for a position, the {@link de.otto.rx.composer.providers.ContentProvider}
 *     that is fetching the content will decide, which content is selected for the position.
 * </p>
 * <p>
 *     ContentProviders that are generating multiple Contents for a single position in a {@link de.otto.rx.composer.Plan}
 *     will return a {@link #isComposite() composite} Content instance. In this case {@code isComposite} will return
 *     true and {@link #asComposite()} will return a {@link CompositeContent}.
 * </p>
 */
public interface Content {

    /**
     * The content position inside of the {@link de.otto.rx.composer.Plan}
     *
     * @return Position
     */
    Position getPosition();

    /**
     * Returns the source of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique source key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     * <p>
     *     The source is mostly used for logging purposes.
     * </p>
     *
     * @return content source
     */
    String getSource();

    /**
     *
     * @return true, if content is available and not empty, false otherwise.
     */
    boolean isAvailable();

    /**
     * The body of the content element, as returned from the {@link de.otto.rx.composer.providers.ContentProvider}
     *
     * @return body or empty String
     */
    String getBody();

    /**
     * Return meta-information about the content returned from a {@link de.otto.rx.composer.providers.ContentProvider}.
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
     * Returns whether or not this instance is a composite, consisting of more than one valid contents.
     *
     * @return boolean
     */
    default boolean isComposite() {
        return false;
    };

    /**
     * If this content is a {@link #isComposite() composite}, a {@link CompositeContent} is returned.
     * @return CompositeContent
     * @throws IllegalStateException if this is not a composite content.
     */
    CompositeContent asComposite();

    /**
     * If this content is not a {@link #isComposite() composite}, a {@link SingleContent} is returned.
     * @return SingleContent
     * @throws IllegalStateException if this is a composite content.
     */
    SingleContent asSingle();

}
