package de.otto.rx.composer.client;

/**
 * Refs for default {@link ClientConfig client configurations}.
 */
public enum DefaultRef implements Ref {

     /**
     * Key of a ClientConfig that is not using circuit-breakers or retries, but only timeouts.
     */
    noResiliency,
    /**
     * Key of a ClientConfig that is using timouts and circuit-breakers, but not doing retries.
     */
    noRetries,
    /**
     * Key of a ClientConfig that is using timeouts, circuit-breakers and a single retry on error.
     */
    singleRetry
}
