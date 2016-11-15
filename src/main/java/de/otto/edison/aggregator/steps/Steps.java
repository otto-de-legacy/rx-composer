package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;

import java.util.function.Function;

import static de.otto.edison.aggregator.steps.CompositeStep.StepContinuation;
import static java.util.Arrays.asList;

public final class Steps {

    private Steps() {}


    public static Step forPos(final Position position, final ContentProvider contentProvider)   {
        return new SingleStep(position, contentProvider);
    }

    public static Step forPos(final Position position,
                              final ContentProvider contentProvider,
                              final StepContinuation then)   {
        if (then.nested.isEmpty()) {
            return new SingleStep(position, contentProvider);
        } else {
            return new CompositeStep(forPos(position, contentProvider), then);
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

}
