package de.otto.rx.composer.content;


import de.otto.rx.composer.content.Content.Availability;
import org.junit.Test;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.EMPTY;
import static de.otto.rx.composer.content.Content.Availability.ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
        assertThat(contents.hasErrors(), is(false));
        assertThat(contents.getContents(), hasSize(1));
        assertThat(contents.getContent(A).isPresent(), is(true));
        assertThat(contents.getEmpty(), is(empty()));
        assertThat(contents.getErrors(), is(empty()));
    }

    @Test
    public void shouldAddEmptyContent() {
        // given
        final Contents contents = new Contents();
        // when
        contents.add(someContent("", EMPTY));
        // then
        assertThat(contents.hasErrors(), is(false));
        assertThat(contents.getContents(), is(empty()));
        assertThat(contents.getContent(A).isPresent(), is(false));
        assertThat(contents.getEmpty(), contains(A));
        assertThat(contents.getErrors(), is(empty()));
    }

    @Test
    public void shouldAddErrorContent() {
        // given
        final Contents contents = new Contents();
        // when
        contents.add(someContent("", ERROR));
        // then
        assertThat(contents.hasErrors(), is(true));
        assertThat(contents.getContents(), is(empty()));
        assertThat(contents.getContent(A).isPresent(), is(false));
        assertThat(contents.getEmpty(), is(empty()));
        assertThat(contents.getErrors(), contains(A));
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