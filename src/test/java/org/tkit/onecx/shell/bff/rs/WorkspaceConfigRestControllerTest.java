package org.tkit.onecx.shell.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.shell.bff.rs.controllers.WorkspaceConfigRestController;

import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.theme.client.model.Theme;
import gen.org.tkit.onecx.workspace.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceConfigRestController.class)
class WorkspaceConfigRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getWorkspaceConfigByBaseUrlTest() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");
        Workspace workspace = new Workspace();
        workspace.name("w1").theme("theme1");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspace)));

        WorkspaceLoad loadResponse = new WorkspaceLoad();
        loadResponse.setName("w1");
        Product product1 = new Product();
        product1.baseUrl("/product1").productName("product1").microfrontends(List.of(
                new Microfrontend().basePath("/app1").mfeId("app1")));
        loadResponse.setProducts(List.of(product1));

        // create mock rest endpoint for load workspace by name
        mockServerClient.when(request().withPath("/v1/workspaces/w1/load").withMethod(HttpMethod.GET))
                .withId("mockWSLoad")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(loadResponse)));

        ProductPSV1 productResponse = new ProductPSV1();
        productResponse.basePath("/product1").name("product1").microfrontends(List.of(
                new MicrofrontendPSV1().exposedModule("App1Module")
                        .appName("app1")
                        .remoteBaseUrl("/remoteBaseUrl")
                        .remoteEntry("/remoteEntry.js")
                        .technology("ANGULAR")
                        .type(MicrofrontendTypePSV1.MODULE)));
        // create mock rest endpoint for get product by name from product-store
        mockServerClient.when(request().withPath("/v1/products/product1").withMethod(HttpMethod.GET))
                .withId("mockPS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(productResponse)));

        Theme themeResponse = new Theme();
        themeResponse.name("theme1").cssFile("cssfile").properties(new Object()).logoUrl("someLogoUrl")
                .faviconUrl("someFavIconUrl");
        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1").withMethod(HttpMethod.GET))
                .withId("mockTheme")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(themeResponse)));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceConfigResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("w1", output.getWorkspace().getName());
        Assertions.assertEquals("theme1", output.getTheme().getName());
        Assertions.assertEquals(1, output.getRoutes().size());

        //CHECK FOR MOCKED REMOTE COMPONENTS
        //SHOULD BE REMOVED AFTER IMPLEMENTATION
        Assertions.assertEquals("PortalMenu", output.getRemoteComponents().get(0).getName());
        Assertions.assertEquals("appId", output.getRemoteComponents().get(0).getAppId());
        Assertions.assertEquals("menu", output.getShellRemoteComponents().get(0).getSlotName());

        mockServerClient.clear("mockWS");
        mockServerClient.clear("mockPS");
        mockServerClient.clear("mockTheme");
        mockServerClient.clear("mockWSLoad");
    }

    @Test
    void getWorkspaceConfigByBaseUrlTest_emptyImageUrl_should_create_urls_test() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");
        Workspace workspace = new Workspace();
        workspace.name("w1").theme("theme1");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspace)));

        WorkspaceLoad loadResponse = new WorkspaceLoad();
        loadResponse.setName("w1");
        Product product1 = new Product();
        product1.baseUrl("/product1").productName("product1").microfrontends(List.of(
                new Microfrontend().basePath("/app1").mfeId("app1")));
        loadResponse.setProducts(List.of(product1));

        // create mock rest endpoint for load workspace by name
        mockServerClient.when(request().withPath("/v1/workspaces/w1/load").withMethod(HttpMethod.GET))
                .withId("mockWSLoad")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(loadResponse)));

        ProductPSV1 productResponse = new ProductPSV1();
        productResponse.basePath("/product1").name("product1").microfrontends(List.of(
                new MicrofrontendPSV1().exposedModule("App1Module")
                        .appName("app1")
                        .remoteBaseUrl("/remoteBaseUrl")
                        .remoteEntry("/remoteEntry.js")
                        .technology("ANGULAR")
                        .type(MicrofrontendTypePSV1.MODULE)));
        // create mock rest endpoint for get product by name from product-store
        mockServerClient.when(request().withPath("/v1/products/product1").withMethod(HttpMethod.GET))
                .withId("mockPS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(productResponse)));

        Theme themeResponse = new Theme();
        themeResponse.name("theme1").cssFile("cssfile").properties(new Object());
        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1").withMethod(HttpMethod.GET))
                .withId("mockTheme")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(themeResponse)));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceConfigResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("w1", output.getWorkspace().getName());
        Assertions.assertEquals("theme1", output.getTheme().getName());
        Assertions.assertEquals(1, output.getRoutes().size());
        Assertions.assertNotNull(output.getTheme().getFaviconUrl());
        Assertions.assertNotNull(output.getTheme().getLogoUrl());

        //CHECK FOR MOCKED REMOTE COMPONENTS
        //SHOULD BE REMOVED AFTER IMPLEMENTATION
        Assertions.assertEquals("PortalMenu", output.getRemoteComponents().get(0).getName());
        Assertions.assertEquals("appId", output.getRemoteComponents().get(0).getAppId());
        Assertions.assertEquals("menu", output.getShellRemoteComponents().get(0).getSlotName());

        mockServerClient.clear("mockWS");
        mockServerClient.clear("mockPS");
        mockServerClient.clear("mockTheme");
        mockServerClient.clear("mockWSLoad");
    }

    @Test
    void getWorkspaceConfig_MissingBaseUrlTest() {
        var input = new GetWorkspaceConfigRequestDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void getWorkspaceConfig_WorkspaceNotFoundTest() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
        mockServerClient.clear("mockWS");
    }

    @Test
    void getWorkspaceConfig_searchWorkspace_Bad_Request_Test() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
        Assertions.assertNotNull(output);
        mockServerClient.clear("mockWS");
    }

    @Test
    void getWorkspaceConfig_ProductNotFoundTest() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");
        Workspace workspace = new Workspace();
        workspace.name("w1").theme("theme1");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspace)));

        WorkspaceLoad loadResponse = new WorkspaceLoad();
        loadResponse.setName("w1");
        Product product1 = new Product();
        product1.baseUrl("/product1").productName("product1").microfrontends(List.of(
                new Microfrontend().basePath("/app1").mfeId("app1")));
        loadResponse.setProducts(List.of(product1));

        // create mock rest endpoint for load workspace by name
        mockServerClient.when(request().withPath("/v1/workspaces/w1/load").withMethod(HttpMethod.GET))
                .withId("mockWSLoad")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(loadResponse)));

        // create mock rest endpoint for get product by name from product-store NOT-FOUND
        mockServerClient.when(request().withPath("/v1/products/product1").withMethod(HttpMethod.GET))
                .withId("mockPS")
                .respond(httpRequest -> response().withStatusCode(NOT_FOUND.getStatusCode()));

        Theme themeResponse = new Theme();
        themeResponse.name("theme1").cssFile("cssfile").properties(new Object());
        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1").withMethod(HttpMethod.GET))
                .withId("mockTheme")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(themeResponse)));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceConfigResponseDTO.class);

        Assertions.assertNotNull(output);

        mockServerClient.clear("mockWS");
        mockServerClient.clear("mockPS");
        mockServerClient.clear("mockWSLoad");
        mockServerClient.clear("mockTheme");

    }

    @Test
    void getWorkspaceConfig_searchTheme_throws_BAD_REQUEST_Test() {
        GetWorkspaceByUrlRequest byUrlRequest = new GetWorkspaceByUrlRequest();
        byUrlRequest.setUrl("/w1Url");
        Workspace workspace = new Workspace();
        workspace.name("w1").theme("theme1");

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/byUrl").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(byUrlRequest)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspace)));

        WorkspaceLoad loadResponse = new WorkspaceLoad();
        loadResponse.setName("w1");
        Product product1 = new Product();
        product1.baseUrl("/product1").productName("product1").microfrontends(List.of(
                new Microfrontend().basePath("/app1").mfeId("app1")));
        loadResponse.setProducts(List.of(product1));

        // create mock rest endpoint for load workspace by name
        mockServerClient.when(request().withPath("/v1/workspaces/w1/load").withMethod(HttpMethod.GET))
                .withId("mockWSLoad")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(loadResponse)));

        ProductPSV1 productResponse = new ProductPSV1();
        productResponse.basePath("/product1").name("product1").microfrontends(List.of(
                new MicrofrontendPSV1().exposedModule("App1Module")
                        .appName("app1")
                        .remoteBaseUrl("/remoteBaseUrl")
                        .remoteEntry("/remoteEntry.js")
                        .technology("ANGULAR")));
        // create mock rest endpoint for get product by name from product-store
        mockServerClient.when(request().withPath("/v1/products/product1").withMethod(HttpMethod.GET))
                .withId("mockPS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(productResponse)));

        Theme themeResponse = new Theme();
        themeResponse.name("theme1").cssFile("cssfile").properties(new Object());
        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1").withMethod(HttpMethod.GET))
                .withId("mockTheme")
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode()));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setUrl("/w1Url");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);

        mockServerClient.clear("mockWS");
        mockServerClient.clear("mockPS");
        mockServerClient.clear("mockTheme");
        mockServerClient.clear("mockWSLoad");
    }

    @Test
    void getThemeFaviconTest() {

        byte[] bytesRes = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d };

        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1/favicon").withMethod(HttpMethod.GET))
                .withId("mockFavicon")
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, "image/png"))
                        .withBody(bytesRes));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/favicon")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .extract().body().asByteArray();

        Assertions.assertNotNull(output);
        mockServerClient.clear("mockFavicon");

    }

    @Test
    void getThemeLogoTest() {

        byte[] bytesRes = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d };

        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1/logo").withMethod(HttpMethod.GET))
                .withId("mockLogo")
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, "image/png"))
                        .withBody(bytesRes));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/logo")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .extract().body().asByteArray();

        Assertions.assertNotNull(output);
        mockServerClient.clear("mockLogo");

    }

    @Test
    void getThemeFaviconAndLogo_shouldReturnBadRequest_whenBodyEmpty() {

        byte[] bytesRes = null;

        mockServerClient.when(request().withPath("/v1/themes/theme1/favicon").withMethod(HttpMethod.GET))
                .withId("mockFavicon")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, "image/png"))
                        .withBody(bytesRes));

        mockServerClient.when(request().withPath("/v1/themes/theme1/logo").withMethod(HttpMethod.GET))
                .withId("mockLogo")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withHeaders(
                                new Header(HttpHeaders.CONTENT_TYPE, "image/png"))
                        .withBody(bytesRes));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/favicon")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/logo")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        mockServerClient.clear("mockFavicon");
        mockServerClient.clear("mockLogo");
    }

    @Test
    void getThemeFaviconAndLogo_shouldReturnBadRequest_whenContentTypeEmpty() {

        byte[] bytesRes = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d };

        mockServerClient.when(request().withPath("/v1/themes/theme1/favicon").withMethod(HttpMethod.GET))
                .withId("mockFavicon")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withBody(bytesRes));
        mockServerClient.when(request().withPath("/v1/themes/theme1/logo").withMethod(HttpMethod.GET))
                .withId("mockLogo")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode())
                        .withBody(bytesRes));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/favicon")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/logo")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        mockServerClient.clear("mockFavicon");
        mockServerClient.clear("mockLogo");

    }

    @Test
    void getImage_shouldReturnBadRequest_whenAllEmpty() {

        mockServerClient.when(request().withPath("/v1/themes/theme1/favicon").withMethod(HttpMethod.GET))
                .withId("mockFavicon")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode()));

        mockServerClient.when(request().withPath("/v1/themes/theme1/logo").withMethod(HttpMethod.GET))
                .withId("mockLogo")
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(OK.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/favicon")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("name", "theme1")
                .get("/themes/{name}/logo")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        mockServerClient.clear("mockFavicon");
        mockServerClient.clear("mockLogo");
    }

    @Test
    void loadWorkspaceConfigByBaseUrlTest() {

        var workspace = new WorkspaceWrapper();
        workspace.name("w1").theme("theme1")
                .addProductsItem(
                        new Product().productName("product1").baseUrl("/product1")
                                .addMicrofrontendsItem(new Microfrontend().basePath("/app1").mfeId("app1")))
                .addSlotsItem(
                        new WorkspaceWrapperSlot().name("slot1")
                                .addComponentsItem(
                                        new WorkspaceWrapperComponent().productName("product1").appId("app1")
                                                .name("App1Component"))
                                .addComponentsItem(
                                        new WorkspaceWrapperComponent().productName("product1").appId("app1")
                                                .name("App2Component"))
                                .addComponentsItem(
                                        new WorkspaceWrapperComponent().productName("product1").appId("app1")
                                                .name("App3Component")));

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/load").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(new WorkspaceLoadRequest().path("/w1Url"))))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspace)));

        var productResponse = new LoadProductResponsePSV1();
        productResponse.addProductsItem(new LoadProductItemPSV1().name("product1")
                .addMicrofrontendsItem(new LoadProductMicrofrontendPSV1()
                        .exposedModule("App1Module")
                        .appId("app1")
                        .remoteBaseUrl("/remoteBaseUrl")
                        .remoteEntry("/remoteEntry.js")
                        .technology("ANGULAR")
                        .type(MicrofrontendTypePSV1.MODULE))
                .addMicrofrontendsItem(new LoadProductMicrofrontendPSV1()
                        .exposedModule("App2Component")
                        .appId("app1")
                        .remoteBaseUrl("/remoteBaseUrl")
                        .remoteEntry("/remoteEntry.js")
                        .technology("ANGULAR")
                        .type(MicrofrontendTypePSV1.COMPONENT)));

        // create mock rest endpoint for get product by name from product-store
        mockServerClient.when(request().withPath("/v1/products/load/shell").withMethod(HttpMethod.POST))
                .withId("mockPS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(productResponse)));

        Theme themeResponse = new Theme();
        themeResponse.name("theme1").cssFile("cssfile").properties(new Object()).logoUrl("someLogoUrl")
                .faviconUrl("someFavIconUrl");
        // create mock rest endpoint for get theme by name from theme-svc
        mockServerClient.when(request().withPath("/v1/themes/theme1").withMethod(HttpMethod.GET))
                .withId("mockTheme")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(themeResponse)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new LoadWorkspaceConfigRequestDTO().path("/w1Url"))
                .post("load")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(LoadWorkspaceConfigResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("w1", output.getWorkspace().getName());
        Assertions.assertEquals("theme1", output.getTheme().getName());
        Assertions.assertEquals(1, output.getRoutes().size());

        Assertions.assertEquals("product1#app1#App2Component", output.getComponents().get(0).getName());
        Assertions.assertEquals("app1", output.getComponents().get(0).getAppId());
        Assertions.assertEquals("slot1", output.getSlots().get(0).getName());

        mockServerClient.clear("mockWS");
        mockServerClient.clear("mockPS");
        mockServerClient.clear("mockTheme");
        mockServerClient.clear("mockWSLoad");
    }

    @Test
    void loadWorkspaceConfig_MissingBaseUrlTest() {
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new LoadWorkspaceConfigRequestDTO())
                .post("load")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void loadWorkspaceConfig_WorkspaceNotFoundTest() {

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/load").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(new WorkspaceLoadRequest().path("/w1Url"))))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new LoadWorkspaceConfigRequestDTO().path("/w1Url"))
                .post("load")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
        mockServerClient.clear("mockWS");
    }

}
