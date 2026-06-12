package org.acme.secret.extension.runtime;

public class UnableToRetrieveSecretException extends Exception {

    public UnableToRetrieveSecretException(final Throwable cause) {
        super(cause);
    }
}
