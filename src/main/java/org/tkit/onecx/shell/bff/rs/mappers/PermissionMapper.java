package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.quarkus.permission.client.PermissionResponse;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsResponseDTO;
import io.smallrye.mutiny.Uni;

@Mapper
public interface PermissionMapper {
    @Mapping(target = "removePermissionsItem", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    default GetPermissionsResponseDTO map(Uni<PermissionResponse> rawPermission) {
        GetPermissionsResponseDTO permissionsResponseDTO = new GetPermissionsResponseDTO();
        permissionsResponseDTO.setPermissions(rawPermission.await().indefinitely().getActions());
        return permissionsResponseDTO;
    }
}
