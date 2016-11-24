package de.otto.rx.composer.steps;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.providers.ContentProvider;

import java.util.function.Function;

import static de.otto.rx.composer.steps.CompositeStep.StepContinuation;
import static java.util.Arrays.asList;

public final class Steps {

    private Steps() {}

    /**
     * Create a {@link SingleStep} for the specified Position and use the ContentProvider to
     * fetch the Content.
     *
     * @param position the Position inside the Plan
     * @param contentProvider the ContentProvider used to fetch the Content.
     * @return Step
     */
    public static Step forPos(final Position position, final ContentProvider contentProvider)   {
        return new SingleStep(position, contentProvider);
    }

    /**
     * Create a {@link CompositeStep} used to fetch Content for the specified Position. Depending on
     * this Content, use the StepContinuation to fetch one or more followup Contents.
     *
     * @param position the Position of the initial Content.
     * @param contentProvider the ContentProvider used to fetch the Content for the given Position.
     * @param then the continuation that is fetching more Content depending on the results of the initial Step.
     * @return Step
     */
    public static Step forPos(final Position position,
                              final ContentProvider contentProvider,
                              final StepContinuation then)   {
        if (then == null || then.nested.isEmpty()) {
            throw new IllegalArgumentException("StepContinuation provided by param 'then' must not be null or empty.");
        } else {
            return new CompositeStep(forPos(position, contentProvider), then);
        }
    }

    /**
     * Create a StepContinuation that is used to specify the followup Steps for a CompositeStep.
     * <p>
     *     This method can be used to create the StepContinuation for {@link Steps#forPos(Position, ContentProvider, StepContinuation)}
     * </p>
     * @param parameterExtractor a Function used to extract additional Parameters from a Content item.
     * @param first the first Step of the Continuation.
     * @param more optionally more Steps of the Continuation.
     * @return StepContinuation
     */
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
