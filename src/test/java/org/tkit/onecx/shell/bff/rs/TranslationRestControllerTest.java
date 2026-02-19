package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.tkit.onecx.shell.bff.rs.controllers.TranslationRestController;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TranslationRestController.class)
class TranslationRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getTranslationsTest() {

        GetTranslationsRequestDTO requestDTO = new GetTranslationsRequestDTO();
        requestDTO.setLocale("DE");
        requestDTO.setContext(List.of(new TranslationsContextDTO().artifact("angular-accelerator").version("^8.0.0")));
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
                .extract().as(GetTranslationsResponseDTO.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getTranslations().size(), requestDTO.getContext().size());
        Assertions.assertEquals(output.getTranslations().get(0).getArtifact(), requestDTO.getContext().get(0).getArtifact());
        Assertions.assertEquals(output.getTranslations().get(0).getVersion(), requestDTO.getContext().get(0).getVersion());
    }
}
