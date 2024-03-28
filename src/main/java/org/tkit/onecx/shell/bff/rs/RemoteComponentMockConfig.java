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

    @WithName("baseUrl")
    Map<String, String> baseUrl();

    @WithName("appId")
    Map<String, String> appId();

    @WithName("productName")
    Map<String, String> productName();

    @WithName("remoteBaseUrl")
    Map<String, String> remoteBaseUrl();

    @WithName("remoteEntryUrl")
    Map<String, String> remoteEntryUrl();

    @WithName("slot")
    Map<String, String> slot();

    @WithName("exposedModule")
    Map<String, String> exposedModule();

}
