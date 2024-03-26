package org.tkit.onecx.shell.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.quarkus.permission.client.PermissionClientService;
import org.tkit.onecx.shell.bff.rs.PermissionConfig;
import org.tkit.onecx.shell.bff.rs.mappers.PermissionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.client.api.PermissionApi;
import gen.org.tkit.onecx.shell.bff.rs.internal.PermissionApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsRequestDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class PermissionRestController implements PermissionApiService {

    @Inject
    @RestClient
    PermissionApi permissionClient;

    @Inject
    PermissionClientService permissionClientService;

    @Inject
    PermissionConfig config;

    @Inject
    PermissionMapper mapper;

    @Context
    HttpHeaders httpHeaders;

    @Override
    public Response getPermissions(GetPermissionsRequestDTO getPermissionsRequestDTO) {
        var principalToken = httpHeaders.getRequestHeader(AUTHORIZATION).get(0);
        var rawPermission = permissionClientService.getPermissions(getPermissionsRequestDTO.getProductName(),
                getPermissionsRequestDTO.getAppId(),
                principalToken, config.keySeparator(), config.cachingEnabled());

        return Response.status(Response.Status.OK).entity(mapper.map(rawPermission)).build();
    }
}
