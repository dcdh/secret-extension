package org.acme.secret.extension.runtime;

public class SecretAlreadyStoredException extends Exception {

    public SecretAlreadyStoredException() {
    }

    public SecretAlreadyStoredException(final Throwable cause) {
        super(cause);
    }
}
