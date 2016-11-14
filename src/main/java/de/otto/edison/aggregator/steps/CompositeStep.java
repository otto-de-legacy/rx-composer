package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static rx.Observable.just;
import static rx.Observable.merge;

/**
 * A single fetch in a Plan to retrieve content.
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


    private final Step first;

    private final StepContinuation continuation;

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
