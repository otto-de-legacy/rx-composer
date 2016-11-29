package de.otto.rx.composer.content;

/**
 * A position in a plan.
 * <p>
 *     Example usage:
 *     <pre><code>
 *         public enum MyPosition implements Position { HEADER, MAIN, FOOTER };
 **     </code></pre>
 * </p>
 */
public interface Position {

    public String name();

}
