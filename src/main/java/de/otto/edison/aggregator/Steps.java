package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;

public final class Steps {

    private Steps() {}

    public static Step fetch(final Position position, final ContentProvider contentProvider)   {
        return new SingleStep(position, contentProvider);
    }

    public static Step fetchQuickest(final Position position, final ImmutableList<ContentProvider> steps) {
        return new QuickestOf(position, steps);
    }

}
