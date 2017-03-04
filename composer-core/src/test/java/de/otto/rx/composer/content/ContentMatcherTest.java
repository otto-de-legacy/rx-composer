package de.otto.rx.composer.content;

import org.junit.Test;

import static de.otto.rx.composer.content.ContentMatcher.contentMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class ContentMatcherTest {

    @Test
    public void shouldMatchContent() {
        ContentMatcher contentMatcher = contentMatcher((c) -> true, "");
        assertThat(contentMatcher.test(mock(Content.class)), is(true));
    }

    @Test
    public void shouldFailToMatch() {
        ContentMatcher contentMatcher = contentMatcher((c) -> false, "");
        assertThat(contentMatcher.test(mock(Content.class)), is(false));
    }

    @Test
    public void shouldProvideMismatchInfo() {
        ContentMatcher contentMatcher = contentMatcher((c) -> true, "Some Info");
        assertThat(contentMatcher.getMismatchDescription(), is("Some Info"));
    }

    @Test
    public void shouldProvideDefaultMismatchInfo() {
        ContentMatcher contentMatcher = contentMatcher((c) -> true, null);
        assertThat(contentMatcher.getMismatchDescription(), is("(no description)"));
    }
}