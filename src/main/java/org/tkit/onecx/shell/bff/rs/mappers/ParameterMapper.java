package org.tkit.onecx.shell.bff.rs.mappers;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.parameter.client.model.Parameter;
import gen.org.tkit.onecx.parameter.client.model.ParametersBulkRequest;
import gen.org.tkit.onecx.parameter.client.model.ParametersBulkResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersResponseDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ParameterDTO;

@Mapper
public interface ParameterMapper {
    ParametersBulkRequest map(GetParametersRequestDTO getParametersRequestDTO);

    @Mapping(target = "removeProductsItem", ignore = true)
    GetParametersResponseDTO mapResponse(ParametersBulkResponse parametersBulkResponse);

    Map<String, Map<String, List<ParameterDTO>>> map(Map<String, Map<String, List<Parameter>>> value);

    Map<String, List<ParameterDTO>> mapSub(Map<String, List<Parameter>> value);

    ParameterDTO map(Parameter parameter);

    List<ParameterDTO> map(List<Parameter> value);
}
