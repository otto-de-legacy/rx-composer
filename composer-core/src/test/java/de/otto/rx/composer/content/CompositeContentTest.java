package de.otto.rx.composer.content;


import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.B;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeContentTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateCompositeWithOneContent() {
        someCompositeContent(emptyHeaders(), someContent(A));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateCompositeWithContentForDifferentPositions() {
        someCompositeContent(emptyHeaders(), someContent(A), someContent(A), someContent(B));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToCreateCompositeWithNullHeaders() {
        someCompositeContent(null, someContent(A), someContent(A));
    }

    @Test
    public void shouldAppendSources() {
        final Content content = someCompositeContent(emptyHeaders(), someContent("First Source"), someContent("Second Source"));
        assertThat(content.getSource(), is("First Source,Second Source"));
    }

    @Test
    public void shouldAppendBodies() {
        final Content content = someCompositeContent(emptyHeaders(), someContent("First Body"), someContent("Second Body"));
        assertThat(content.getBody(), is("First Body\nSecond Body"));
    }

    @Test
    public void shouldConvertToCompositeContent() {
        final Content content = someCompositeContent(emptyHeaders(), someContent(A), someContent(A));
        assertThat(content.isComposite(), is(true));
        assertThat(content.asComposite(), is(notNullValue()));
        assertThat(content.asComposite().getContents(), hasSize(2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToConvertToSingleContent() {
        final Content content = someCompositeContent(emptyHeaders(), someContent(A), someContent(A));
        content.asSingle();
    }

    private Content someCompositeContent(final Headers headers, final Content... contents) {
        return CompositeContent.compositeContent(headers, ImmutableList.copyOf(contents));
    }

    private Content someContent(final Position position) {
        Content mock = mock(Content.class);
        when(mock.getPosition()).thenReturn(position);
        return mock;
    }

    private Content someContent(final String sourceAndBody) {
        Content mock = mock(Content.class);
        when(mock.getPosition()).thenReturn(A);
        when(mock.getSource()).thenReturn(sourceAndBody);
        when(mock.getBody()).thenReturn(sourceAndBody);
        return mock;
    }

}