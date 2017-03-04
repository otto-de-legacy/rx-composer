package de.otto.rx.composer.client;


import java.util.Objects;

import static de.otto.rx.composer.client.DefaultRef.noResiliency;
import static de.otto.rx.composer.client.DefaultRef.noRetries;
import static de.otto.rx.composer.client.DefaultRef.singleRetry;

public final class ClientConfig {

    private final Ref key;
    private final int connectTimeout;
    private final int readTimeout;
    private final boolean resilient;
    private final int retries;

    public ClientConfig(final Ref key,
                         final int connectTimeout,
                         final int readTimeout,
                         final boolean resilient,
                         final int retries) {
        this.key = key;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retries = retries;
        this.resilient = retries > 0 || resilient;
    }

    public static ClientConfig singleRetry() {
        return new ClientConfig(singleRetry,
                1000,
                500,
                true,
                1);
    }

    public static ClientConfig singleRetry(final Ref ref, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(ref, connectTimeout, readTimeout, true,1);
    }

    public static ClientConfig noResiliency() {
        return new ClientConfig(noResiliency,
                1000,
                500,
                false,
                0);
    }

    public static ClientConfig noResiliency(final Ref ref, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(ref, connectTimeout, readTimeout, false, 0);
    }

    public static ClientConfig noRetries() {
        return new ClientConfig(noRetries,
                1000,
                500,
                true,
                0);
    }

    public static ClientConfig noRetries(final Ref ref, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(ref, connectTimeout, readTimeout, true, 0);
    }

    public Ref getRef() {
        return key;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public boolean isResilient() {
        return resilient;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientConfig that = (ClientConfig) o;
        return connectTimeout == that.connectTimeout &&
                readTimeout == that.readTimeout &&
                resilient == that.resilient &&
                retries == that.retries &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, connectTimeout, readTimeout, resilient, retries);
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "key=" + key +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", resilient=" + resilient +
                ", retries=" + retries +
                '}';
    }
}
