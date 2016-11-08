package de.otto.edison.aggregator;

import rx.Observable;

public interface ContentProvider {

    Observable<? extends Content> getContent(final Position position, final Parameters parameters);
}
