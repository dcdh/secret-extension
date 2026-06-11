package org.acme.secret.extension.deployment;

import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.vault.VaultKVSecretEngine;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.acme.secret.extension.runtime.VaultSecretRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class VaultSecretRepositoryTest {

    @RegisterExtension
    static QuarkusExtensionTest runner = new QuarkusExtensionTest()
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkiverse.vault", "quarkus-vault", "4.7.0")
            ));

    @Inject
    VaultSecretRepository vaultSecretRepository;

    @Inject
    VaultKVSecretEngine vaultKVSecretEngine;

    @Test
    void shouldStoreSecret() {
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
}
