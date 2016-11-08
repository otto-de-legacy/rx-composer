package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import rx.Observable;

import static com.google.common.collect.ImmutableList.builder;

/**
 * A plan containing the steps to retrieve content from one or more microservices.
 */
public final class Plan {

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

    public ImmutableList<Step> getSteps() {
        return steps;
    }

    public Contents execute(final Parameters params) {
        return  Observable.from(getSteps())
                .flatMap((step) -> step.execute(params))
                .doOnCompleted(() -> System.out.println("Completed at " + System.currentTimeMillis()))
                .doOnNext((c) -> System.out.println("Got Content for " + c.getContentPosition()))
                .doOnError(System.out::println)
                .collect(Contents::new, (contents,content) -> {
                    System.out.println("Collecting content " + content.getContentPosition());
                    contents.add(content);
                })
                .toBlocking()
                .single();
    }

}
