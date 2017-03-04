package de.otto.rx.composer.content;


import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class HeadersTest {

    @Test
    public void shouldIgnoreCase() {
        Headers headers = Headers.of(ImmutableMap.of("key", singletonList("value")));
        assertThat(headers.get("KeY", "error"), is("value"));
    }

    @Test
    public void shouldReturnEmptyOptional() {
        Headers headers = Headers.emptyHeaders();
        assertThat(headers.get("KeY"), is(empty()));
    }

    @Test
    public void shouldReturnDefaultValue() {
        Headers headers = Headers.emptyHeaders();
        assertThat(headers.get("KeY", "some default"), is("some default"));
    }

    @Test
    public void shouldReturnFirstOfMany() {
        Headers headers = Headers.of(ImmutableMap.of("key", asList("first", "second")));
        assertThat(headers.get("KeY", "error"), is("first"));
    }

    @Test
    public void shouldReturnAll() {
        Headers headers = Headers.of(ImmutableMap.of("key", asList("first", "second")));
        assertThat(headers.getAll("KeY"), contains("first", "second"));
    }
}