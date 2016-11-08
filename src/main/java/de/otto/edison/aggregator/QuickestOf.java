package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;
import rx.Observable;

import static java.util.stream.Collectors.toList;
import static rx.Observable.amb;

/**
 * A single fetch in a Plan to retrieve content.
 */
class QuickestOf implements Step {

    private final ImmutableList<ContentProvider> contentProviders;
    private final Position position;

    QuickestOf(final Position position,
               final ImmutableList<ContentProvider> contentProviders) {
        this.position = position;
        this.contentProviders = contentProviders;
    }

    @Override
    public Observable<? extends Content> execute(final Parameters parameters) {
        return amb(contentProviders
                .stream()
                .map((cp) -> cp.getContent(position, parameters))
                .collect(toList()));
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
