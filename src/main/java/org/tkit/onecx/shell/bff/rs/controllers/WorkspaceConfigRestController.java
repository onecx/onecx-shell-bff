package org.tkit.onecx.shell.bff.rs.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.shell.bff.rs.RemoteComponentMockConfig;
import org.tkit.onecx.shell.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.WorkspaceConfigMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.ProductPSV1;
import gen.org.tkit.onecx.shell.bff.rs.internal.WorkspaceConfigApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.api.ThemesApi;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.workspace.client.model.Workspace;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceLoad;

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

    @Inject
    RemoteComponentMockConfig mockConfig;

    @Context
    UriInfo uriInfo;

    @Override
    public Response getWorkspaceConfig(GetWorkspaceConfigRequestDTO getWorkspaceConfigRequestDTO) {
        GetWorkspaceConfigResponseDTO responseDTO = new GetWorkspaceConfigResponseDTO();

        //get base workspace info
        try (Response response = workspaceClient.getWorkspaceByUrl(mapper.map(getWorkspaceConfigRequestDTO))) {
            Response.ResponseBuilder responseBuilder = null;
            var workspaceResponse = response.readEntity(Workspace.class);
            if (workspaceResponse != null) {
                responseDTO.setWorkspace(mapper.map(workspaceResponse));

                //getDetailed workspace info (incl. products, mfes etc.)
                try (Response workspaceDetailResponse = workspaceClient.loadWorkspaceByName(workspaceResponse.getName())) {
                    var detailedWorkspaceInfo = workspaceDetailResponse.readEntity(WorkspaceLoad.class);

                    //get productStore information for each Product
                    List<RouteDTO> routes = new ArrayList<>();
                    detailedWorkspaceInfo.getProducts().forEach(p -> {
                        try (Response psResponse = productStoreClient.getProductByName(p.getProductName())) {
                            var product = psResponse.readEntity(ProductPSV1.class);
                            product.getMicrofrontends()
                                    .forEach(mfe -> routes.add(mapper.mapRoute(mfe, product, p.getMicrofrontends(),
                                            workspaceResponse.getBaseUrl())));
                        } catch (WebApplicationException ex) {
                            //skip
                        }
                    });
                    responseDTO.setRoutes(routes);
                }
                //get theme info
                try (Response themeResponse = themeClient.getThemeByName(workspaceResponse.getTheme())) {
                    var themeInfo = themeResponse.readEntity(Theme.class);
                    if (themeInfo.getFaviconUrl() == null) {
                        themeInfo.setFaviconUrl(uriInfo.getPath() + "/themes/" + themeInfo.getName() + "/favicon");
                    }
                    if (themeInfo.getLogoUrl() == null) {
                        themeInfo.setLogoUrl(uriInfo.getPath() + "/themes/" + themeInfo.getName() + "/logo");
                    }
                    responseDTO.setTheme(mapper.mapTheme(themeInfo));
                }
                //call remoteComponent Mocks => should be removed after implementation
                responseDTO = mockRemoteComponents(responseDTO);
                responseBuilder = Response.status(Response.Status.OK).entity(responseDTO);
            } else {
                responseBuilder = Response.status(Response.Status.NOT_FOUND.getStatusCode(),
                        "No workspace with matching url found");
            }
            return responseBuilder.build();
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
    public Response getThemeLogoByName(String name) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = themeClient.getThemeLogoByName(name)) {
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

    /**
     * SHOULD BE REMOVED AFTER IMPLEMENTATION
     *
     * Method to mock remoteComponents based on application.properties
     *
     * @param responseDTO responseDTO without remoteComponents
     * @return responseDTO with mocked remoteComponents
     */
    public GetWorkspaceConfigResponseDTO mockRemoteComponents(GetWorkspaceConfigResponseDTO responseDTO) {
        List<RemoteComponentDTO> remoteComponents = new ArrayList<>();
        List<RemoteComponentMappingDTO> remoteShellComponents = new ArrayList<>();

        mockConfig.keys().forEach(componentKey -> {
            RemoteComponentDTO componentDTO = new RemoteComponentDTO();
            componentDTO.setName(mockConfig.name().get(componentKey));
            componentDTO.setAppId(mockConfig.appId().get(componentKey));
            componentDTO.setBaseUrl(mockConfig.baseUrl().get(componentKey));
            componentDTO.setRemoteEntryUrl(mockConfig.remoteEntryUrl().get(componentKey));
            componentDTO.setExposedModule(mockConfig.exposedModule().get(componentKey));
            componentDTO.setProductName(mockConfig.productName().get(componentKey));
            remoteComponents.add(componentDTO);

            RemoteComponentMappingDTO componentMappingDTO = new RemoteComponentMappingDTO();
            componentMappingDTO.setRemoteComponent(mockConfig.name().get(componentKey));
            componentMappingDTO.setSlotName(mockConfig.slot().get(componentKey));
            remoteShellComponents.add(componentMappingDTO);
        });
        responseDTO.setRemoteComponents(remoteComponents);
        responseDTO.setShellRemoteComponents(remoteShellComponents);
        return responseDTO;
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
