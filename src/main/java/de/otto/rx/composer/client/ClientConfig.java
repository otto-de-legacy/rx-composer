package de.otto.rx.composer.client;


public final class ClientConfig {

    private final String key;
    private final int connectTimeout;
    private final int readTimeout;
    private final boolean resilient;
    private final int retries;

    public ClientConfig(final String key,
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
        return new ClientConfig("default-single-retry",
                1000,
                500,
                true,
                1);
    }

    public static ClientConfig singleRetry(final String key, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(key, connectTimeout, readTimeout, true,1);
    }

    public static ClientConfig noResiliency() {
        return new ClientConfig("default-no-resiliency",
                1000,
                500,
                false,
                0);
    }

    public static ClientConfig noResiliency(final String key, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(key, connectTimeout, readTimeout, false, 0);
    }

    public static ClientConfig noRetries() {
        return new ClientConfig("default-no-retries",
                1000,
                500,
                true,
                0);
    }

    public static ClientConfig noRetries(final String key, final int connectTimeout, final int readTimeout) {
        return new ClientConfig(key, connectTimeout, readTimeout, true, 0);
    }

    public String getKey() {
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
}
