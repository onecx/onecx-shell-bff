package org.tkit.onecx.shell.bff.rs;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Shell bff configuration
 */
@ConfigDocFilename("onecx-shell-bff.adoc")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.shell")
public interface ShellConfig {

    /**
     * permission configurations
     */
    @WithName("permissions")
    PermissionConfig permissions();

    interface PermissionConfig {
        /**
         * Enable or disable caching
         */
        @WithDefault("true")
        @WithName("cache-enabled")
        boolean cachingEnabled();

        /**
         * select default key separator
         */
        @WithDefault("#")
        @WithName("key-separator")
        String keySeparator();
    }
}
