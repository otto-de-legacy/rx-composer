package de.otto.edison.aggregator.providers;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.content.*;
import de.otto.edison.aggregator.steps.Step;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.Content.Availability.AVAILABLE;
import static de.otto.edison.aggregator.content.Content.Availability.EMPTY;
import static de.otto.edison.aggregator.content.Headers.emptyHeaders;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.providers.ContentProviders.fetchQuickest;
import static de.otto.edison.aggregator.steps.Steps.forPos;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class QuickestWinsContentProviderTest {

    @Test
    public void shouldReturnOnlyNonEmptyContent() {
        // given
        final Step step = forPos(X, fetchQuickest(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "")),
                (position, parameters) -> just(new TestContent(X, "Yeah!"))
        )));
        // when
        final Observable<Content> result = step
                .execute(emptyParameters())
                .onErrorReturn(throwable -> new ErrorContent(X, throwable));
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Step step = forPos(X, fetchQuickest(ImmutableList.of(
                (position, parameters) -> {throw new IllegalStateException("Bumm!!!");},
                (position, parameters) -> just(new TestContent(X, "Yeah!"))
        )));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    private static final class TestContent implements Content {
        private final String text;
        private AbcPosition position;

        TestContent(final AbcPosition position,
                    final String text) {
            this.position = position;
            this.text = text;
        }

        @Override
        public Position getPosition() {
            return position;
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