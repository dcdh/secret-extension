package org.acme.secret.extension.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.acme.secret.extension.runtime.JdbcPostgresSecretRepository;
import org.acme.secret.extension.runtime.VaultSecretRepository;

class SecretExtensionProcessor {

    private static final String FEATURE = "secret-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeanBuildItem(final BuildProducer<ValidationPhaseBuildItem.ValidationErrorBuildItem> validationErrorBuildItemBuildProducer) {
        final AdditionalBeanBuildItem.Builder builder = AdditionalBeanBuildItem.builder();
        if (QuarkusClassLoader.isClassPresentAtRuntime("io.quarkus.vault.VaultKVSecretEngine")) {
            builder.addBeanClass(VaultSecretRepository.class);
        } else if (QuarkusClassLoader.isClassPresentAtRuntime("io.quarkus.jdbc.postgresql.runtime.PostgreSQLAgroalConnectionConfigurer")) {
            builder.addBeanClass(JdbcPostgresSecretRepository.class);
        } else {
            validationErrorBuildItemBuildProducer.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(
                    new IllegalStateException("No secret repository found - please add io.quarkiverse.vault:quarkus-vault or io.quarkus:quarkus-jdbc-postgresql dependency")));
        }
        return builder.build();
    }
}
