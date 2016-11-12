package de.otto.edison.aggregator.providers;

import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import rx.Observable;

public interface ContentProvider {

    default Observable<Content> getContent(final Position position, final Parameters parameters) {
        return getContent(position, 0, parameters);
    }

    Observable<Content> getContent(final Position position, final int index, final Parameters parameters);
}
