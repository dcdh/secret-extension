package org.acme.secret.extension.deployment;

import io.quarkus.test.QuarkusExtensionTest;
import jakarta.inject.Inject;
import org.acme.secret.extension.runtime.InMemorySecretRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class InMemorySecretRepositoryTest {

    @RegisterExtension
    static QuarkusExtensionTest runner = new QuarkusExtensionTest();

    @Inject
    InMemorySecretRepository inMemorySecretRepository;

    @Test
    void shouldStoreSecret() {
        Optional<String> firstGet = inMemorySecretRepository.getSecret("my-secret");
        String stored = inMemorySecretRepository.store("my-secret", "my-value");
        Optional<String> secondGet = inMemorySecretRepository.getSecret("my-secret");

        assertAll(
                () -> assertThat(firstGet).isEmpty(),
                () -> assertThat(stored).isEqualTo("my-value"),
                () -> assertThat(secondGet).isEqualTo(Optional.of("my-value"))
        );
    }
}
