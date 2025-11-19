package org.tkit.onecx.shell.bff.rs.mappers;

import static gen.org.tkit.onecx.product.store.client.model.MicrofrontendTypePSV1.MODULE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.model.*;

@Mapper
public interface WorkspaceConfigMapper {

    WorkspaceDTO map(Workspace workspaceInfo);

    @Mapping(target = "overrides", ignore = true)
    @Mapping(expression = "java( String.valueOf(themeInfo.getProperties()) )", target = "properties")
    ThemeDTO mapTheme(Theme themeInfo);

    default RouteDTO mapRoute(MicrofrontendPSV1 mfe, ProductPSV1 product,
            List<Microfrontend> wsMfes, String workspaceUrl) {
        RouteDTO route = new RouteDTO();
        route.setRemoteEntryUrl(mfe.getRemoteEntry());
        route.setExposedModule(mfe.getExposedModule());
        route.setProductName(product.getName());
        route.setDisplayName(product.getDisplayName());
        route.setAppId(mfe.getAppId());
        route.setTechnology(TechnologiesDTO.ANGULAR);
        var selectedMfe = wsMfes.stream().filter(microfrontend -> microfrontend.getMfeId().equals(mfe.getAppId())).findFirst();
        selectedMfe.ifPresent(microfrontend -> route.setBaseUrl(workspaceUrl + microfrontend.getBasePath()));
        route.setUrl(mfe.getRemoteBaseUrl());
        route.setPathMatch(PathMatchDTO.PREFIX);
        if (mfe.getRemoteName() != null) {
            route.setRemoteName(mfe.getRemoteName());
        }
        return route;
    }

    WorkspaceLoadRequest createRequest(LoadWorkspaceConfigRequestDTO dto);

    default LoadWorkspaceConfigResponseDTO createResponse(WorkspaceWrapper workspaceWrapper) {
        return new LoadWorkspaceConfigResponseDTO().workspace(createWorkspace(workspaceWrapper));
    }

    WorkspaceDTO createWorkspace(WorkspaceWrapper workspaceWrapper);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "baseUrl", source = "mfe.remoteBaseUrl")
    @Mapping(target = "remoteEntryUrl", source = "mfe.remoteEntry")
    @Mapping(target = "elementName", source = "mfe.tagName")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productVersion", source = "product.version")
    RemoteComponentDTO createComponent(LoadProductItemPSV1 product, LoadProductMicrofrontendPSV1 mfe);

    @AfterMapping
    default void componentName(@MappingTarget RemoteComponentDTO target, LoadProductItemPSV1 product,
            LoadProductMicrofrontendPSV1 mfe) {
        target.setName(componentName(product, mfe));
    }

    default String componentName(LoadProductItemPSV1 product, LoadProductMicrofrontendPSV1 mfe) {
        String name = "";
        if (product != null) {
            name = product.getName();
        }
        if (mfe == null) {
            return name;
        }
        return componentName(name, mfe.getAppId(), mfe.getExposedModule());
    }

    default String componentName(String productName, String appId, String exposedModule) {
        return productName + "#" + appId + "#" + exposedModule;
    }

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "technology", source = "mfe.technology")
    @Mapping(target = "url", source = "mfe.remoteBaseUrl")
    @Mapping(target = "pathMatch", constant = "PREFIX")
    @Mapping(target = "baseUrl", ignore = true)
    @Mapping(target = "remoteEntryUrl", source = "mfe.remoteEntry")
    @Mapping(target = "displayName", source = "productDisplayName")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "appId", source = "mfe.appId")
    @Mapping(target = "exposedModule", source = "mfe.exposedModule")
    @Mapping(target = "remoteName", source = "mfe.remoteName")
    @Mapping(target = "endpoints", source = "mfe.endpoints")
    @Mapping(target = "elementName", source = "mfe.tagName")
    @Mapping(target = "productVersion", source = "product.version")
    RouteDTO createRoute(LoadProductItemPSV1 product, LoadProductMicrofrontendPSV1 mfe,
            Map<String, List<Microfrontend>> pathMapping,
            WorkspaceWrapper workspace, String productBaseUrl, String productDisplayName);

    default TechnologiesDTO toEnum(String technologyString) {
        for (TechnologiesDTO technology : TechnologiesDTO.values()) {
            if (technology.toString().toUpperCase().equals(technologyString)) {
                return technology;
            }
        }
        return null;
    }

    @AfterMapping
    default void createRouteAfter(@MappingTarget RouteDTO target,
            Map<String, List<Microfrontend>> pathMapping,
            WorkspaceWrapper workspace,
            String productBaseUrl) {
        var mfeList = pathMapping.get(target.getAppId());
        if (mfeList == null || mfeList.isEmpty()) {
            return;
        }

        Microfrontend selectedMfe = null;
        var exposedModule = target.getExposedModule();
        boolean isExposedModuleEmpty = exposedModule.isBlank();

        if (!isExposedModuleEmpty) {
            selectedMfe = mfeList.stream()
                    .filter(mfe -> exposedModule.equals(mfe.getExposedModule()))
                    .findFirst()
                    .orElse(null);

            if (selectedMfe == null && mfeList.size() == 1
                    && (mfeList.get(0).getExposedModule() == null || mfeList.get(0).getExposedModule().isBlank())) {
                selectedMfe = mfeList.get(0);
            }
        } else {
            if (mfeList.size() == 1) {
                selectedMfe = mfeList.get(0);
            }
        }

        if (selectedMfe != null && selectedMfe.getBasePath() != null) {
            target.setBaseUrl(workspace.getBaseUrl() + productBaseUrl + selectedMfe.getBasePath());
        }
    }

    List<SlotDTO> createSlots(List<WorkspaceWrapperSlot> slots);

    default SlotDTO createSlot(WorkspaceWrapperSlot slot) {
        if (slot == null) {
            return null;
        }

        SlotDTO result = new SlotDTO().name(slot.getName());
        if (slot.getComponents() != null) {
            slot.getComponents().removeIf(Objects::isNull);
            slot.getComponents()
                    .forEach(c -> result.addComponentsItem(componentName(c.getProductName(), c.getAppId(), c.getName())));
        }
        return result;
    }

    @Mapping(target = "overrides", ignore = true)
    @Mapping(target = "properties", ignore = true)
    ThemeDTO createTheme(Theme themeInfo);

    @AfterMapping
    default void createThemeAfter(@MappingTarget ThemeDTO target, Theme themeInfo) {
        if (themeInfo != null) {
            target.setProperties(String.valueOf(themeInfo.getProperties()));
        }
    }

    default LoadProductRequestPSV1 create(WorkspaceWrapper wrapper) {
        return new LoadProductRequestPSV1().productNames(wrapper.getProducts().stream().map(Product::getProductName).toList());
    }

    @SuppressWarnings("java:S3776")
    default void createMfeAndComponents(LoadWorkspaceConfigResponseDTO result, WorkspaceWrapper wrapper,
            LoadProductResponsePSV1 loadProducts) {
        if (loadProducts == null || loadProducts.getProducts() == null) {
            return;
        }

        var workspaceProducts = wrapper.getProducts().stream().collect(Collectors.toMap(Product::getProductName, p -> p));

        loadProducts.getProducts().forEach(product -> {

            var workspaceProduct = workspaceProducts.get(product.getName());

            // create mapping APP_ID -> LIST OF PATHS
            var pathMapping = workspaceProduct.getMicrofrontends().stream()
                    .collect(Collectors.groupingBy(Microfrontend::getMfeId));

            if (product.getMicrofrontends() != null) {
                product.getMicrofrontends().forEach(mfe -> {
                    if (mfe.getType() == MODULE) {
                        var productDisplayName = (workspaceProduct.getDisplayName() != null) ? workspaceProduct.getDisplayName()
                                : product.getDisplayName();
                        var route = createRoute(product, mfe, pathMapping, wrapper, workspaceProduct.getBaseUrl(),
                                productDisplayName);
                        if (route.getBaseUrl() != null) {
                            result.addRoutesItem(createRoute(product, mfe, pathMapping, wrapper, workspaceProduct.getBaseUrl(),
                                    productDisplayName));
                        }
                    } else if (mfe.getType() == MicrofrontendTypePSV1.COMPONENT) {
                        result.addComponentsItem(createComponent(product, mfe));
                    }
                });
            }

        });
    }

}
