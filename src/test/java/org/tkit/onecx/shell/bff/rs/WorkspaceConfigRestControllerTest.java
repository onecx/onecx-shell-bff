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
import org.tkit.onecx.shell.bff.rs.controllers.WorkspaceConfigRestController;

import gen.org.tkit.onecx.product.store.client.model.MicrofrontendPSV1;
import gen.org.tkit.onecx.product.store.client.model.ProductPSV1;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetWorkspaceConfigRequestDTO;
import gen.org.tkit.onecx.shell.bff.rs.internal.model.GetWorkspaceConfigResponseDTO;
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
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setBaseUrl("/w1Url");
        criteria.setPageNumber(0);
        criteria.setPageSize(1);
        WorkspacePageResult pageResult = new WorkspacePageResult();
        pageResult.setStream(List.of(new WorkspaceAbstract().name("w1").theme("theme1").products(List.of("p1"))));

        // create mock rest endpoint for workspace search
        mockServerClient.when(request().withPath("/v1/workspaces/search").withMethod(HttpMethod.POST)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(JsonBody.json(criteria)))
                .withId("mockWS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

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
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(themeResponse)));

        var input = new GetWorkspaceConfigRequestDTO();
        input.setBaseUrl("/w1Url");

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
    }
}
