package org.tkit.onecx.shell.bff.rs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.permission")
public interface PermissionConfig {
    @WithName("caching.enabled")
    boolean cachingEnabled();

    @WithName("default.separator")
    String keySeparator();
}
