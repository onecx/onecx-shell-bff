package org.tkit.onecx.shell.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.product.store.client.model.MicrofrontendPSV1;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteriaPSV1;
import gen.org.tkit.onecx.product.store.client.model.ProductPSV1;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.model.GetWorkspaceByUrlRequest;
import gen.org.tkit.onecx.workspace.client.model.Microfrontend;
import gen.org.tkit.onecx.workspace.client.model.Workspace;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceAbstract;

@Mapper
public interface WorkspaceConfigMapper {

    GetWorkspaceByUrlRequest map(GetWorkspaceConfigRequestDTO getWorkspaceConfigRequestDTO);

    WorkspaceDTO map(Workspace workspaceInfo);

    @Mapping(expression = "java( String.valueOf(themeInfo.getProperties()) )", target = "properties")
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
        route.setPathMatch(PathMatchDTO.PREFIX);
        if (mfe.getRemoteName() != null) {
            route.setRemoteName(mfe.getRemoteName());
        }
        return route;
    }
}
