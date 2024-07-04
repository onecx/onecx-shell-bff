package org.tkit.onecx.shell.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.quarkus.permission.service.PermissionResponse;
import org.tkit.onecx.shell.bff.rs.ShellConfig;
import org.tkit.onecx.shell.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.PermissionMapper;
import org.tkit.quarkus.log.cdi.LogExclude;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.client.api.PermissionApi;
import gen.org.tkit.onecx.permission.client.model.ApplicationPermissions;
import gen.org.tkit.onecx.permission.client.model.PermissionRequest;
import gen.org.tkit.onecx.shell.bff.rs.internal.PermissionApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class PermissionRestController implements PermissionApiService {

    @Inject
    @RestClient
    PermissionApi permissionClient;

    @Inject
    ShellConfig config;

    @Inject
    PermissionMapper mapper;

    @Context
    HttpHeaders httpHeaders;

    @Inject
    ExceptionMapper exceptionMapper;

    @CacheName("onecx-shell")
    Cache cache;

    @Override
    public Response getPermissions(GetPermissionsRequestDTO getPermissionsRequestDTO) {
        var principalToken = httpHeaders.getRequestHeader(AUTHORIZATION).get(0);
        var rawPermission = getPermissions(getPermissionsRequestDTO.getProductName(),
                getPermissionsRequestDTO.getAppId(),
                principalToken, config.permissions().keySeparator());

        return Response.status(Response.Status.OK).entity(mapper.map(rawPermission)).build();
    }

    public PermissionResponse getPermissions(String productName, String appName, @LogExclude(mask = "****") String token,
            String keySeparator) {
        if (!config.permissions().cachingEnabled()) {
            return getPermissionsLocal(productName, appName, token, keySeparator);
        }
        var key = new CompositeCacheKey(productName, appName, token);
        return cache.get(key, compositeCacheKey -> getPermissionsLocal(productName, appName, token, keySeparator)).await()
                .indefinitely();
    }

    public PermissionResponse getPermissionsLocal(String productName, String appName, @LogExclude(mask = "****") String token,
            String keySeparator) {
        try (Response response = permissionClient.getApplicationPermissions(productName, appName,
                new PermissionRequest().token(token))) {
            var data = response.readEntity(ApplicationPermissions.class);
            List<String> result = new ArrayList<>();
            if (data.getPermissions() != null) {
                data.getPermissions().forEach((resource, actions) -> {
                    if (actions != null && !actions.isEmpty()) {
                        actions.forEach(action -> result.add(resource + keySeparator + action));
                    }
                });
            }
            return PermissionResponse.create(result);
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
