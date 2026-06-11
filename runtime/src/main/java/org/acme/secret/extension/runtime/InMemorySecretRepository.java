package org.acme.secret.extension.runtime;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Unremovable
public class InMemorySecretRepository implements SecretRepository {

    private final Map<String, String> secrets = new HashMap<>();

    @Override
    public Optional<String> getSecret(final String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(secrets.get(name));
    }

    @Override
    public String store(final String name, final String secret) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(secret);
        secrets.put(name, secret);
        return secret;
    }
}
