package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.shell.bff.rs.mappers.TranslationMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.file.storage.client.api.FileStorageApi;
import gen.org.tkit.onecx.file.storage.client.model.PresignedUrlResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.TranslationApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsResponseDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.TranslationResultDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class TranslationRestController implements TranslationApiService {

    @Inject
    @RestClient
    FileStorageApi fileStorageClient;

    @Inject
    TranslationMapper mapper;

    @Override
    public Response getTranslations(GetTranslationsRequestDTO getTranslationsRequestDTO) {

        GetTranslationsResponseDTO getTranslationsResponseDTO = new GetTranslationsResponseDTO();
        getTranslationsRequestDTO.getContext().forEach(translationsContextDTO -> {
            try (Response response = fileStorageClient
                    .getPresignedDownloadUrl(mapper.map(translationsContextDTO, getTranslationsRequestDTO))) {
                var data = response.readEntity(PresignedUrlResponse.class);
                TranslationResultDTO translationResultDTO = mapper.map(data, translationsContextDTO);
                getTranslationsResponseDTO.addTranslationsItem(translationResultDTO);
            }
        });
        return Response.status(Response.Status.OK).entity(getTranslationsResponseDTO).build();
    }
}
