package org.acme.secret.extension.runtime;

import java.util.Optional;

public interface SecretRepository {

    Optional<String> getSecret(String name) throws UnableToRetrieveSecretException;

    String store(String name, String secret) throws SecretAlreadyStoredException, UnableToStoreSecretException;
}
