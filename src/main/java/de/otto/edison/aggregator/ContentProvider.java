package de.otto.edison.aggregator;

import rx.Observable;

import java.util.function.Function;

public final class ContentProvider {

    private final Function<ContentPosition, Observable<? extends Content>> providerFunc;

    public ContentProvider(final Function<ContentPosition, Observable<? extends Content>> providerFunc) {
        this.providerFunc = providerFunc;
    }

    public Observable<? extends Content> getContent(final ContentPosition contentPosition, final Parameters parameters) {
        return providerFunc.apply(contentPosition);
    }
}
