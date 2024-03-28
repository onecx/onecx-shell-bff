package org.tkit.onecx.shell.bff.rs;

import java.util.List;
import java.util.Map;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "onecx.component.mock")
public interface RemoteComponentMockConfig {

    @WithName("keys")
    List<String> keys();

    @WithName("baseurl")
    Map<String, String> baseUrl();

    @WithName("name")
    Map<String, String> name();

    @WithName("appid")
    Map<String, String> appId();

    @WithName("productname")
    Map<String, String> productName();

    @WithName("remotebaseurl")
    Map<String, String> remoteBaseUrl();

    @WithName("remoteentryurl")
    Map<String, String> remoteEntryUrl();

    @WithName("slot")
    Map<String, String> slot();

    @WithName("exposedmodule")
    Map<String, String> exposedModule();

}
