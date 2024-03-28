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

    default RouteDTO mapRoute(MicrofrontendPSV1 mfe, ProductPSV1 product,
            List<Microfrontend> wsMfes) {
        RouteDTO route = new RouteDTO();
        route.setRemoteEntryUrl(mfe.getRemoteEntry());
        route.setExposedModule(mfe.getExposedModule());
        route.setProductName(product.getName());
        route.setAppId(mfe.getAppId());
        route.setTechnology(TechnologiesDTO.ANGULAR);
        var selectedMfe = wsMfes.stream().filter(microfrontend -> microfrontend.getMfeId().equals(mfe.getAppId())).findFirst();
        selectedMfe.ifPresent(microfrontend -> route.setBaseUrl(microfrontend.getBasePath()));
        route.setUrl(mfe.getRemoteBaseUrl());
        route.setPathMatch(PathMatchDTO.FULL); //temp fixed value
        if (mfe.getRemoteName() != null) {
            route.setRemoteName(mfe.getRemoteName());
        }
        return route;
    }
}
