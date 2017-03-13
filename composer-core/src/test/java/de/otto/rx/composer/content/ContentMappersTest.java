package de.otto.rx.composer.content;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentMappersTest {

    @Test
    public void shouldExtractBody() {
        // given
        final Content content = mock(Content.class);
        when(content.getBody()).thenReturn("<body>Test</body>");

        // when
        final Content mappedContent = ContentMappers.htmlBody(content);

        // then
        assertThat(mappedContent.getBody(), is("Test"));
    }

    @Test
    public void shouldIgnoreAttributes() {
        // given
        final Content content = mock(Content.class);
        when(content.getBody()).thenReturn("<!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>Test</h1></body></html>");

        // when
        final Content mappedContent = ContentMappers.htmlBody(content);

        // then
        assertThat(mappedContent.getBody(), is("<h1>Test</h1>"));
    }

    @Test
    public void shouldConcatenateMultipleBodesForCompositeContents() {
        // given
        final Content content = mock(Content.class);
        when(content.getBody()).thenReturn(
                "<!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>Test</h1></body>" +
                "</html><!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>Test</h1></body></html>");

        // when
        final Content mappedContent = ContentMappers.htmlBody(content);

        // then
        assertThat(mappedContent.getBody(), is("<h1>Test</h1><h1>Test</h1>"));
    }
}