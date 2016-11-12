package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.*;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.Content.Availability.AVAILABLE;
import static de.otto.edison.aggregator.content.Content.Availability.EMPTY;
import static de.otto.edison.aggregator.content.Headers.emptyHeaders;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.steps.Steps.fetchQuickest;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class QuickestWithContentStepTest {

    @Test
    public void shouldHandleExceptions() {
        // given
        final Step step = fetchQuickest(X, ImmutableList.of(
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