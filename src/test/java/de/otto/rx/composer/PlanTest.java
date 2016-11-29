package de.otto.rx.composer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import de.otto.rx.composer.steps.Step;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.Plan.planIsTo;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlanTest {

    @Test
    public void shouldCreatePlanWithSomeSteps() {
        // given
        final Step doFirst = mock(Step.class);
        final Step doSecond = mock(Step.class);
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
        final Step doFirst = mock(Step.class);
        final Step doSecond = mock(Step.class);
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
    public void shouldReturnContents() {
        // given
        final Step doFirst = mock(Step.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.execute(emptyParameters())).thenReturn(Observable.just(someContent("test", X, "Foo")));
        // and
        final Plan plan = planIsTo(
                doFirst
        );
        // when
        final Contents contents = plan.execute(emptyParameters());
        // then
        assertThat(contents.getBody(X), is("Foo"));
    }

    @Test
    public void shouldIgnoreStepsWithEmptyResults() {
        // given
        final Step doFirst = mock(Step.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.execute(emptyParameters())).thenReturn(Observable.empty());
        // and
        final Plan plan = planIsTo(
                doFirst
        );
        // when
        final Contents contents = plan.execute(emptyParameters());
        // then
        assertThat(contents.get(X).isAvailable(), is(false));
    }

    @Test
    public void shouldExecutePlanMultipleTimes() {
        // given
        final Step doFirst = mock(Step.class);
        final Step doSecond = mock(Step.class);
        final Plan plan = planIsTo(
                        doFirst, doSecond
        );
        // when
        plan.execute(emptyParameters());
        plan.execute(emptyParameters());
        // then
        verify(doFirst, times(2)).execute(any());
        verify(doSecond, times(2)).execute(any());
    }

    @Test
    public void shouldForwardParameters() {
        // given
        final Parameters someParameters = parameters(of(
                "message", "World"
        ));
        final Step doFirst = mock(Step.class);
        final Step doSecond = mock(Step.class);
        final Plan plan = planIsTo(
                doFirst, doSecond
        );
        // when
        plan.execute(someParameters);
        // then
        verify(doFirst).execute(someParameters);
        verify(doSecond).execute(someParameters);
    }

    private Content someContent(final String source, final Position position, final String text) {
        return new TestContent(source, position, text);
    }

    private static final class TestContent extends SingleContent {
        private final String source;
        private final String text;
        private final Position position;

        TestContent(final String source,
                    final Position position,
                    final String text) {
            this.source = source;
            this.position = position;
            this.text = text;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public Position getPosition() {
            return position;
        }

        @Override
        public boolean isAvailable() {
            return !text.isEmpty();
        }

        @Override
        public String getBody() {
            return text;
        }

        @Override
        public Headers getHeaders() {
            return emptyHeaders();
        }

        @Override
        public LocalDateTime getCreated() {
            return now();
        }

    }
}