package org.jdbi.quarkus.deployment;

import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;

class JdbiQuarkusProcessor {

    private static final String FEATURE = "quarkus-jdbi";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexExternalDependency() {
        return new IndexDependencyBuildItem("org.jdbi", "jdbi3-core");
    }

    @BuildStep
    ReflectiveClassBuildItem reflectionConstructorOnly() {
        // since we only need reflection to the constructor of the class, we can specify `false` for both the methods and the fields arguments.
        return new ReflectiveClassBuildItem(false, false,
                "org.jdbi.v3.core.transaction.SerializableTransactionRunner$Configuration",
                "org.jdbi.v3.core.config.JdbiCaches",
                "org.jdbi.v3.core.statement.SqlStatements",
                "org.jdbi.v3.core.statement.StatementExceptions",
                "org.jdbi.v3.core.argument.Arguments",
                "org.jdbi.v3.core.mapper.RowMappers",
                "org.jdbi.v3.core.mapper.ColumnMappers",
                "org.jdbi.v3.core.mapper.Mappers",
                "org.jdbi.v3.core.mapper.MapMappers",
                "org.jdbi.v3.core.mapper.MapEntryMappers",
                "org.jdbi.v3.core.mapper.reflect.ReflectionMappers",
                "org.jdbi.v3.core.mapper.reflect.internal.PojoTypes",
                "org.jdbi.v3.core.collector.JdbiCollectors",
                "org.jdbi.v3.core.qualifier.Qualifiers",
                "org.jdbi.v3.core.result.ResultProducers",
                "org.jdbi.v3.core.array.SqlArrayTypes",
                "org.jdbi.v3.core.extension.Extensions",
                "org.jdbi.v3.core.internal.OnDemandExtensions",
                "org.jdbi.v3.core.internal.EnumStrategies",
                "org.jdbi.v3.core.enums.Enums",
                "org.jdbi.v3.core.Handles",
                "org.jdbi.v3.sqlobject.Handlers",
                "org.jdbi.v3.sqlobject.HandlerDecorators",
                "org.jdbi.v3.sqlobject.statement.internal.SqlObjectStatementConfiguration",
                "org.jdbi.v3.sqlobject.SqlObjects",
                "com.github.benmanes.caffeine.cache.PSMS",
                "com.github.benmanes.caffeine.cache.SSMS",
                "org.jdbi.v3.postgres.PostgresTypes");
    }

    @BuildStep
    ReflectiveClassBuildItem reflectionMethods() {
        return new ReflectiveClassBuildItem(true, false,
                "com.github.benmanes.caffeine.cache.CacheLoader",
                "org.jdbi.v3.sqlobject.SqlObject");
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
