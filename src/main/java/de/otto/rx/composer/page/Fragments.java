package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.providers.ContentProvider;

import java.util.function.Function;

import static de.otto.rx.composer.page.CompositeFragment.FragmentContinuation;
import static java.util.Arrays.asList;

/**
 * Factory methods used to create {@link Fragment fragments}.
 */
public final class Fragments {

    private Fragments() {}

    /**
     * Create a {@link SingleFragment} for the specified Position and use the ContentProvider to
     * fetch the Content.
     *
     * @param position the Position inside the Plan
     * @param contentProvider the ContentProvider used to fetch the Content.
     * @return Step
     */
    public static Fragment fragment(final Position position, final ContentProvider contentProvider)   {
        return new SingleFragment(position, contentProvider);
    }

    /**
     * Create a {@link CompositeFragment} used to fetch Content for the specified Position. Depending on
     * this Content, use the StepContinuation to fetch one or more followup Contents.
     *
     * @param position the Position of the initial Content.
     * @param contentProvider the ContentProvider used to fetch the Content for the given Position.
     * @param then the continuation that is fetching more Content depending on the results of the initial Step.
     * @return Step
     */
    public static Fragment fragment(final Position position,
                                    final ContentProvider contentProvider,
                                    final FragmentContinuation then)   {
        if (then == null || then.nested.isEmpty()) {
            throw new IllegalArgumentException("StepContinuation provided by param 'followedBy' must not be null or empty.");
        } else {
            return new CompositeFragment(fragment(position, contentProvider), then);
        }
    }

    /**
     * Create a StepContinuation that is used to specify the followup Steps for a CompositeStep.
     * <p>
     *     This method can be used to create the StepContinuation for {@link #fragment(Position, ContentProvider, FragmentContinuation)}
     * </p>
     * @param parameterExtractor a Function used to extract additional Parameters from a Content item.
     * @param first the first Step of the Continuation.
     * @param more optionally more Steps of the Continuation.
     * @return StepContinuation
     */
    public static FragmentContinuation followedBy(final Function<Content,Parameters> parameterExtractor,
                                                  final Fragment first,
                                                  final Fragment... more) {
        final ImmutableList.Builder<Fragment> builder = ImmutableList.<Fragment>builder().add(first);
        if (more != null) {
            builder.addAll(asList(more));
        }
        return new FragmentContinuation(parameterExtractor, builder.build());
    }

}
