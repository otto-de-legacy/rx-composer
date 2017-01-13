package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import rx.Observable;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * {@inheritDoc}
 *
 * <p>
 *     A Fragment that consists of a single delegate Fragment and a list of nested Fragments that are used to continue
 *     retrieving content based on the results of the initial content.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <p>
 *     First fetch {@link Content} from System X. Extract {@link Parameters} from this Content and proceed
 *     with these Parameters by calling the nested Fragments.
 * </p>
 */
class CompositeFragment implements Fragment {

    private static final Logger LOG = getLogger(CompositeFragment.class);

    static class FragmentContinuation {
        final ImmutableList<Fragment> nested;
        final Function<Content,Parameters> paramExtractor;

        FragmentContinuation(final Function<Content, Parameters> paramExtractor,
                             final ImmutableList<Fragment> nested) {
            this.nested = nested;
            this.paramExtractor = paramExtractor;
        }
    }

    /** The initial Fragment. */
    private final Fragment first;
    /** The continuation that is executed using the results from the first Fragment. */
    private final FragmentContinuation continuation;

    /**
     * Creates a CompositeFragment from a first Fragment and a continuation.
     * @param first the fist / initial Fragment to fetch
     * @param continuation Function to extract Parameters from the first Fragment plus list of nested Fragments.
     */
    CompositeFragment(final Fragment first,
                      final FragmentContinuation continuation) {
        this.first = first;
        this.continuation = continuation;
    }

    @Override
    public Observable<Content> fetchWith(final Parameters parameters) {
        return first
                .fetchWith(parameters)
                .flatMap(content -> {
                    final Parameters nestedParams = parameters.with(continuation.paramExtractor.apply(content));
                    final List<Observable<Content>> observables = this.continuation.nested
                            .stream()
                            .map(fragment -> fragment
                                    .fetchWith(nestedParams)
                                    .doOnError((t) -> LOG.error(t.getMessage(), t))
                            )
                            .collect(toList());
                    // Add the content, so we can retrieve the content from the first fragment:
                    observables.add(just(content));
                    return merge(observables)
                            .doOnError((t) -> LOG.error(t.getMessage(), t));
                });
    }

    @Override
    public Position getPosition() {
        return first.getPosition();
    }
}
