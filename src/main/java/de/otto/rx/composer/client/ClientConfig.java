package de.otto.rx.composer.client;

import static de.otto.rx.composer.client.ClientConfig.DefaultConfigRef.DEFAULT_CONFIG;

public final class ClientConfig {

    public enum DefaultConfigRef implements ClientConfigRef {DEFAULT_CONFIG}

    private final ClientConfigRef clientConfigRef;
    private final int connectTimeout;
    private final int readTimeout;
    private final int retries;

    private ClientConfig(final ClientConfigRef clientConfigRef,
                         final int connectTimeout,
                         final int readTimeout,
                         final int retries) {
        this.clientConfigRef = clientConfigRef;
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

    public static ClientConfig clientConfig(final ClientConfigRef clientConfigRef,
                                            final int connectTimeout,
                                            final int readTimeout,
                                            final int retries) {
        return new ClientConfig(clientConfigRef, connectTimeout, readTimeout, retries);
    }

    public ClientConfigRef getRef() {
        return clientConfigRef;
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
