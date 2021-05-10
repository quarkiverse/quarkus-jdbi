package org.jdbi.quarkus.deployment;

import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class JdbiQuarkusProcessor {

    private static final String FEATURE = "jdbi-quarkus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexExternalDependency() {
        return new IndexDependencyBuildItem("org.jdbi", "jdbi3-core");
    }

    @BuildStep
    void registerForReflectionAllJdbiConfigImplementations(CombinedIndexBuildItem index, BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {

        DotName jdbiConfig = DotName.createSimple("org.jdbi.v3.core.config.JdbiConfig");

        index.getIndex().getAllKnownImplementors(jdbiConfig)
            .forEach(info -> {
                reflectionClasses.produce(new ReflectiveClassBuildItem(true, true, false, info.name().toString()));
            });

        //TODO get all classes that use method handles, such as CollectionCollectors, and register for reflection the target class for the reflective operation, Collectors in the case of the example
        //maybe there is another way to address the various reflection uses by appliying substitutions, I don't know

    }

}
