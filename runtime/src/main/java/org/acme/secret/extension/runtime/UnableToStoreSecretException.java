package org.acme.secret.extension.runtime;

public class UnableToStoreSecretException extends Exception {

    public UnableToStoreSecretException(final Throwable cause) {
        super(cause);
    }
}
