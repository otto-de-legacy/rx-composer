package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.AbcPosition;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Headers;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.steps.Step;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.Iterator;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.EMPTY;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchFirst;
import static de.otto.rx.composer.steps.Steps.forPos;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class FetchOneOfManyContentProviderTest {

    @Test
    public void shouldSetPosition() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "Foo")),
                (position, parameters) -> just(new TestContent(X, "Bar"))
        )));
        // then
        assertThat(step.getPosition(), is(X));
    }

    @Test
    public void shouldExecuteStep() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "Foo")),
                (position, parameters) -> just(new TestContent(X, "Bar"))
        )));
        // when
        final Observable<Content> result =  step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldExecuteStepMultipleTimes() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "Foo")),
                (position, parameters) -> just(new TestContent(X, "Bar"))
        )));
        // when
        step.execute(emptyParameters());
        step.execute(emptyParameters());
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldSelectFirstNotEmpty() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "")),
                (position, parameters) -> just(new TestContent(X, "Hello World"))
        )));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().toIterable().iterator();
        assertThat(content.next().getBody(), is("Hello World"));
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleEmptyContents() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
                (position, parameters) -> just(new TestContent(X, "")),
                (position, parameters) -> just(new TestContent(X, ""))
        )));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().getIterator();
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Step step = forPos(X, fetchFirst(ImmutableList.of(
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