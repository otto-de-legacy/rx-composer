package de.otto.rx.composer.client;

/**
 * A reference to a {@link ClientConfig client configuration}.
 *
 * <p>Usage:</p>
 * <pre><code>
 *     enum MyRefs implements Ref {
 *          someConfig,
 *          someOtherConfig
 *     }
 * </code></pre>
 *
 * <p>Or:</p>
 * <pre><code>
 *     Ref someConfigRef = () -&gt; "someConfig"
 * </code></pre>
 */
public interface Ref {

    String name();

}
