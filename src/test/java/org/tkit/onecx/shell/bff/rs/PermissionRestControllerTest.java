package org.tkit.onecx.shell.bff.rs;

import jakarta.inject.Inject;

import org.mockserver.client.MockServerClient;
import org.tkit.onecx.shell.bff.rs.controllers.PermissionRestController;

import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(PermissionRestController.class)
public class PermissionRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Inject
    PermissionConfig config;

    //    @Test
    //    void getPermissionsTest() {
    //
    //        GetPermissionsRequestDTO requestDTO = new GetPermissionsRequestDTO();
    //        requestDTO.setAppId("app1");
    //        requestDTO.setProductName("product1");
    //        var output = given()
    //                .when()
    //                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
    //                .header(APM_HEADER_PARAM, ADMIN)
    //                .contentType(APPLICATION_JSON)
    //                .body(requestDTO)
    //                .post()
    //                .then()
    //                .statusCode(Response.Status.OK.getStatusCode())
    //                .contentType(APPLICATION_JSON)
    //                .extract().as(GetPermissionsResponseDTO.class);
    //        Assertions.assertNotNull(output);
    //    }
}
