package de.otto.rx.composer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.steps.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static com.google.common.collect.ImmutableList.builder;
import static de.otto.rx.composer.content.Contents.contentsBuilder;
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
        // use a latch to await execution of all steps:
        final CountDownLatch latch = new CountDownLatch(1);
        final Contents.Builder contents = contentsBuilder();
        from(getSteps())
                .flatMap((step) -> step.execute(params))
                .subscribe(
                        (c) -> {
                            LOG.trace("Got Content for {}", c.getPosition());
                            contents.add(c);
                        },
                        (t) -> {
                            LOG.error(t.getMessage(), t);
                        },
                        () -> {
                            LOG.trace("Completed execution");
                            // doOnComplete: all contents are collected - we can proceed now.
                            latch.countDown();
                        }
                );
        try {
            // wait for completion of the plan execution:
            latch.await();
        } catch (final InterruptedException e) {
            LOG.error("Interrupted waiting for Contents: {}", e.getMessage());
        }
        return contents.build();
    }

    ImmutableList<Step> getSteps() {
        return steps;
    }

}
