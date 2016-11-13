package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.otto.edison.aggregator.content.Contents;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.steps.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableList.builder;

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
        LOG.info("Started: " + LocalDateTime.now());
        final Contents result = Observable.from(getSteps())
                .flatMap((step) -> step.execute(params))
                .doOnCompleted(() -> LOG.info("Completed at " + System.currentTimeMillis()))
                .doOnNext((c) -> LOG.info("Got Content for " + c.getPosition()))
                .doOnError((t) -> LOG.info(t.getMessage(), t))
                .collect(Contents::new, (contents, content) -> {
                    LOG.info("Collecting content " + content.getPosition());
                    contents.add(content);
                })
                .toBlocking()
                .single();
        LOG.info("Finished: " + LocalDateTime.now());

        return result;
    }

    ImmutableList<Step> getSteps() {
        return steps;
    }

}
