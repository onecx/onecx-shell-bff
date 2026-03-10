package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.file.storage.client.model.PresignedUrlRequest;
import gen.org.tkit.onecx.file.storage.client.model.PresignedUrlResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.TranslationResultDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.TranslationsContextDTO;

@Mapper
public interface TranslationMapper {

    @Mapping(target = "applicationId", expression = "java(\"onecx-shell-bff\")")
    @Mapping(target = "productName", expression = "java(\"onecx-shell\")")
    @Mapping(target = "fileName", expression = "java(getTranslationsRequestDTO.getLocale() + \"/\" + translationsContextDTO.getArtifact() + \"-\" + translationsContextDTO.getVersion() + \".json\")")
    PresignedUrlRequest map(TranslationsContextDTO translationsContextDTO, GetTranslationsRequestDTO getTranslationsRequestDTO);

    @Mapping(target = "artifact", source = "translationsContextDTO.artifact")
    @Mapping(target = "version", source = "translationsContextDTO.version")
    @Mapping(target = "url", source = "presignedUrlResponse.url")
    TranslationResultDTO map(PresignedUrlResponse presignedUrlResponse, TranslationsContextDTO translationsContextDTO);
}
