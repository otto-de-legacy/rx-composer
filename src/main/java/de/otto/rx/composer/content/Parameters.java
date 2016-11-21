package de.otto.rx.composer.content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public final class Parameters {

    private static final Parameters EMPTY_PARAMETERS = parameters(ImmutableMap.of());

    private final ImmutableMap<String, Object> params;

    private Parameters(final ImmutableMap<String, Object> params) {
        this.params = params;
    }

    public static Parameters emptyParameters() {
        return EMPTY_PARAMETERS;
    }

    public static Parameters parameters(final ImmutableMap<String, Object> params) {
        return new Parameters(params);
    }

    public Parameters with(final Parameters moreParams) {
        return new Parameters(ImmutableMap.<String,Object>builder()
                .putAll(params)
                .putAll(moreParams.params)
                .build());
    }

    public String getString(final String key) {
        return params.containsKey(key) ? params.get(key).toString() : null;
    }

    public ImmutableSet<String> getKeys(final String key) {
        return params.keySet();
    }

    public ImmutableMap<String, Object> asImmutableMap() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameters that = (Parameters) o;

        return params != null ? params.equals(that.params) : that.params == null;

    }

    @Override
    public int hashCode() {
        return params != null ? params.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "params=" + params +
                '}';
    }
}
