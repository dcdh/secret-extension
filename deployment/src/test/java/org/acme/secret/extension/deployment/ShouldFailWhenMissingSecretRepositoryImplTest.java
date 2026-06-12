package org.acme.secret.extension.deployment;

import io.quarkus.test.QuarkusExtensionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class ShouldFailWhenMissingSecretRepositoryImplTest {

    @RegisterExtension
    static QuarkusExtensionTest runner = new QuarkusExtensionTest()
            .withEmptyApplication()
            .assertException(throwable -> assertThat(throwable)
                    .hasNoSuppressedExceptions()
                    .rootCause()
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage("No secret repository found - please add io.quarkiverse.vault:quarkus-vault or io.quarkus:quarkus-jdbc-postgresql dependency")
                    .hasNoSuppressedExceptions());

    @Test
    void test() {
        Assertions.fail("Startup should have failed");
    }
}
