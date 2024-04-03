package org.tkit.onecx.shell.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsRequestDTO;

@ApplicationScoped
public class PermissionLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, GetPermissionsRequestDTO.class,
                        x -> GetPermissionsRequestDTO.class.getSimpleName() + "[productName:"
                                + ((GetPermissionsRequestDTO) x).getProductName() +
                                ", appId:"
                                + ((GetPermissionsRequestDTO) x).getAppId() + "]"));
    }
}
