package de.otto.rx.composer.providers;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import rx.Observable;

/**
 * ContentProviders are used to fetch Content for a Position in a Plan.
 */
public interface ContentProvider {

    /**
     * Returns an Observable Content for the given Position and Parameters.
     * <p>
     *     The returned Observable must be {@link rx.Observable#empty() empty}, if no Content is
     *     {@link de.otto.rx.composer.content.Content.Availability available}.
     * </p>
     * <p>
     *     Only a {@link Observable#single() single} Content is emitted by the Observer, if available.
     * </p>
     * @param position the Position of the observed content inside the {@link de.otto.rx.composer.Plan}.
     * @param parameters Parameters used by the ContentProvider to fetch the Content.
     * @return Observable emitting nothing, or a single Content item.
     */
    Observable<Content> getContent(final Position position, final Parameters parameters);
}
