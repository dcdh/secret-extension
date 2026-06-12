package org.acme.secret.extension.runtime;

import io.quarkus.arc.Unremovable;
import io.quarkus.vault.VaultKVSecretEngine;
import io.quarkus.vault.client.VaultClientException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Unremovable
public class VaultSecretRepository implements SecretRepository {

    private final VaultKVSecretEngine vaultKVSecretEngine;

    public VaultSecretRepository(final VaultKVSecretEngine vaultKVSecretEngine) {
        this.vaultKVSecretEngine = Objects.requireNonNull(vaultKVSecretEngine);
    }

    @Override
    public Optional<String> getSecret(final String name) throws UnableToRetrieveSecretException {
        Objects.requireNonNull(name);
        try {
            final Map<String, String> data = vaultKVSecretEngine.readSecret(name);
            if (data == null || !data.containsKey("secret")) {
                return Optional.empty();
            }
            final String secret = data.get("secret");
            return Optional.of(secret);
        } catch (final VaultClientException vaultClientException) {
            if (Integer.valueOf(404).equals(vaultClientException.getStatus())) {
                return Optional.empty();
            } else {
                throw new UnableToRetrieveSecretException(vaultClientException);
            }
        }
    }

    @Override
    public String store(final String name, final String value) throws SecretAlreadyStoredException, UnableToStoreSecretException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        final Map<String, String> secret = Map.of("secret", value);
        try {
            final Map<String, String> existing = vaultKVSecretEngine.readSecret(name);
            if (existing != null && existing.containsKey("secret")) {
                throw new SecretAlreadyStoredException();
            } else {
                vaultKVSecretEngine.writeSecret(name, secret);
                return value;
            }
        } catch (final VaultClientException vaultClientException) {
            if (Integer.valueOf(404).equals(vaultClientException.getStatus())) {
                // emitted by readSecret if the secret does not exist
                vaultKVSecretEngine.writeSecret(name, secret);
                return value;
            } else {
                throw new UnableToStoreSecretException(vaultClientException);
            }
        }
    }
}
