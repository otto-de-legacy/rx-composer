package de.otto.rx.composer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.steps.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableList.builder;
import static rx.Observable.from;

/**
 * A plan containing the steps to retrieve content from one or more microservices.
 */
public final class Plan {

    private static final Logger LOG = LoggerFactory.getLogger(Plan.class);

    private final ImmutableList<Step> steps;

    private Plan(ImmutableList<Step> steps) {
        this.steps = steps;
    }

    public static Plan planIsTo(final Step firstStep, final Step... moreSteps) {
        final Builder<Step> steps = builder();
        steps.add(firstStep);
        if (moreSteps != null) {
            steps.add(moreSteps);
        }
        return new Plan(steps.build());
    }

    public Contents execute(final Parameters params) {
        LOG.trace("Started execution");
        return from(getSteps())
                .flatMap((step) -> step.execute(params))
                .doOnCompleted(() -> LOG.trace("Completed execution"))
                .doOnNext((c) -> LOG.trace("Got Content for {}", c.getPosition()))
                .doOnError((t) -> LOG.error(t.getMessage(), t))
                .collect(Contents::new, Contents::add)
                .toBlocking()
                .single();
    }

    ImmutableList<Step> getSteps() {
        return steps;
    }

}
