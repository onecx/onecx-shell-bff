package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.tkit.onecx.shell.bff.rs.controllers.TranslationRestController;

import gen.org.tkit.onecx.file.storage.client.model.PresignedUrlRequest;
import gen.org.tkit.onecx.file.storage.client.model.PresignedUrlResponse;
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
        String token = keycloakClient.getAccessToken(ADMIN);
        GetTranslationsRequestDTO requestDTO = new GetTranslationsRequestDTO();
        requestDTO.setLocale("DE");
        requestDTO.setContext(List.of(new TranslationsContextDTO().artifact("angular-accelerator").version("^8.0.0")));

        PresignedUrlRequest presignedUrlRequest = new PresignedUrlRequest();
        presignedUrlRequest.setApplicationId("onecx-shell-bff");
        presignedUrlRequest.setProductName("onecx-shell");
        presignedUrlRequest.setFileName("DE/angular-accelerator-^8.0.0.json");

        PresignedUrlResponse presignedUrlResponse = new PresignedUrlResponse();
        presignedUrlResponse.setUrl("https://mocked-url/download");

        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/v1/file-storage/presigned/download")
                        .withHeader(APM_HEADER_PARAM, ADMIN)
                        .withBody(JsonBody.json(presignedUrlRequest)))
                .withId("mockPresignedUrl")
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader(APPLICATION_JSON)
                                .withBody(JsonBody.json(presignedUrlResponse)));

        var output = given()
                .when()
                .auth().oauth2(token)
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

        mockServerClient.clear("mockPresignedUrl");
    }

    @Test
    void getTranslationsNullUrlTest() {
        String token = keycloakClient.getAccessToken(ADMIN);
        GetTranslationsRequestDTO requestDTO = new GetTranslationsRequestDTO();
        requestDTO.setLocale("DE");
        requestDTO.setContext(List.of(new TranslationsContextDTO().artifact("angular-accelerator").version("^8.0.0")));

        PresignedUrlRequest presignedUrlRequest = new PresignedUrlRequest();
        presignedUrlRequest.setApplicationId("onecx-shell-bff");
        presignedUrlRequest.setProductName("onecx-shell");
        presignedUrlRequest.setFileName("DE/angular-accelerator-^8.0.0.json");

        PresignedUrlResponse presignedUrlResponse = new PresignedUrlResponse();
        presignedUrlResponse.setUrl(null);

        mockServerClient
                .when(request()
                        .withMethod("POST")
                        .withPath("/v1/file-storage/presigned/download")
                        .withHeader(APM_HEADER_PARAM, ADMIN)
                        .withBody(JsonBody.json(presignedUrlRequest)))
                .withId("mockPresignedUrlNull")
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader(APPLICATION_JSON)
                                .withBody(JsonBody.json(presignedUrlResponse)));

        var output = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetTranslationsResponseDTO.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(requestDTO.getContext().size(), output.getTranslations().size());
        Assertions.assertNull(output.getTranslations().get(0).getUrl());
        Assertions.assertEquals(requestDTO.getContext().get(0).getArtifact(), output.getTranslations().get(0).getArtifact());
        Assertions.assertEquals(requestDTO.getContext().get(0).getVersion(), output.getTranslations().get(0).getVersion());

        mockServerClient.clear("mockPresignedUrlNull");
    }
}
