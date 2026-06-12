package org.acme.secret.extension.deployment;

import io.quarkus.builder.Version;
import io.quarkus.maven.dependency.Dependency;
import io.quarkus.test.QuarkusExtensionTest;
import jakarta.inject.Inject;
import org.acme.secret.extension.runtime.JdbcPostgresSecretRepository;
import org.acme.secret.extension.runtime.SecretAlreadyStoredException;
import org.acme.secret.extension.runtime.UnableToRetrieveSecretException;
import org.acme.secret.extension.runtime.UnableToStoreSecretException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class JdbcPostgresSecretRepositoryTest {

    @RegisterExtension
    static QuarkusExtensionTest runner = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("init.sql", "init.sql"))
            .withConfigurationResource("application.properties")
            .setForcedDependencies(List.of(
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql", Version.getVersion()),
                    Dependency.of("io.quarkus", "quarkus-jdbc-postgresql-deployment", Version.getVersion())
            ));

    @Inject
    JdbcPostgresSecretRepository jdbcPostgresSecretRepository;

    @Inject
    DataSource dataSource;

    @BeforeEach
    @AfterEach
    void tearDown() {
        try (final Connection connection = dataSource.getConnection();
             final Statement stmt = connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE secret");
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldStoreSecret() throws SecretAlreadyStoredException, UnableToRetrieveSecretException, UnableToStoreSecretException {
        Optional<String> firstGet = jdbcPostgresSecretRepository.getSecret("my-secret");
        String stored = jdbcPostgresSecretRepository.store("my-secret", "my-value");
        Optional<String> secondGet = jdbcPostgresSecretRepository.getSecret("my-secret");

        assertAll(
                () -> assertThat(firstGet).isEmpty(),
                () -> assertThat(stored).isEqualTo("my-value"),
                () -> assertThat(secondGet).isEqualTo(Optional.of("my-value"))
        );
    }

    @Test
    void shouldFailToStoreSecretWhenAlreadyStored() throws SecretAlreadyStoredException, UnableToStoreSecretException {
        jdbcPostgresSecretRepository.store("my-secret", "my-value");
        assertThatThrownBy(() -> jdbcPostgresSecretRepository.store("my-secret", "new-my-value"))
                .isInstanceOf(SecretAlreadyStoredException.class)
                .hasRootCauseInstanceOf(PSQLException.class);
    }
}
