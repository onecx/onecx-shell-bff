package org.tkit.onecx.shell.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.quarkus.permission.client.PermissionClientService;
import org.tkit.onecx.shell.bff.rs.PermissionConfig;
import org.tkit.onecx.shell.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.PermissionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.permission.client.api.PermissionApi;
import gen.org.tkit.onecx.shell.bff.rs.internal.PermissionApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ProblemDetailResponseDTO;

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

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getPermissions(GetPermissionsRequestDTO getPermissionsRequestDTO) {
        var principalToken = httpHeaders.getRequestHeader(AUTHORIZATION).get(0);
        var rawPermission = permissionClientService.getPermissions(getPermissionsRequestDTO.getProductName(),
                getPermissionsRequestDTO.getAppId(),
                principalToken, config.keySeparator(), config.cachingEnabled());

        return Response.status(Response.Status.OK).entity(mapper.map(rawPermission)).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
