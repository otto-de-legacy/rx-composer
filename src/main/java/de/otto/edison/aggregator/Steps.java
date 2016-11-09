package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;

public final class Steps {

    private Steps() {}

    public static Step fetch(final Position position, final ContentProvider contentProvider)   {
        return new SingleStep(position, contentProvider);
    }

    /**
     * Fetch the content from the quickest ContentProvider.
     * <p>
     *     This method can be used to implement a "Fan Out and Quickest Wins" pattern, if there
     *     are multiple possible providers and 'best performance' is most important.
     * </p>
     * @param position the content position
     * @param contentProviders the ContentProviders.
     * @return Step
     */
    public static Step fetchQuickest(final Position position,
                                     final ImmutableList<ContentProvider> contentProviders) {
        return new QuickestWithContentStep(position, contentProviders);
    }

    public static Step fetchFirstWithContent(final Position position,
                                             final ImmutableList<ContentProvider> steps) {
        return new FirstWithContentStep(position, steps);
    }

}
