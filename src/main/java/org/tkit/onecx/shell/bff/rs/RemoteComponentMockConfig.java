package org.tkit.onecx.shell.bff.rs;

import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.component.mock")
public interface RemoteComponentMockConfig {

    @WithName("names")
    List<String> names();

    @WithName("base-url")
    Map<String, String> baseUrl();

    @WithName("app-id")
    Map<String, String> appId();

    @WithName("product-name")
    Map<String, String> productName();

    @WithName("remote-base-url")
    Map<String, String> remoteBaseUrl();

    @WithName("remote-entry-url")
    Map<String, String> remoteEntryUrl();

    @WithName("slot")
    Map<String, String> slot();

    @WithName("exposed-module")
    Map<String, String> exposedModule();

}
