package de.otto.rx.composer.client;

/**
 * A reference to a {@link ClientConfig client configuration}.
 * <p>
 *     This type is intended to be implemented by an {@code enum} that is implementing {@code Ref}:
 * </p>
 * <pre><code>
 *     enum MyRef implements Ref {
 *          someConfig,
 *          someOtherConfig
 *     }
 * </code></pre>
 */
public interface Ref {

    String name();

}
