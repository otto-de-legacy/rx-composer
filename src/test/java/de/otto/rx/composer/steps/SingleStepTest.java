package de.otto.rx.composer.steps;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Headers;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.providers.ContentProvider;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.ERROR;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.steps.Steps.forPos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static rx.Observable.just;

public class SingleStepTest {

    @Test
    public void shouldBuildSingleStepUsingForPos() {
        final Step step = forPos(X, mock(ContentProvider.class));
        assertThat(step, is(instanceOf(SingleStep.class)));
    }

    @Test
    public void shouldFetchContent() {
        // given
        final Step step = forPos(X, (position, parameters) -> just(someContent("Yeah!")));
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
        final Step step = forPos(X, (position, parameters) -> {throw new IllegalStateException("Bumm!!!");});
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
            public String getSource() {
                return body;
            }

            @Override
            public Position getPosition() {
                return X;
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