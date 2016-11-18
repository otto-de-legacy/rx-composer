package de.otto.rx.composer.steps;

import de.otto.rx.composer.Plan;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import rx.Observable;

/**
 * A Step that is providing an Observable for Content for a single {@link Position}.
 * <p>
 *     Steps are part of a {@link Plan}. They can be created using class {@link Steps}.
 * </p>
 */
public interface Step {

    /**
     * Execute the Step and get the Observable Content.
     *
     * @param parameters parameters provided when executing the {@link Plan}
     * @return Observable content for the Step's position.
     */
    Observable<Content> execute(Parameters parameters);

    /**
     *
     * @return the Position of the content provided when this Step is executed.
     */
    Position getPosition();
}
