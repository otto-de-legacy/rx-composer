package de.otto.edison.aggregator.content;

import com.google.common.collect.ImmutableCollection;
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
    private final ImmutableMap<String,List<Object>> headers;

    /**
     * Creates Headers from a Map.
     *
     * @param headers Map of headers.
     */
    public Headers(final Map<String, List<Object>> headers) {
        this.headers = headers != null ? lowerCaseKeysOf(headers) : ImmutableMap.of();
    }

    /**
     * Returns an optionally available header value for a case-insensitive key as
     * an expected type. If the value has multiple values, the first value is returned.
     * <p>
     *     If the value has a different type as requested, a ClassCastException will be thrown.
     * </p>
     * @param key case-insensitive key of the header value.
     * @param asType the expected type of the value
     * @param <T> dito
     * @return optional value
     */
    public <T> Optional<T> getValue(final String key, final Class<T> asType) {
        return Optional.ofNullable(
                getValues(key, asType).isEmpty()
                        ? null
                        : getValues(key, asType).iterator().next());
    }

    /**
     * Returns the immutable collection of values for the given case-insensitive key.
     * <p>
     *     If the value has a different type as requested, a ClassCastException will be thrown.
     * </p>
     * @param key case-insensitive key of the header values
     * @param asType the expected type of the values
     * @param <T> dito
     * @return immutable collection of values.
     */
    @SuppressWarnings("unchecked")
    public <T> ImmutableCollection<T> getValues(final String key, final Class<T> asType) {
        final String caseInsensitiveKey = key.toLowerCase();
        final ImmutableCollection<T> values = (ImmutableCollection<T>) headers.get(caseInsensitiveKey);
        return values == null || values.isEmpty()
                        ? ImmutableList.of()
                        : values;
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
    private ImmutableMap<String, List<Object>> lowerCaseKeysOf(final Map<String, List<Object>> headers) {
        final ImmutableMap.Builder<String, List<Object>> builder = ImmutableMap.builder();
        headers.entrySet().forEach(entry -> {
            List<Object> values = entry.getValue();
            builder.put(entry.getKey().toLowerCase(), copyOf(values));
        });
        return builder.build();
    }
}
