package org.tkit.onecx.shell.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetWorkspaceConfigRequestDTO;

@ApplicationScoped
public class WorkspaceConfigLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, GetWorkspaceConfigRequestDTO.class,
                        x -> GetWorkspaceConfigRequestDTO.class.getSimpleName() + "[url:"
                                + ((GetWorkspaceConfigRequestDTO) x).getUrl() + "]"));
    }
}
