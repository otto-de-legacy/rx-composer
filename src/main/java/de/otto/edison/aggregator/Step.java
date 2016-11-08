package de.otto.edison.aggregator;

import rx.Observable;

public interface Step {

    Observable<? extends Content> execute(Parameters parameters);

    Position getPosition();
}
