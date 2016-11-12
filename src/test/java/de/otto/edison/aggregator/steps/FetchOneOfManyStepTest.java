package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.*;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.Iterator;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.Content.Availability.AVAILABLE;
import static de.otto.edison.aggregator.content.Content.Availability.EMPTY;
import static de.otto.edison.aggregator.content.Headers.emptyHeaders;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.steps.Steps.fetchFirst;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class FetchOneOfManyStepTest {

    @Test
    public void shouldSetPosition() {
        // given
        final Step step = fetchFirst(X, ImmutableList.of(
                (position, index, parameters) -> just(new TestContent(X, "Foo")),
                (position, index, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // then
        assertThat(step.getPosition(), is(X));
    }

    @Test
    public void shouldExecuteStep() {
        // given
        final Step step = fetchFirst(X, ImmutableList.of(
                (position, index, parameters) -> just(new TestContent(X, "Foo")),
                (position, index, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldExecuteStepMultipleTimes() {
        // given
        final Step step = fetchFirst(X, ImmutableList.of(
                (position, index, parameters) -> just(new TestContent(X, "Foo")),
                (position, index, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        step.execute(emptyParameters());
        step.execute(emptyParameters());
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldHandleEmptyContents() {
        // given
        final Step step = fetchFirst(X, ImmutableList.of(
                (position, index, parameters) -> just(new TestContent(X, "")),
                (position, index, parameters) -> just(new TestContent(X, ""))
        ));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().getIterator();
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Step step = fetchFirst(X, ImmutableList.of(
                (position, index, parameters) -> {throw new IllegalStateException("Bumm!!!");},
                (position, index, parameters) -> just(new TestContent(X, "Yeah!"))
        ));
        // when
        final Observable<Content> result = step
                .execute(emptyParameters())
                .onErrorReturn(throwable -> new ErrorContent(X, 0, throwable));
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    private static final class TestContent implements Content {
        private final String text;
        private AbcPosition position;

        public TestContent(final AbcPosition position,
                           final String text) {
            this.position = position;
            this.text = text;
        }

        @Override
        public Position getPosition() {
            return position;
        }

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public boolean hasContent() {
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

        @Override
        public Availability getAvailability() {
            return hasContent() ? AVAILABLE : EMPTY;
        }
    }
}