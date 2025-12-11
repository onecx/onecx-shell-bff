package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.WorkspaceConfigMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.shell.bff.rs.internal.WorkspaceConfigApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.api.ThemesApi;
import gen.org.tkit.onecx.theme.client.model.AvailableImageTypes;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceConfigRestController implements WorkspaceConfigApiService {

    @Inject
    @RestClient
    WorkspaceExternalApi workspaceClient;

    @Inject
    @RestClient
    ThemesApi themeClient;

    @Inject
    @RestClient
    ProductsApi productStoreClient;

    @Inject
    WorkspaceConfigMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response loadWorkspaceConfig(LoadWorkspaceConfigRequestDTO loadWorkspaceConfigRequestDTO) {

        WorkspaceWrapper wrapper;
        try (Response response = workspaceClient.loadWorkspaceByRequest(mapper.createRequest(loadWorkspaceConfigRequestDTO))) {
            wrapper = response.readEntity(WorkspaceWrapper.class);
        }
        var result = mapper.createResponse(wrapper);

        // load products and create corresponding module and components
        if (wrapper.getProducts() != null) {
            try (Response psResponse = productStoreClient.loadProductsByNames(mapper.create(wrapper))) {
                var productResponse = psResponse.readEntity(LoadProductResponsePSV1.class);
                mapper.createMfeAndComponents(result, wrapper, productResponse);
            }
        }

        // create slots
        result.setSlots(mapper.createSlots(wrapper.getSlots()));

        //get theme info
        try (Response themeResponse = themeClient.getThemeByName(wrapper.getTheme())) {
            var theme = themeResponse.readEntity(Theme.class);
            result.setTheme(mapper.createTheme(theme));
        }

        return Response.ok(result).build();
    }

    @Override
    public Response getAvailableImageTypes(String name) {
        try (Response response = themeClient.getAvailableImageTypes(name)) {
            var availableTypes = response.readEntity(AvailableImageTypes.class);
            return Response.ok(mapper.mapImageTypes(availableTypes)).build();
        }
    }

    @Override
    public Response getThemeFaviconByName(String name) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = themeClient.getThemeFaviconByName(name)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }
            return responseBuilder.build();
        }
    }

    @Override
    public Response getThemeImageByNameAndRefType(String name, String refType) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = themeClient.getThemeImageByNameAndRefType(name, refType)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }
            return responseBuilder.build();
        }
    }

    @Override
    public Response getThemeLogoByName(String name, Boolean small) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = themeClient.getThemeLogoByName(name, small)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }
            return responseBuilder.build();
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
