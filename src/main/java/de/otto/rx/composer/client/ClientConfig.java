package de.otto.rx.composer.client;


public final class ClientConfig {

    public static final String DEFAULT_CONFIG = "default";

    private final String key;
    private final int connectTimeout;
    private final int readTimeout;

    private final int retries;

    private ClientConfig(final String key,
                         final int connectTimeout,
                         final int readTimeout,
                         final int retries) {
        this.key = key;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retries = retries;
    }

    public static ClientConfig singleRetry() {
        return new ClientConfig(DEFAULT_CONFIG,
                1000,
                250,
                1);
    }

    public static ClientConfig noRetries() {
        return new ClientConfig(DEFAULT_CONFIG,
                1000,
                250,
                1);
    }

    public static ClientConfig clientConfig(final String key,
                                            final int connectTimeout,
                                            final int readTimeout,
                                            final int retries) {
        return new ClientConfig(key, connectTimeout, readTimeout, retries);
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

    public int getRetries() {
        return retries;
    }
}
