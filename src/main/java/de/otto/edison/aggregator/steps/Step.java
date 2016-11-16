package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.Plan;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
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
