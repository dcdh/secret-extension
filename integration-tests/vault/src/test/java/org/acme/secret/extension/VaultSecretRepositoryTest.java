package org.acme.secret.extension;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vault.VaultKVSecretEngine;
import jakarta.inject.Inject;
import org.acme.secret.extension.runtime.SecretAlreadyStoredException;
import org.acme.secret.extension.runtime.UnableToRetrieveSecretException;
import org.acme.secret.extension.runtime.UnableToStoreSecretException;
import org.acme.secret.extension.runtime.VaultSecretRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class VaultSecretRepositoryTest {

    @Inject
    VaultSecretRepository vaultSecretRepository;

    @Inject
    VaultKVSecretEngine vaultKVSecretEngine;

    @AfterEach
    @BeforeEach
    void tearDown() {
        vaultKVSecretEngine.deleteSecret("my-secret");
    }

    @Test
    void shouldStoreSecret() throws SecretAlreadyStoredException, UnableToRetrieveSecretException, UnableToStoreSecretException {
        Optional<String> firstGet = vaultSecretRepository.getSecret("my-secret");
        String stored = vaultSecretRepository.store("my-secret", "my-value");
        Optional<String> secondGet = vaultSecretRepository.getSecret("my-secret");
        Map<String, String> secret = vaultKVSecretEngine.readSecret("my-secret");

        assertAll(
                () -> assertThat(firstGet).isEmpty(),
                () -> assertThat(stored).isEqualTo("my-value"),
                () -> assertThat(secondGet).isEqualTo(Optional.of("my-value")),
                () -> assertThat(secret).isEqualTo(Map.of("secret", "my-value"))
        );
    }

    @Test
    void shouldFailToStoreSecretWhenAlreadyStored() throws SecretAlreadyStoredException, UnableToStoreSecretException {
        vaultSecretRepository.store("my-secret", "my-value");
        assertThatThrownBy(() -> vaultSecretRepository.store("my-secret", "new-my-value"))
                .isExactlyInstanceOf(SecretAlreadyStoredException.class);
    }
}
