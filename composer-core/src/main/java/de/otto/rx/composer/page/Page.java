package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.tracer.Stats;
import de.otto.rx.composer.tracer.Tracer;
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
 *     The plan consists of several {@link Fragment fragments} that describe how to get content for the different
 *     {@link de.otto.rx.composer.content.Position positions} of the Plan.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <pre><code>
 *     final Plan plan = consistsOf(
 *          fragment(
 *              X,
 *              contentFrom(httpClient, "http://example.com/someContent", TEXT_PLAIN_TYPE),
 *              followedBy(
 *                  (final Content content) -&gt; parameters(ImmutableMap.of("param", content.getBody()),
 *                  fragment(
 *                      Y,
 *                      contentFrom(httpClient, "http://example.com/otherContent{?param}"), TEXT_HTML_TYPE)),
 *                  fragment(
 *                      Z,
 *                      contentFrom(httpClient, "http://example.com/moreContent{?param}", TEXT_HTML_TYPE))
 *              )
 *          )
 *     );
 *
 *     final Contents contents = plan.fetchWith(emptyParameters());
 * </code></pre>
 * <p>
 *     During execution, content is retrieved asynchronously, whenever possible. In the example above, the first fragment
 *     is to fetch plain text content for position X. The returned text is followedBy provided as a {@link Parameters parameter}
 *     to fetch content for Y and Z.
 * </p>
 * <p>
 *     The ContentProviders for Y and Z are executed concurrently.
 * </p>
 *
 */
public final class Page {

    private static final Logger LOG = LoggerFactory.getLogger(Page.class);

    private final ImmutableList<Fragment> fragments;

    private Page(final ImmutableList<Fragment> fragments) {
        this.fragments = fragments;
    }

    /**
     * Create a Plan using one ore more {@link Fragment}s.
     * <p>
     *     Fragments can be created using the factory methods of {@link Fragments}.
     * </p>
     * <p>
     *     The returned Plan <em>MUST NOT</em> not be executed concurrently. You should build a new Plan for every request,
     *     otherwise you will see unpredictable results.
     * </p>
     * @param firstFragment the first fragment to fetchWith
     * @param moreFragments optionally more fragments
     * @return execution Plan
     */
    public static Page consistsOf(final Fragment firstFragment, final Fragment... moreFragments) {
        final Builder<Fragment> fragments = builder();
        fragments.add(firstFragment);
        if (moreFragments != null) {
            fragments.add(moreFragments);
        }
        return new Page(fragments.build());
    }

    /**
     * Executes the fragments of the plan concurrently and returns the {@link Content#isAvailable() available} {@link Contents}.
     * @param params Parameters used to fetch the content
     * @return available Contents
     */
    public Contents fetchWith(final Parameters params) {
        final Tracer tracer = new Tracer();
        // use a latch to await execution of all fragments:
        final CountDownLatch latch = new CountDownLatch(1);
        final Contents.Builder contents = contentsBuilder();
        from(fragments)
                .flatMap((fragment) -> fragment.fetchWith(tracer, params))
                .subscribe(
                        contents::add,
                        (t) -> LOG.error(t.getMessage(), t),
                        latch::countDown
                );
        try {
            // wait for completion of the plan execution:
            latch.await();
        } catch (final InterruptedException e) {
            LOG.error("Interrupted waiting for Contents: {}", e.getMessage());
        }
        final Stats statistics = tracer.gatherStatistics();
        LOG.info(statistics.toString());
        return contents
                .setStats(statistics)
                .build();
    }

    ImmutableList<Fragment> getFragments() {
        return fragments;
    }

}
