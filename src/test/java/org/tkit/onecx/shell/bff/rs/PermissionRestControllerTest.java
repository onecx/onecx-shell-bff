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
import gen.org.tkit.onecx.shell.bff.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(PermissionRestController.class)
class PermissionRestControllerTest extends AbstractTest {

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

        String token = keycloakClient.getAccessToken(ADMIN);
        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.setToken("Bearer " + token);
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
                .withHeader(APM_HEADER_PARAM, ADMIN)
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
                .auth().oauth2(token)
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

        mockServerClient.clear("mockPermission2");
        mockServerClient.clear("mockPermission");
    }

    @Test
    void getPermissionsResponsesTest() {

        String token = keycloakClient.getAccessToken(ADMIN);
        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.setToken("Bearer " + token);

        // return empty list of permissions
        mockServerClient.when(request().withPath("/v1/permissions/user/product1/app2").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withHeader(APM_HEADER_PARAM, ADMIN))
                .withId("test10")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new ApplicationPermissions())));

        var output2 = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new GetPermissionsRequestDTO().productName("product1").appId("app2"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetPermissionsResponseDTO.class);
        Assertions.assertNotNull(output2);

        // return empty action list of permissions
        mockServerClient.when(request().withPath("/v1/permissions/user/product1/app3").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withHeader(APM_HEADER_PARAM, ADMIN))
                .withId("test11")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new ApplicationPermissions().permissions(new HashMap<>()))));

        var output3 = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new GetPermissionsRequestDTO().productName("product1").appId("app3"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetPermissionsResponseDTO.class);
        Assertions.assertNotNull(output3);

        // return empty action list of permissions
        Map<String, Set<String>> actions = new HashMap<>();
        actions.put("action1", null);
        actions.put("action2", new HashSet<>());
        mockServerClient.when(request().withPath("/v1/permissions/user/product1/app4").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withHeader(APM_HEADER_PARAM, ADMIN))
                .withId("test12")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(new ApplicationPermissions().permissions(actions))));

        var output4 = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new GetPermissionsRequestDTO().productName("product1").appId("app4"))
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetPermissionsResponseDTO.class);
        Assertions.assertNotNull(output4);

        mockServerClient.clear("test10");
        mockServerClient.clear("test11");
        mockServerClient.clear("test12");
    }

    @Test
    void getPermissions_missing_input_Test() {
        String token = keycloakClient.getAccessToken(ADMIN);

        GetPermissionsRequestDTO requestDTO = new GetPermissionsRequestDTO();
        var output = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);
        Assertions.assertNotNull(output);
    }

    @Test
    void getPermissions_svc_error_Test() {

        ApplicationPermissions applicationPermissions = new ApplicationPermissions();
        Map<String, Set<String>> permissions = new HashMap<>();
        permissions.put("workspaceConfig", Set.of("read", "write", "delete"));
        permissions.put("userProfile", Set.of("read", "write", "delete"));
        permissions.put("permission", Set.of("read"));
        permissions.put("permissions", Set.of("admin-write", "admin-read"));
        applicationPermissions.setPermissions(permissions);
        applicationPermissions.setAppId("onecx-shell-bff");
        applicationPermissions.setProductName("onecx-shell");

        String token = keycloakClient.getAccessToken(ADMIN);
        PermissionRequest permissionRequest = new PermissionRequest();
        permissionRequest.setToken("Bearer " + token);
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
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode()));

        GetPermissionsRequestDTO requestDTO = new GetPermissionsRequestDTO();
        requestDTO.setAppId("app1");
        requestDTO.setProductName("product1");
        var output = given()
                .when()
                .auth().oauth2(token)
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        Assertions.assertNotNull(output);

        mockServerClient.clear("mockPermission2");
        mockServerClient.clear("mockPermission");

    }
}
