package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static de.otto.edison.aggregator.AbcPosition.X;
import static de.otto.edison.aggregator.AbcPosition.Y;
import static de.otto.edison.aggregator.Parameters.emptyParameters;
import static de.otto.edison.aggregator.Parameters.parameters;
import static de.otto.edison.aggregator.Plan.planIsTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlanTest {

    @Test
    public void shouldCreatePlanWithSomeSteps() {
        // given
        final Step doFirst = mock(SingleStep.class);
        final Step doSecond = mock(SingleStep.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doSecond.getPosition()).thenReturn(Y);
        final Plan plan = planIsTo(
                doFirst, doSecond
        );
        // when
        final ImmutableList<Step> steps = plan.getSteps();
        // then
        assertThat(steps, hasSize(2));
        assertThat(steps.get(0).getPosition(), is(X));
        assertThat(steps.get(1).getPosition(), is(Y));
    }

    @Test
    public void shouldExecuteSteps() {
        // given
        final Step doFirst = mock(SingleStep.class);
        final Step doSecond = mock(SingleStep.class);
        final Plan plan = planIsTo(
                        doFirst, doSecond
        );
        // when
        plan.execute(emptyParameters());
        // then
        verify(doFirst).execute(any());
        verify(doSecond).execute(any());
    }

    @Test
    public void shouldForwardParameters() {
        // given
        final Parameters someParameters = parameters(of(
                "message", "World"
        ));
        final Step doFirst = mock(SingleStep.class);
        final Step doSecond = mock(SingleStep.class);
        final Plan plan = planIsTo(
                doFirst, doSecond
        );
        // when
        plan.execute(someParameters);
        // then
        verify(doFirst).execute(someParameters);
        verify(doSecond).execute(someParameters);
    }

}