package org.acme.secret.extension.runtime;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "secret")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SecretConfiguration {

    /**
     * Postgres configuration.
     */
    Postgres postgres();

    @ConfigGroup
    interface Postgres {

        /**
         * Master key
         */
        Optional<String> masterKey();
    }
}
