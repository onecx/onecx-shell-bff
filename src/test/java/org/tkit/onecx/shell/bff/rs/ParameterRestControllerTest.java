package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.shell.bff.rs.controllers.ParameterRestController;

import gen.org.tkit.onecx.parameter.client.model.Parameter;
import gen.org.tkit.onecx.parameter.client.model.ParametersBulkRequest;
import gen.org.tkit.onecx.parameter.client.model.ParametersBulkResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetParametersResponseDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ParameterRestController.class)
class ParameterRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getParametersTest() {

        ParametersBulkRequest request = new ParametersBulkRequest();
        var data = new HashMap<>(Map.of("p1", Set.of("app1")));
        request.setProducts(data);

        ParametersBulkResponse response = new ParametersBulkResponse();
        response.setProducts(Map.of("p1",
                Map.of("app1", List.of(new Parameter().productName("p1").applicationId("app1").name("n1").value("v1")))));
        // create mock rest endpoint for permission svc
        mockServerClient
                .when(request().withPath("/bff/v1/parameters").withMethod(HttpMethod.POST)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(request)))
                .withId("mockParameter")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        GetParametersRequestDTO requestDTO = new GetParametersRequestDTO();
        requestDTO.setProducts(data);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetParametersResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(response.getProducts().size(), output.getProducts().size());

        mockServerClient.clear("mockParameter");
    }

    @Test
    void getParameters_Svc_error_test() {
        ParametersBulkRequest request = new ParametersBulkRequest();
        var data = new HashMap<>(Map.of("p1", Set.of("app1")));
        request.setProducts(data);
        // create mock rest endpoint for permission svc
        mockServerClient
                .when(request().withPath("/bff/v1/parameters").withMethod(HttpMethod.POST)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(request)))
                .withId("mockParameter")
                .respond(httpRequest -> response().withStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        GetParametersRequestDTO requestDTO = new GetParametersRequestDTO();
        requestDTO.setProducts(data);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetParametersResponseDTO.class);

        Assertions.assertNotNull(output);
        mockServerClient.clear("mockParameter");
    }

    @Test
    void getParametersConstraintViolationExceptionTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
