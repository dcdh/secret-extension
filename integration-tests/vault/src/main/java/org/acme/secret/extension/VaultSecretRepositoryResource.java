package org.acme.secret.extension;

import io.quarkus.vault.VaultKVSecretEngine;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.acme.secret.extension.runtime.SecretAlreadyStoredException;
import org.acme.secret.extension.runtime.UnableToRetrieveSecretException;
import org.acme.secret.extension.runtime.UnableToStoreSecretException;
import org.acme.secret.extension.runtime.VaultSecretRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.stream.Collectors;

@Path("/secret")
public class VaultSecretRepositoryResource {

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
    VaultSecretRepository vaultSecretRepository;

    @Inject
    VaultKVSecretEngine vaultKVSecretEngine;

    @Path("/{name}/tearDown")
    @DELETE
    public void tearDown(@PathParam("name") String name) {
        vaultKVSecretEngine.deleteSecret(name);
    }

    @Path("/{name}")
    @GET
    public String getSecret(@PathParam("name") String name) throws UnableToRetrieveSecretException {
        return vaultSecretRepository.getSecret(name).orElse("null");
    }

    @Path("/{name}/store")
    @POST
    public String store(@PathParam("name") String name, @FormParam("value") String value) throws UnableToStoreSecretException, SecretAlreadyStoredException {
        return vaultSecretRepository.store(name, value);
    }
}
