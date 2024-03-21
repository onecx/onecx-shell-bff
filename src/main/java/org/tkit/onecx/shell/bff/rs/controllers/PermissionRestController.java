package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.quarkus.permission.client.PermissionClientService;
import org.tkit.onecx.shell.bff.rs.PermissionConfig;
import org.tkit.onecx.shell.bff.rs.mappers.PermissionMapper;
import org.tkit.quarkus.context.ApplicationContext;
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

    @Override
    public Response getPermissions(GetPermissionsRequestDTO getPermissionsRequestDTO) {
        var context = ApplicationContext.get();
        var principalToken = context.getPrincipalToken().toString();
        var rawPermission = permissionClientService.getPermissions(getPermissionsRequestDTO.getProductName(),
                getPermissionsRequestDTO.getAppId(),
                principalToken, config.keySeparator(), config.cachingEnabled());

        return Response.status(Response.Status.OK).entity(mapper.map(rawPermission)).build();
    }
}
