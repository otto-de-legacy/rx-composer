package de.otto.rx.composer.content;


import de.otto.rx.composer.content.Content.Availability;
import org.junit.Test;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.EMPTY;
import static de.otto.rx.composer.content.Content.Availability.ERROR;
import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ContentsTest {

    @Test
    public void shouldAddAvailableContent() {
        // given
        final Contents contents = new Contents();
        // when
        contents.add(someContent("some content", AVAILABLE));
        // then
        assertThat(contents.getAll(), hasSize(1));
        assertThat(contents.get(A).hasContent(), is(true));
    }

    @Test
    public void shouldAddEmptyContent() {
        // given
        final Contents contents = new Contents();
        // when
        contents.add(someContent("", EMPTY));
        // then
        assertThat(contents.getAll(), is(empty()));
        assertThat(contents.get(A).hasContent(), is(false));
    }

    @Test
    public void shouldGetEmptyContentAsFallback() {
        // given
        final Contents contents = new Contents();
        // when
        final Content content = contents.get(A);
        // then
        assertThat(content.hasContent(), is(false));
        assertThat(content.getBody(), is(""));
    }

    @Test
    public void shouldGetEmptyContentOnError() {
        // given
        final Contents contents = new Contents();
        contents.add(errorContent(A, new IllegalStateException("test")));
        // when
        final Content content = contents.get(A);
        // then
        assertThat(content.hasContent(), is(false));
        assertThat(content.getBody(), is(""));

    }

    @Test
    public void shouldAddErrorContent() {
        // given
        final Contents contents = new Contents();
        // when
        contents.add(someContent("", ERROR));
        // then
        assertThat(contents.getAll(), is(empty()));
        assertThat(contents.get(A).hasContent(), is(false));
    }

    private Content someContent(final String body, final Availability availability) {
        return new Content() {
            @Override
            public String getSource() {
                return body;
            }

            @Override
            public Position getPosition() {
                return A;
            }

            @Override
            public boolean hasContent() {
                return !body.isEmpty();
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public Headers getHeaders() {
                return Headers.of(null);
            }

            @Override
            public LocalDateTime getCreated() {
                return LocalDateTime.now();
            }

            @Override
            public Availability getAvailability() {
                return availability;
            }
        };
    }
}