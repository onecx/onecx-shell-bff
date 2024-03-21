package org.tkit.onecx.shell.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.product.store.client.model.MicrofrontendPSV1;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteriaPSV1;
import gen.org.tkit.onecx.product.store.client.model.ProductPSV1;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.model.Microfrontend;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceAbstract;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceSearchCriteria;

@Mapper
public interface WorkspaceConfigMapper {
    @Mapping(target = "themeName", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "pageSize", constant = "1")
    @Mapping(target = "pageNumber", constant = "0")
    WorkspaceSearchCriteria map(GetWorkspaceConfigRequestDTO getWorkspaceConfigRequestDTO);

    @Mapping(target = "name", source = "workspaceInfo.name")
    @Mapping(target = "baseUrl", source = "requestDTO.baseUrl")
    WorkspaceDTO map(WorkspaceAbstract workspaceInfo, GetWorkspaceConfigRequestDTO requestDTO);

    ThemeDTO mapTheme(Theme themeInfo);

    @Mapping(target = "productNames", source = "products")
    @Mapping(target = "pageSize", ignore = true)
    @Mapping(target = "pageNumber", ignore = true)
    ProductItemSearchCriteriaPSV1 map(WorkspaceAbstract workspaceInfo);

    default AngularRouteDTO mapRoute(MicrofrontendPSV1 mfe, ProductPSV1 product,
            List<Microfrontend> wsMfes, String workspaceBasePath) {
        AngularRouteDTO angularRoute = new AngularRouteDTO();
        angularRoute.setRemoteEntryUrl(mfe.getRemoteEntry());
        angularRoute.setExposedModule(mfe.getExposedModule());
        angularRoute.setProductName(product.getName());
        angularRoute.setAppId(mfe.getAppId());
        angularRoute.setTechnology(TechnologiesDTO.ANGULAR);
        var selectedMfe = wsMfes.stream().filter(microfrontend -> microfrontend.getMfeId().equals(mfe.getAppId())).findFirst();
        selectedMfe.ifPresent(microfrontend -> angularRoute.setBasePath(microfrontend.getBasePath()));
        angularRoute.setUrl(workspaceBasePath);
        angularRoute.setPathMatch(PathMatchDTO.FULL); //temp fixed value
        return angularRoute;
    }

    default WebComponentRouteDTO mapWcRoute(MicrofrontendPSV1 mfe, ProductPSV1 product,
            List<Microfrontend> wsMfes, String workspaceBasePath) {
        WebComponentRouteDTO wcRoute = new WebComponentRouteDTO();
        wcRoute.setRemoteEntryUrl(mfe.getRemoteEntry());
        wcRoute.setExposedModule(mfe.getExposedModule());
        wcRoute.setProductName(product.getName());
        wcRoute.setAppId(mfe.getAppId());
        wcRoute.setTechnology(TechnologiesDTO.WEBCOMPONENT);
        var selectedMfe = wsMfes.stream().filter(microfrontend -> microfrontend.getMfeId().equals(mfe.getAppId())).findFirst();
        selectedMfe.ifPresent(microfrontend -> wcRoute.setBasePath(microfrontend.getBasePath()));
        wcRoute.setUrl(workspaceBasePath);
        wcRoute.setPathMatch(PathMatchDTO.FULL); //temp fixed value
        return wcRoute;
    }

    default GetWorkspaceConfigResponseDTO updateConfigRoutes(GetWorkspaceConfigResponseDTO responseDTO,
            List<AngularRouteDTO> angularRoutes,
            List<WebComponentRouteDTO> webComponentRoutes) {
        GetWorkspaceConfigResponseRoutesDTO routesDTO = new GetWorkspaceConfigResponseRoutesDTO();
        routesDTO.setAngularRoutes(angularRoutes);
        routesDTO.setWebComponentRoutes(webComponentRoutes);
        responseDTO.setRoutes(routesDTO);
        return responseDTO;
    }
}
