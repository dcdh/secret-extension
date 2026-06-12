package org.acme.secret.extension;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.secret.extension.runtime.JdbcPostgresSecretRepository;
import org.acme.secret.extension.runtime.SecretAlreadyStoredException;
import org.acme.secret.extension.runtime.UnableToRetrieveSecretException;
import org.acme.secret.extension.runtime.UnableToStoreSecretException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

@Path("/secret")
public class JdbcPostgresSecretRepositoryResource {

    @ServerExceptionMapper
    public RestResponse<String> mapException(Exception exception) {
        Response.Status status = switch (exception) {
            case SecretAlreadyStoredException ignored -> Response.Status.CONFLICT;
            case UnableToRetrieveSecretException ignored -> Response.Status.INTERNAL_SERVER_ERROR;
            case UnableToStoreSecretException ignored -> Response.Status.INTERNAL_SERVER_ERROR;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
        return RestResponse.status(status, ExceptionUtils.getThrowableList(exception)
                .stream()
                .map(t -> t.getClass().getSimpleName())
                .distinct()
                .collect(Collectors.joining(" -> ")));
    }

    @Inject
    JdbcPostgresSecretRepository jdbcPostgresSecretRepository;

    @Inject
    DataSource dataSource;

    @Path("/{name}/tearDown")
    @DELETE
    public void tearDown(@PathParam("name") String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM secret WHERE name = ?")) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to delete secret '" + name + "'", e);
        }
    }

    @Path("/{name}")
    @GET
    public String getSecret(@PathParam("name") String name) throws UnableToRetrieveSecretException {
        return jdbcPostgresSecretRepository.getSecret(name).orElse("null");
    }

    @Path("/{name}/store")
    @POST
    public String store(@PathParam("name") String name, @FormParam("value") String value) throws UnableToStoreSecretException, SecretAlreadyStoredException {
        return jdbcPostgresSecretRepository.store(name, value);
    }
}
