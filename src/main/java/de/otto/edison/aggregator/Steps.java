package de.otto.edison.aggregator;

public final class Steps {

    private Steps() {}

    public static Step fetch(final Position position, final ContentProvider contentProvider)   {
        return new Step(position, contentProvider);
    }


}
