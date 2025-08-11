package org.jdbi.quarkus.deployment;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalApplicationArchiveMarkerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

class JdbiQuarkusProcessor {

    private static final String FEATURE = "quarkus-jdbi";
    private static final String PLUGIN = "org.jdbi.v3.core.spi.JdbiPlugin";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexExternalDependency(
            BuildProducer<AdditionalApplicationArchiveMarkerBuildItem> markers) {
        markers.produce(new AdditionalApplicationArchiveMarkerBuildItem("META-INF/services/org.jdbi.v3.core.spi.JdbiPlugin"));

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
                "org.jdbi.v3.core.mapper.immutables.JdbiImmutables",
                "org.jdbi.v3.core.mapper.reflect.internal.PojoTypes",
                "org.jdbi.v3.core.collector.JdbiCollectors",                            
                "org.jdbi.v3.core.mapper.freebuilder.JdbiFreeBuilders",
                "org.jdbi.v3.core.qualifier.Qualifiers",
                "org.jdbi.v3.core.result.ResultProducers",
                "org.jdbi.v3.core.array.SqlArrayTypes",
                "org.jdbi.v3.core.extension.Extensions",
                "org.jdbi.v3.core.internal.OnDemandExtensions",
                "org.jdbi.v3.core.internal.EnumStrategies",
                "org.jdbi.v3.sqlobject.transaction.internal.TransactionDecorator",
                "org.jdbi.v3.core.config.internal.ConfigCaches",
                "org.jdbi.v3.core.enums.Enums",
                "org.jdbi.v3.core.Handles",
                "org.jdbi.v3.sqlobject.Handlers",
                "org.jdbi.v3.sqlobject.HandlerDecorators",
                "org.jdbi.v3.sqlobject.statement.internal.SqlObjectStatementConfiguration",
                "org.jdbi.v3.sqlobject.customizer.TimestampedConfig",
                "org.jdbi.v3.sqlobject.SqlObjects",
                "com.github.benmanes.caffeine.cache.PSMS",
                "org.jdbi.v3.postgres.PostgresTypes");
    }

    @BuildStep
    ReflectiveClassBuildItem reflectionMethods() {
        return new ReflectiveClassBuildItem(true, false,
                "com.github.benmanes.caffeine.cache.CacheLoader",
                "com.github.benmanes.caffeine.cache.SSMS",
                "org.jdbi.v3.sqlobject.SqlObject");
    }

    @BuildStep
    void registerForReflectionAllJdbiConfigImplementations(
            CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {

        DotName jdbiConfig = DotName.createSimple("org.jdbi.v3.core.config.JdbiConfig");
        index.getIndex().getAllKnownImplementors(jdbiConfig)
                .forEach(info -> {
                    reflectionClasses.produce(new ReflectiveClassBuildItem(true, true, false, info.name().toString()));
                });
    }

    @BuildStep
    void registerForReflectionAllJdbiPluginImplementations(CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses,
            BuildProducer<ServiceProviderBuildItem> serviceProviders) {

        DotName plugin = DotName.createSimple(PLUGIN);

        Collection<String> plugins = new ArrayList<>();
        index.getIndex().getAllKnownImplementors(plugin)
                .forEach(info -> {
                    String pluginName = info.name().toString();
                    plugins.add(pluginName);
                    reflectionClasses.produce(new ReflectiveClassBuildItem(true, true, false, pluginName));
                });

        serviceProviders.produce(new ServiceProviderBuildItem(PLUGIN, plugins));
    }

}
