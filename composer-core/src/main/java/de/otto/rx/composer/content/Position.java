package de.otto.rx.composer.content;

/**
 * The position of a {@link de.otto.rx.composer.page.Page page} {@link de.otto.rx.composer.page.Fragment fragment}.
 * <p>
 *     Example usage:
 * </p>
 * <pre><code>
 *     public enum MyPosition implements Position { HEADER, MAIN, FOOTER };
 * </code></pre>
 */
public interface Position {

    public String name();

}
