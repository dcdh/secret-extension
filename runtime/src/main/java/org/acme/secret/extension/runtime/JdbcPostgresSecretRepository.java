package org.acme.secret.extension.runtime;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Unremovable
public class JdbcPostgresSecretRepository implements SecretRepository {

    private final DataSource dataSource;

    public JdbcPostgresSecretRepository(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public Optional<String> getSecret(final String name) throws UnableToRetrieveSecretException {
        final String sql =
                // language=sql
                """
                        SELECT value FROM secret WHERE name = ?
                        """;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getString("value"));
            }
        } catch (final SQLException sqlException) {
            throw new UnableToRetrieveSecretException(sqlException);
        }
    }

    @Override
    public String store(final String name, final String value) throws SecretAlreadyStoredException, UnableToStoreSecretException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        final String sql =
                // language=sql
                """
                        INSERT INTO secret(name, value) VALUES (?, ?)
                        """;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, value);
            stmt.executeUpdate();
            return value;
        } catch (final SQLException sqlException) {
            if (isUniqueViolation(sqlException)) {
                throw new SecretAlreadyStoredException(sqlException);
            } else {
                throw new UnableToStoreSecretException(sqlException);
            }
        }
    }

    private boolean isUniqueViolation(final SQLException exception) {
        SQLException current = exception;
        while (current != null) {
            /*
             * PostgreSQL unique violation SQL state
             */
            if ("23505".equals(current.getSQLState())) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }
}
