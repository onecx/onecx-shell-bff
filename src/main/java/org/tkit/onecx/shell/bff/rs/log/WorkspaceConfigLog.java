package org.tkit.onecx.shell.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.LoadWorkspaceConfigRequestDTO;

@ApplicationScoped
public class WorkspaceConfigLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, LoadWorkspaceConfigRequestDTO.class,
                        x -> LoadWorkspaceConfigRequestDTO.class.getSimpleName() + ",path:"
                                + ((LoadWorkspaceConfigRequestDTO) x).getPath()));
    }
}
