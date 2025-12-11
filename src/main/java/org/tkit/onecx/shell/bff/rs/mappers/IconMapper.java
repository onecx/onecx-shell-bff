package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.icon.client.model.IconCriteria;
import gen.org.tkit.onecx.icon.client.model.IconListResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.IconCriteriaDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.IconListResponseDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface IconMapper {
    IconCriteria mapCriteria(IconCriteriaDTO iconCriteriaDTO);

    @Mapping(target = "removeIconsItem", ignore = true)
    IconListResponseDTO map(IconListResponse iconListResponse);
}
