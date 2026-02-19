package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsResponseDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.TranslationResultDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.TranslationsContextDTO;

@Mapper
public interface TranslationMapper {

    @Mapping(target = "translations", source = "context")
    @Mapping(target = "removeTranslationsItem", ignore = true)
    GetTranslationsResponseDTO map(GetTranslationsRequestDTO getTranslationsRequestDTO);

    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "removeTranslationsItem", ignore = true)
    TranslationResultDTO map(TranslationsContextDTO contextDTO);
}
