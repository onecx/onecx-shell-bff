package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.shell.bff.rs.controllers.IconRestController;

import gen.org.tkit.onecx.icon.client.model.Icon;
import gen.org.tkit.onecx.icon.client.model.IconCriteria;
import gen.org.tkit.onecx.icon.client.model.IconListResponse;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.IconCriteriaDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.IconListResponseDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(IconRestController.class)
class IconRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void findIconsByNamesAndRefId_Test() {

        IconListResponse iconListResponse = new IconListResponse();
        Icon icon1 = new Icon();
        icon1.setName("prime:abc");
        icon1.setBody("someBody");
        icon1.setType("SVG");

        Icon icon2 = new Icon();
        icon2.setName("prime:def");
        icon2.setBody("someBody");
        icon2.setType("SVG");

        iconListResponse.setIcons(List.of(icon1, icon2));
        IconCriteria criteria = new IconCriteria();
        criteria.setNames(List.of("prime:abc", "prime:def"));

        mockServerClient.when(request().withPath("/v1/icons/theme1").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(iconListResponse)));

        IconCriteriaDTO criteriaDTO = new IconCriteriaDTO();
        criteriaDTO.setNames(List.of("prime:abc", "prime:def"));
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .pathParam("refId", "theme1")
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(IconListResponseDTO.class);

        Assertions.assertEquals(iconListResponse.getIcons().size(), output.getIcons().size());
        mockServerClient.clear("mock");
    }

    @Test
    void findIconsByNamesAndRefId_Missing_Criteria_Test() {

        IconCriteriaDTO criteriaDTO = new IconCriteriaDTO();
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .pathParam("refId", "theme1")
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void findIconsByNamesAndRefId_Client_Eception_Test() {
        IconCriteria criteria = new IconCriteria();
        criteria.setNames(List.of("prime:abc", "prime:def"));

        mockServerClient.when(request().withPath("/v1/icons/theme1").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode()));
        IconCriteriaDTO criteriaDTO = new IconCriteriaDTO();
        criteriaDTO.setNames(List.of("prime:abc", "prime:def"));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .pathParam("refId", "theme1")
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        mockServerClient.clear("mock");
    }
}
