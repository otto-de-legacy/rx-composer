package de.otto.edison.aggregator;

import rx.Observable;

public interface ContentProvider {

    default Observable<? extends Content> getContent(final Position position, final Parameters parameters) {
        return getContent(position, 0, parameters);
    }

    Observable<? extends Content> getContent(final Position position, final int index, final Parameters parameters);
}
