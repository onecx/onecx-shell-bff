package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.shell.bff.rs.controllers.UserProfileRestController;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetUserProfileResponseDTO;
import gen.org.tkit.onecx.user.profile.client.model.MenuMode;
import gen.org.tkit.onecx.user.profile.client.model.UserPerson;
import gen.org.tkit.onecx.user.profile.client.model.UserProfile;
import gen.org.tkit.onecx.user.profile.client.model.UserProfileAccountSettings;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
class UserProfileControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getUserProfileTest() {

        UserProfile profileResponse = new UserProfile();
        profileResponse.organization("org1").id("profile1")
                .person(new UserPerson().firstName("Max").lastName("Mustermann").email("testEmail"))
                .accountSettings(new UserProfileAccountSettings().menuMode(MenuMode.HORIZONTAL));

        // create mock rest endpoint for user-profile-svc
        mockServerClient.when(request().withPath("/v1/userProfile/me").withMethod(HttpMethod.GET))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(profileResponse)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetUserProfileResponseDTO.class);

        Assertions.assertNotNull(output);

        mockServerClient.clear("mock");
    }

    @Test
    void getUserProfile_svc_error_Test() {

        // create mock rest endpoint for user-profile-svc
        mockServerClient.when(request().withPath("/v1/userProfile/me").withMethod(HttpMethod.GET))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode()));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .get()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);
        mockServerClient.clear("mock");
    }
}
