package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.shell.bff.rs.mappers.TranslationMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.shell.bff.rs.internal.TranslationApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetTranslationsRequestDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class TranslationRestController implements TranslationApiService {

    @Inject
    TranslationMapper mapper;

    // To be implemented
    @Override
    public Response getTranslations(GetTranslationsRequestDTO getTranslationsRequestDTO) {
        return Response.status(Response.Status.OK).entity(mapper.map(getTranslationsRequestDTO)).build();
    }
}
