package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Headers;
import de.otto.edison.aggregator.content.Position;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.Content.Availability.AVAILABLE;
import static de.otto.edison.aggregator.content.Content.Availability.ERROR;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.steps.Steps.fetch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class SingleStepTest {

    @Test
    public void shouldFetchContent() {
        // given
        final Step step = fetch(X, (position, index, parameters) -> just(someContent("Yeah!")));
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getAvailability(), is(AVAILABLE));
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Step step = fetch(X, (position, index, parameters) -> {throw new IllegalStateException("Bumm!!!");});
        // when
        final Observable<Content> result = step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getAvailability(), is(ERROR));
        assertThat(content.getBody(), is("Bumm!!!"));
    }

    private Content someContent(final String body) {
        return  new Content() {
            @Override
            public Position getPosition() {
                return X;
            }

            @Override
            public int getIndex() {
                return 0;
            }

            @Override
            public boolean hasContent() {
                return true;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public Headers getHeaders() {
                return Headers.emptyHeaders();
            }

            @Override
            public LocalDateTime getCreated() {
                return LocalDateTime.now();
            }

            @Override
            public Availability getAvailability() {
                return AVAILABLE;
            }
        };
    }


}