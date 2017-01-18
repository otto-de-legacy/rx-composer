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
     * @return Fragment
     */
    public static Fragment fragment(final Position position, final ContentProvider contentProvider)   {
        return new SingleFragment(position, contentProvider);
    }

    /**
     * Create a {@link CompositeFragment} used to fetch Content for the specified Position. Depending on
     * this Content, use the FragmentContinuation to fetch one or more followup Contents.
     *
     * @param position the Position of the initial Content.
     * @param contentProvider the ContentProvider used to fetch the Content for the given Position.
     * @param then the continuation that is fetching more Content depending on the results of the initial Fragment.
     * @return Fragment
     */
    public static Fragment fragment(final Position position,
                                    final ContentProvider contentProvider,
                                    final FragmentContinuation then)   {
        if (then == null || then.nested.isEmpty()) {
            throw new IllegalArgumentException("FragmentContinuation provided by param 'followedBy' must not be null or empty.");
        } else {
            return new CompositeFragment(fragment(position, contentProvider), then);
        }
    }

    /**
     * Create a FragmentContinuation that is used to specify the followup Fragments for a CompositeFragment.
     * <p>
     *     This method can be used to create the FragmentContinuation for {@link Fragments#fragment(Position, ContentProvider, FragmentContinuation)}
     * </p>
     * @param parameterExtractor a Function used to extract additional Parameters from a Content item.
     * @param first the first Fragment of the Continuation.
     * @param more optionally more Fragments of the Continuation.
     * @return FragmentContinuation
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
