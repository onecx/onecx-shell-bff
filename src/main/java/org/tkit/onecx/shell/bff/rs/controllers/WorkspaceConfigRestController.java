package org.tkit.onecx.shell.bff.rs.controllers;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.shell.bff.rs.mappers.WorkspaceConfigMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.ProductPSV1;
import gen.org.tkit.onecx.shell.bff.rs.internal.WorkspaceConfigApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.api.ThemesApi;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.api.WorkspaceExternalApi;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceLoad;
import gen.org.tkit.onecx.workspace.client.model.WorkspacePageResult;

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

    @Override
    public Response getWorkspaceConfig(GetWorkspaceConfigRequestDTO getWorkspaceConfigRequestDTO) {
        GetWorkspaceConfigResponseDTO responseDTO = new GetWorkspaceConfigResponseDTO();

        //get base workspace info
        try (Response response = workspaceClient.searchWorkspaces(mapper.map(getWorkspaceConfigRequestDTO))) {
            var workspaceInfo = response.readEntity(WorkspacePageResult.class).getStream().get(0);
            responseDTO.setWorkspace(mapper.map(workspaceInfo, getWorkspaceConfigRequestDTO));

            //getDetailed workspace info (incl. products, mfes etc.)
            try (Response workspaceDetailResponse = workspaceClient.loadWorkspaceByName(workspaceInfo.getName())) {
                var detailedWorkspaceInfo = workspaceDetailResponse.readEntity(WorkspaceLoad.class);

                //get productStore information for each Product
                List<RouteDTO> routes = new ArrayList<>();
                detailedWorkspaceInfo.getProducts().forEach(p -> {
                    try (Response psResponse = productStoreClient.getProductByName(p.getProductName())) {
                        var product = psResponse.readEntity(ProductPSV1.class);
                        product.getMicrofrontends().forEach(mfe -> {
                            routes.add(mapper.mapRoute(mfe, product, p.getMicrofrontends(),
                                    getWorkspaceConfigRequestDTO.getBaseUrl()));
                        });
                    }
                });
                responseDTO.setRoutes(routes);
            }
            //get theme info
            try (Response themeResponse = themeClient.getThemeByName(workspaceInfo.getTheme())) {
                var themeInfo = themeResponse.readEntity(Theme.class);
                responseDTO.setTheme(mapper.mapTheme(themeInfo));
            }

        }
        return Response.status(Response.Status.OK).entity(responseDTO).build();
    }
}
