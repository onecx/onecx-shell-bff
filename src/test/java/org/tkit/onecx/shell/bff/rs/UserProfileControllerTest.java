package org.tkit.onecx.shell.bff.rs;

import org.mockserver.client.MockServerClient;
import org.tkit.onecx.shell.bff.rs.controllers.UserProfileRestController;

import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserProfileRestController.class)
public class UserProfileControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    //    @Test
    //    void getUserProfileTest() {
    //
    //        UserProfile profileResponse = new UserProfile();
    //        profileResponse.organization("org1").id("profile1")
    //                .person(new UserPerson().firstName("Max").lastName("Mustermann").email("testEmail"))
    //                .accountSettings(new UserProfileAccountSettings().menuMode(MenuMode.HORIZONTAL));
    //
    //        // create mock rest endpoint for user-profile-svc
    //        mockServerClient.when(request().withPath("/v1/userProfile/me").withMethod(HttpMethod.GET))
    //                .withId("mock")
    //                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
    //                        .withContentType(MediaType.APPLICATION_JSON)
    //                        .withBody(JsonBody.json(profileResponse)));
    //
    //        var output = given()
    //                .when()
    //                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
    //                .header(APM_HEADER_PARAM, ADMIN)
    //                .get()
    //                .then()
    //                .statusCode(Response.Status.OK.getStatusCode())
    //                .contentType(APPLICATION_JSON)
    //                .extract().as(GetUserProfileResponseDTO.class);
    //
    //        Assertions.assertNotNull(output);
    //    }
}
