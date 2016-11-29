package de.otto.rx.composer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.otto.rx.composer.content.Content;
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
 * A plan describes how to gather {@link de.otto.rx.composer.content.Content} from one or more
 * {@link de.otto.rx.composer.providers.ContentProvider}s.
 * <p>
 *     The plan consists of several {@link Step steps} that describe how to get content for the different
 *     {@link de.otto.rx.composer.content.Position positions} of the Plan.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <pre><code>
 *     final Plan plan = planIsTo(
 *          forPos(
 *              X,
 *              fetchViaHttpGet(httpClient, "http://example.com/someContent", TEXT_PLAIN_TYPE),
 *              then(
 *                  (final Content content) -&gt; parameters(ImmutableMap.of("param", content.getBody()),
 *                  forPos(
 *                      Y,
 *                      fetchViaHttpGet(httpClient, "http://example.com/otherContent{?param}"), TEXT_HTML_TYPE)),
 *                  forPos(
 *                      Z,
 *                      fetchViaHttpGet(httpClient, "http://example.com/moreContent{?param}", TEXT_HTML_TYPE))
 *              )
 *          )
 *     );
 *
 *     final Contents contents = plan.execute(emptyParameters());
 * </code></pre>
 * <p>
 *     During execution, content is retrieved asynchronously, whenever possible. In the example above, the first step
 *     is to fetch plain text content for position X. The returned text is then provided as a {@link Parameters parameter}
 *     to fetch content for Y and Z.
 * </p>
 * <p>
 *     The ContentProviders for Y and Z are executed concurrently.
 * </p>
 *
 */
public final class Plan {

    private static final Logger LOG = LoggerFactory.getLogger(Plan.class);

    private final ImmutableList<Step> steps;

    private Plan(final ImmutableList<Step> steps) {
        this.steps = steps;
    }

    /**
     * Create a Plan using one ore more {@link Step}s.
     * <p>
     *     Steps can be created using the factory methods of {@link de.otto.rx.composer.steps.Steps}.
     * </p>
     * <p>
     *     The returned Plan <em>MUST NOT</em> not be executed concurrently. You should build a new Plan for every request,
     *     otherwise you will see unpredictable results.
     * </p>
     * @param firstStep the first step to execute
     * @param moreSteps optionally more steps
     * @return execution Plan
     */
    public static Plan planIsTo(final Step firstStep, final Step... moreSteps) {
        final Builder<Step> steps = builder();
        steps.add(firstStep);
        if (moreSteps != null) {
            steps.add(moreSteps);
        }
        return new Plan(steps.build());
    }

    /**
     * Executes the steps of the plan concurrently and returns the {@link Content#isAvailable() available} {@link Contents}.
     * @param params Parameters used to fetch the content
     * @return available Contents
     */
    public Contents execute(final Parameters params) {
        LOG.trace("Started execution");
        // use a latch to await execution of all steps:
        final CountDownLatch latch = new CountDownLatch(1);
        final Contents.Builder contents = contentsBuilder();
        from(steps)
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
