package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.quarkus.permission.service.PermissionResponse;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsResponseDTO;

@Mapper
public interface PermissionMapper {
    @Mapping(target = "removePermissionsItem", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    default GetPermissionsResponseDTO map(PermissionResponse rawPermission) {
        GetPermissionsResponseDTO permissionsResponseDTO = new GetPermissionsResponseDTO();
        permissionsResponseDTO.setPermissions(rawPermission.getActions());
        return permissionsResponseDTO;
    }
}
