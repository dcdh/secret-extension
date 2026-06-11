package org.acme.secret.extension;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.secret.extension.runtime.InMemorySecretRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@QuarkusTest
class InMemorySecretRepositoryTest {

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
