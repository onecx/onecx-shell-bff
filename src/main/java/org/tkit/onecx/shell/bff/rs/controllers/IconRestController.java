package org.tkit.onecx.shell.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.shell.bff.rs.mappers.IconMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.icon.client.api.IconsApi;
import gen.org.tkit.onecx.icon.client.model.IconListResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.IconApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.IconCriteriaDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class IconRestController implements IconApiService {

    @Inject
    @RestClient
    IconsApi iconsApi;

    @Inject
    IconMapper iconMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response findIconsByNamesAndRefId(String refId, IconCriteriaDTO iconCriteriaDTO) {
        try (Response response = iconsApi.findIconsByNamesAndRefId(refId, iconMapper.mapCriteria(iconCriteriaDTO))) {

            var mappedIconList = iconMapper.map(response.readEntity(IconListResponse.class));
            return Response.status(response.getStatus()).entity(mappedIconList).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
