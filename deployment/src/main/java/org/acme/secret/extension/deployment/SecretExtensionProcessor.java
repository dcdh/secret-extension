package org.acme.secret.extension.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.acme.secret.extension.runtime.InMemorySecretRepository;
import org.acme.secret.extension.runtime.VaultSecretRepository;

class SecretExtensionProcessor {

    private static final String FEATURE = "secret-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeanBuildItem() {
        final AdditionalBeanBuildItem.Builder builder = AdditionalBeanBuildItem.builder();
        if (QuarkusClassLoader.isClassPresentAtRuntime("io.quarkus.vault.VaultKVSecretEngine")) {
            builder.addBeanClass(VaultSecretRepository.class);
        } else {
            builder.addBeanClass(InMemorySecretRepository.class);
        }
        return builder.build();
    }
}
