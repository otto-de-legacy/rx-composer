package de.otto.rx.composer.page;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import rx.Observable;

/**
 * A Fragment of a Page that is providing Content for a single {@link Position}.
 * <p>
 *     A Fragment is part of a {@link Page} and provides content for a position inside of the page.
 *     They can be created using class {@link Fragments}.
 * </p>
 */
public interface Fragment {

    /**
     * Fetch the observable Content for the Fragment.
     *
     * @param parameters parameters provided when fetching the {@link Page}
     * @return Observable content for the Fragment's position.
     */
    Observable<Content> fetchWith(Parameters parameters);

    /**
     *
     * @return the Position of the content provided when this Fragment's content is fetched.
     */
    Position getPosition();
}
