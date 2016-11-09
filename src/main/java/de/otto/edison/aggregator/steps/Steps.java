package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;

import static java.util.Comparator.comparingInt;

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
        return new FetchOneOfManyStep(position, steps, Content::hasContent, comparingInt(Content::getIndex));
    }

}