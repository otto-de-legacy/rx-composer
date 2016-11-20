package de.otto.rx.composer.steps;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Headers;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.providers.ContentProvider;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableList.copyOf;
import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.B;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.steps.Steps.forPos;
import static de.otto.rx.composer.steps.Steps.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class CompositeStepTest {

    @Test
    public void shouldExecuteInitialContentProvider() {
        // given
        final ContentProvider initial = mock(ContentProvider.class);
        when(initial.getContent(A, emptyParameters())).thenReturn(just(mock(Content.class)));
        final Step step = forPos(A, initial, then((c) -> emptyParameters(), mock(Step.class)));
        // when
        step.execute(emptyParameters());
        // then
        verify(initial).getContent(A, emptyParameters());
    }

    @Test
    public void shouldExecuteNestedStep() {
        // given
        final ContentProvider fetchInitial = mock(ContentProvider.class);
        when(fetchInitial.getContent(A, emptyParameters())).thenReturn(just(someContent(A)));
        // and
        final Step nestedStep = mock(Step.class);
        when(nestedStep.getPosition()).thenReturn(B);
        when(nestedStep.execute(emptyParameters())).thenReturn(just(someContent(B)));
        // and
        final Step compositeStep = forPos(A, fetchInitial, then((c) -> emptyParameters(), nestedStep));
        // when
        ImmutableList<Content> contents = copyOf(compositeStep.execute(emptyParameters()).toBlocking().toIterable());

        // then
        assertThat(contents, hasSize(2));
        verify(nestedStep).execute(emptyParameters());
    }

    private Content someContent(final Position position) {
        return new Content() {
            @Override
            public String getSource() {
                return position.name();
            }

            @Override
            public Position getPosition() {
                return position;
            }

            @Override
            public boolean hasContent() {
                return true;
            }

            @Override
            public String getBody() {
                return "Yes!";
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
                return Availability.AVAILABLE;
            }
        };

    }

}