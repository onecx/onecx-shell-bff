package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.shell.bff.rs.controllers.PermissionRestController;

import gen.org.tkit.onecx.permission.model.ApplicationPermissions;
import gen.org.tkit.onecx.permission.model.PermissionRequest;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetPermissionsResponseDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(PermissionRestController.class)
public class PermissionRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getPermissionsTest() {

        ApplicationPermissions applicationPermissions = new ApplicationPermissions();
        Map<String, Set<String>> permissions = new HashMap<>();
        permissions.put("workspaceConfig", Set.of("read", "write", "delete"));
        permissions.put("userProfile", Set.of("read", "write", "delete"));
        permissions.put("permission", Set.of("read"));
        permissions.put("permissions", Set.of("admin-write", "admin-read"));
        applicationPermissions.setPermissions(permissions);
        applicationPermissions.setAppId("onecx-shell-bff");
        applicationPermissions.setProductName("onecx-shell");

        String AUTHTOKEN = keycloakClient.getAccessToken(ADMIN);
        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.setToken("Bearer " + AUTHTOKEN);
        // create mock rest endpoint for permission svc
        mockServerClient
                .when(request().withPath("/v1/permissions/user/applications/onecx-shell-bff").withMethod(HttpMethod.POST)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(permissionRequest)))
                .withId("mockPermission")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(applicationPermissions)));

        // create mock rest endpoint for permission svc
        mockServerClient.when(request().withPath("/v1/permissions/user/product1/app1").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(permissionRequest)))
                .withId("mockPermission2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(applicationPermissions)));

        GetPermissionsRequestDTO requestDTO = new GetPermissionsRequestDTO();
        requestDTO.setAppId("app1");
        requestDTO.setProductName("product1");
        var output = given()
                .when()
                .auth().oauth2(AUTHTOKEN)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetPermissionsResponseDTO.class);
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.getPermissions().containsAll(
                List.of("permissions#admin-write", "permissions#admin-read", "permission#read",
                        "workspaceConfig#read", "workspaceConfig#delete", "workspaceConfig#write",
                        "workspaceConfig#write", "userProfile#read", "userProfile#delete", "userProfile#write")));
    }
}
