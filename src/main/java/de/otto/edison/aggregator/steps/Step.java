package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import rx.Observable;

public interface Step {

    Observable<Content> execute(Parameters parameters);

    Position getPosition();
}
