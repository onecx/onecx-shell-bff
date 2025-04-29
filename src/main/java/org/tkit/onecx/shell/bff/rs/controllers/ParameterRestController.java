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
import org.tkit.onecx.shell.bff.rs.mappers.ParameterMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.parameter.client.api.ParametersBffApi;
import gen.org.tkit.onecx.parameter.client.model.ParametersBulkResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.ParameterApiService;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersResponseDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ParameterRestController implements ParameterApiService {

    @Inject
    @RestClient
    ParametersBffApi parametersBffApi;

    @Inject
    ParameterMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getParameters(GetParametersRequestDTO getParametersRequestDTO) {
        GetParametersResponseDTO responseDTO;
        try (Response response = parametersBffApi.getParametersByProductsAndAppIds(mapper.map(getParametersRequestDTO))) {
            responseDTO = mapper.mapResponse(response.readEntity(ParametersBulkResponse.class));
        } catch (ClientWebApplicationException ex) {
            responseDTO = new GetParametersResponseDTO();
        }
        return Response.status(Response.Status.OK).entity(responseDTO).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
