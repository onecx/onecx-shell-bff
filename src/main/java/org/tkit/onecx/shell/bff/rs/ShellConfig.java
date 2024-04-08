package org.tkit.onecx.shell.bff.rs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.shell")
public interface ShellConfig {

    @WithName("permissions")
    PermissionConfig permissions();

    interface PermissionConfig {
        @WithDefault("true")
        @WithName("cache-enabled")
        boolean cachingEnabled();

        @WithDefault("#")
        @WithName("key-separator")
        String keySeparator();
    }
}
