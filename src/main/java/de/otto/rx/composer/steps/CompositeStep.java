package de.otto.rx.composer.steps;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * {@inheritDoc}
 *
 * <p>
 *     A Step that consists of single delegate Step and a list of nested Steps that are used to continue
 *     retrieving content based on the results of the initial Step.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <p>
 *     First fetch {@link Content} from System X. Extract {@link Parameters} from this Content and proceed
 *     with these Parameters by calling the nested Steps.
 * </p>
 */
class CompositeStep implements Step {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeStep.class);

    static class StepContinuation {
        final ImmutableList<Step> nested;
        final Function<Content,Parameters> paramExtractor;

        StepContinuation(final Function<Content, Parameters> paramExtractor,
                         final ImmutableList<Step> nested) {
            this.nested = nested;
            this.paramExtractor = paramExtractor;
        }
    }

    /** The initial Step. */
    private final Step first;
    /** The StepContinuation that is executed using the results from the first Step. */
    private final StepContinuation continuation;

    /**
     * Creates a CompositeStep from a first Step and a StepContinuation.
     * @param first the fist / initial Step to execute
     * @param continuation Function to extract Parameters from the first Step plus list of nested Steps.
     */
    CompositeStep(final Step first,
                  final StepContinuation continuation) {
        this.first = first;
        this.continuation = continuation;
    }

    @Override
    public Observable<Content> execute(final Parameters parameters) {
        return first
                .execute(parameters)
                .flatMap(content -> {
                    final Parameters nestedParams = parameters.with(continuation.paramExtractor.apply(content));
                    final List<Observable<Content>> observables = this.continuation.nested
                            .stream()
                            .map(step -> step
                                    .execute(nestedParams)
                                    .doOnError((t) -> LOG.error(t.getMessage(), t))
                            )
                            .collect(toList());
                    // Add the content, so we can retrieve the content from the first step:
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
