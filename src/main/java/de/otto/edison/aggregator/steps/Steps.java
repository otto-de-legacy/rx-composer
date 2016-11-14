package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;

import java.util.function.Function;

import static de.otto.edison.aggregator.steps.CompositeStep.StepContinuation;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;

public final class Steps {

    private Steps() {}


    public static Step fetch(final Position position, final ContentProvider contentProvider)   {
        return new SingleStep(position, contentProvider);
    }

    public static Step fetch(final Position position, final ContentProvider contentProvider, final StepContinuation then)   {
        if (then.nested.isEmpty()) {
            return new SingleStep(position, contentProvider);
        } else {
            return new CompositeStep(fetch(position, contentProvider), then);
        }
    }

    public static StepContinuation then(final Function<Content,Parameters> parameterExtractor,
                                        final Step first,
                                        final Step... more) {
        final ImmutableList.Builder<Step> builder = ImmutableList.<Step>builder().add(first);
        if (more != null) {
            builder.addAll(asList(more));
        }
        return new StepContinuation(parameterExtractor, builder.build());
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

    public static Step fetchFirst(final Position position,
                                  final ImmutableList<ContentProvider> contentProviders) {
        return new FetchOneOfManyStep(position, contentProviders, Content::hasContent, comparingInt(Content::getIndex));
    }

}
