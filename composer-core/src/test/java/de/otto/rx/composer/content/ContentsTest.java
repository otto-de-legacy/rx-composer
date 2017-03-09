package de.otto.rx.composer.content;


import org.junit.Test;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Contents.contentsBuilder;
import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static de.otto.rx.composer.content.StaticTextContent.staticTextContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ContentsTest {

    @Test
    public void shouldAddAvailableContent() {
        // given
        final Contents.Builder builder = contentsBuilder();
        // when
        final Contents contents = builder.add(someContent("some content")).build();
        // then
        assertThat(contents.getAll(), hasSize(1));
        assertThat(contents.get(A).isAvailable(), is(true));
    }

    @Test
    public void shouldAddEmptyContent() {
        // given
        final Contents.Builder builder = contentsBuilder();
        // when
        final Contents contents = builder.add(someContent("")).build();
        // then
        assertThat(contents.getAll(), is(empty()));
        assertThat(contents.get(A).isAvailable(), is(false));
    }

    @Test
    public void shouldGetContentByPosition() {
        // given
        final Contents builder = contentsBuilder()
                .add(someContent("some content"))
                .build();
        // when
        final Content content = builder.get(A);
        // then
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("some content"));
    }

    @Test
    public void shouldGetContentByOtherPositionWithSameName() {
        // given
        final Contents builder = contentsBuilder()
                .add(someContent("some content"))
                .build();
        // when
        final Content content = builder.get(() -> "A");
        // then
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("some content"));
    }

    @Test
    public void shouldGetEmptyContentAsFallback() {
        // given
        final Contents.Builder builder = contentsBuilder();
        // when
        final Content content = builder.build().get(A);
        // then
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));
    }

    @Test
    public void shouldGetEmptyContentOnError() {
        // given
        final Contents.Builder builder = contentsBuilder();
        builder.add(errorContent(A, new IllegalStateException("test"), 0L));
        // when
        final Content content = builder.build().get(A);
        // then
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));

    }

    private Content someContent(final String body) {
        return staticTextContent(
                body, A, body
        );
    }
}