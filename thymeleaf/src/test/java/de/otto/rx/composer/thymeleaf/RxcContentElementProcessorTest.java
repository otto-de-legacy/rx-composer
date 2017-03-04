package de.otto.rx.composer.thymeleaf;

public class RxcContentElementProcessorTest {

    /*
    @Test
    public void shouldResolveEsiInclude() throws IOException {
        Fetch fetch = s -> withResponse(200, "test");

        //when
        final String html = processor(fetch).fetch("someSrc", "template", CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"someSrc\"> -->test<!-- </esi:include> -->"));
    }

    @Test
    public void shouldHandleHttp404() throws IOException {
        Fetch fetch = fetchWith404();

        //when
        final String html = processor(fetch).fetch("someSrc", "template", CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"someSrc\"> --><!-- </esi:include> -->"));
    }

    @Test
    public void shouldHttp404WithError() throws IOException {
        Fetch fetch = fetchWith404();

        //when
        final String html = processor(fetch).fetch("someSrc", "template", NO_CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"someSrc\"> -->404: some status<!-- </esi:include> -->"));
    }

    @Test
    public void shouldFailOnUnknownStatusCode() throws IOException {
        Fetch fetch = s -> withResponse(333, "kaputt");

        //when
        final String html = processor(fetch).fetch("redirect", "template", CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"redirect\"> --><!-- </esi:include> -->"));
    }

    @Test
    public void shouldFailOnUnknownStatusCodeWithError() throws IOException {
        Fetch fetch = s -> withResponse(333, "kaputt");

        //when
        final String html = processor(fetch).fetch("redirect", "template", NO_CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"redirect\"> -->333: some status<!-- </esi:include> -->"));
    }


    @Test
    public void shouldHandleExceptionInFetchFunction() throws IOException {
        Fetch fetch = s -> {
            throw new IllegalStateException("error");
        };

        //when
        final String html = processor(fetch).fetch("redirect", "template", CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"redirect\"> --><!-- </esi:include> -->"));
    }

    @Test
    public void shouldHandleExceptionInFetchFunctionWithError() throws IOException {
        Fetch fetch = s -> {
            throw new IllegalStateException("error");
        };

        //when
        final String html = processor(fetch).fetch("redirect", "template", NO_CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"redirect\"> -->error<!-- </esi:include> -->"));
    }

    @Test
    public void shouldReplaceRelativePathWithAbsolutePath() throws Exception {
        Fetch fetch = src -> withResponse(200, src);

        //when
        final String html = processor(fetch).fetch("/relative", "template", NO_CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"/relative\"> -->https://www.otto.de/relative<!-- </esi:include> -->"));
    }

    @Test
    public void shouldNotReplaceRelativePathWhenHostnameIsEmpty() throws Exception {
        Fetch fetch = src -> withResponse(200, src);

        //when
        final String html = processor(fetch, null).fetch("/relative", "template", NO_CONTINUE_ON_ERROR);

        //then
        assertThat(html, is("<!-- <esi:include src=\"/relative\"> -->/relative<!-- </esi:include> -->"));
    }

    private Fetch fetchWith404() {
        return s -> withResponse(404, "irgendwas");
    }

    private EsiIncludeElementProcessor processor(Fetch fetch) {
        return processor(fetch, "https://www.otto.de");
    }

    private EsiIncludeElementProcessor processor(Fetch fetch, String hostname) {
        return new EsiIncludeElementProcessor(fetch, hostname);
    }

    private Response withResponse(int statusCode, String responseBody) {
        Response response = mock(Response.class);
        when(response.getResponseBody()).thenReturn(responseBody);
        when(response.getStatusCode()).thenReturn(statusCode);
        when(response.getStatusText()).thenReturn("some status");
        return response;
    }

    */
}