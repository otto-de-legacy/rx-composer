package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Meta-information about {@link Content} items.
 * <p>
 *     The keys used to select header values are case-insensitive.
 * </p>
 */
public final class Headers {
    private final ImmutableMap<String,ImmutableList<String>> headers;

    /**
     * Creates Headers from a Map.
     *
     * @param headers Map of headers.
     */
    private Headers(final Map<String, List<String>> headers) {
        this.headers = headers != null ? lowerCaseKeysOf(headers) : ImmutableMap.of();
    }

    public static Headers emptyHeaders() {
        return new Headers(null);
    }

    public static Headers of(final Map<String, List<String>> headers) {
        return new Headers(headers);
    }

    /**
     * Returns an optionally available header value for a case-insensitive key.
     * <p>
     *     If the value has multiple values, the first value is returned.
     * </p>
     * @param key case-insensitive key of the header value.
     * @return optional value
     */
    public Optional<String> get(final String key) {
        final ImmutableList<String> values = getAll(key);
        return values.isEmpty()
                ? Optional.empty()
                : Optional.of(values.get(0));
    }

    /**
     * Returns a header value for a case-insensitive key, or the default value, if the header does not exist.
     * <p>
     *     If the value has multiple values, the first value is returned.
     * </p>
     * @param key case-insensitive key of the header value.
     * @param defaultValue default value that is returned if value does not exist.
     * @return value or default value
     */
    public String get(final String key, final String defaultValue) {
        final ImmutableList<String> values = getAll(key);
        return values.isEmpty()
                ? defaultValue
                : values.get(0);
    }

    /**
     * Returns the immutable collection of values for the given case-insensitive key.
     * @param key case-insensitive key of the header values
     * @return immutable collection of values.
     */
    @SuppressWarnings("unchecked")
    public ImmutableList<String> getAll(final String key) {
        final String caseInsensitiveKey = key.toLowerCase();
        final ImmutableList<String> values = headers.get(caseInsensitiveKey);
        return values != null ? values : ImmutableList.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Headers headers1 = (Headers) o;

        return headers != null ? headers.equals(headers1.headers) : headers1.headers == null;

    }

    @Override
    public int hashCode() {
        return headers != null ? headers.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Headers{" +
                "headers=" + headers +
                '}';
    }

    /**
     * Converts a map of headers into an immutable map with lower-case keys.
     *
     * @param headers the given headers
     * @return immutable headers with lower-case keys.
     */
    private ImmutableMap<String, ImmutableList<String>> lowerCaseKeysOf(final Map<String, List<String>> headers) {
        final ImmutableMap.Builder<String, ImmutableList<String>> builder = ImmutableMap.builder();
        headers.entrySet().forEach(
                entry -> builder.put(entry.getKey().toLowerCase(), copyOf(entry.getValue()))
        );
        return builder.build();
    }
}
