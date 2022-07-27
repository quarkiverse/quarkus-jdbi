package org.jdbi.quarkus.deployment;

import com.google.common.collect.HashMultimap;
import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;

class JdbiAnnotationsQuarkusProcessor {

    private static final HashMultimap<DotName, String> annotationToHandlers = HashMultimap.create();
    private static Set<DotName> proxyTriggers = new HashSet<>();
    private static final Set<DotName> registerAnnotations = new HashSet<>();

    static {
        addConfig("RegisterArgumentFactories", true);
        addConfig("RegisterArgumentFactory", true);
        addConfig("RegisterBeanMapper", true);
        addConfig("RegisterBeanMappers", true);
        addConfig("RegisterCollectorFactory", true);
        addConfig("RegisterColumnMapper", true);
        addConfig("RegisterColumnMapperFactories", true);
        addConfig("RegisterColumnMapperFactory", true);
        addConfig("RegisterColumnMappers", true);
        addConfig("RegisterConstructorMapper", true);
        addConfig("RegisterConstructorMappers", true);
        addConfig("RegisterFieldMapper", true);
        addConfig("RegisterFieldMappers", true);
        addConfig("RegisterJoinRowMapper", true);
        addConfig("RegisterObjectArgumentFactories", true);
        addConfig("RegisterObjectArgumentFactory", true);
        addConfig("RegisterRowMapper", true);
        addConfig("RegisterRowMapperFactories", true);
        addConfig("RegisterRowMapperFactory", true);
        addConfig("RegisterRowMappers", true);

        addConfig("UseEnumStrategy", false);
        addConfig("UseSqlParser", true);
        addConfig("UseTemplateEngine", true);

        addConfig("KeyColumn", false);
        addConfig("ValueColumn", false);

        addLocator("UseAnnotationSqlLocator");
        addLocator("UseClasspathSqlLocator");

        addHandler("org.jdbi.v3.sqlobject.statement.MapTo",
                "org.jdbi.v3.sqlobject.statement.internal.MapToFactory");
        addStmtHandler("SqlBatch");
        addStmtHandler("SqlCall");
        addStmtHandler("SqlQuery");
        addStmtHandler("SqlScripts");
        addStmtHandler("SqlUpdate");

        addHandler("org.jdbi.v3.sqlobject.CreateSqlObject",
                "org.jdbi.v3.sqlobject.internal.CreateSqlObjectHandler");
        addProxyTrigger("org.jdbi.v3.sqlobject.CreateSqlObject");

        addClassReg("org.jdbi.v3.sqlobject.statement.UseRowMapper");
        addClassReg("org.jdbi.v3.sqlobject.statement.UseRowReducer");
        addProxyTrigger("org.jdbi.v3.sqlobject.statement.UseRowMapper");
        addProxyTrigger("org.jdbi.v3.sqlobject.statement.UseRowReducer");

        addCustomizer("AllowUnusedBindings");
        addCustomizer("Bind");
        addCustomizer("BindBean");
        addCustomizer("BindBeanList");
        addCustomizer("BindFields");
        addCustomizer("BindList");
        addCustomizer("BindMap");
        addCustomizer("BindMethods");
        addCustomizer("BindMethodsList");
        addCustomizer("BindPojo");
        addCustomizer("Define");
        addCustomizer("DefineList");
        addCustomizer("DefineNamedBindings");
        addCustomizer("FetchSize");
        addCustomizer("MaxRows");
        addCustomizer("OutParameter");
        addCustomizer("OutParameterList");
        addCustomizer("QueryTimeOut");
        addCustomizer("Timestamped");
        addHandler("org.jdbi.v3.sqlobject.customizer.Timestamped",
                "org.jdbi.v3.sqlobject.customizer.TimestampedConfig");
    }

    static void addConfig(String name, boolean register) {
        String triggerAnnotation = "org.jdbi.v3.sqlobject.config." + name;
        addHandler(triggerAnnotation, "org.jdbi.v3.sqlobject.config.internal." + name + "Impl");
        addClassReg(triggerAnnotation);
    }

    static void addLocator(String name) {
        String triggerAnnotation = "org.jdbi.v3.sqlobject.locator." + name;
        addHandler(triggerAnnotation,
                "org.jdbi.v3.sqlobject.locator.internal." + name + "Impl");
        addProxyTrigger(triggerAnnotation);
    }

    static void addStmtHandler(String name) {
        String triggerAnnotation = "org.jdbi.v3.sqlobject.statement." + name;

        addHandler(triggerAnnotation,
                "org.jdbi.v3.sqlobject.statement.internal." + name + "Handler");

        addProxyTrigger(triggerAnnotation);
    }

    static void addCustomizer(String name) {
        addHandler("org.jdbi.v3.sqlobject.customizer." + name,
                "org.jdbi.v3.sqlobject.customizer.internal." + name + "Factory");
    }

    static void addHandler(String triggerName, String handler) {
        annotationToHandlers.put(DotName.createSimple(triggerName), handler);
    }

    static void addClassReg(String triggerAnnotation) {
        registerAnnotations.add(DotName.createSimple(triggerAnnotation));
    }

    static void addProxyTrigger(String triggerAnnotation) {
        proxyTriggers.add(DotName.createSimple(triggerAnnotation));
    }

    @BuildStep
    void findRegistrations(CombinedIndexBuildItem index, BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {

        Consumer<AnnotationInstance> recordClasses = ai -> {
            reflectionClasses.produce(new ReflectiveClassBuildItem(false, false, ai.value().asClass().name().toString()));
        };

        // Register constructor for classes with a pointer in an annotation
        for (DotName registerAnnotation : registerAnnotations) {
            index.getIndex().
                    getAnnotations(registerAnnotation).
                    forEach(recordClasses);
        }
    }

    @BuildStep
    void findHandlers(CombinedIndexBuildItem index, BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        // Register handlers of the annptations. There might be more than one class to be registered for a certain handler
        for (DotName annotation : annotationToHandlers.keySet()) {
            if (index.getIndex().getAnnotations(annotation).isEmpty() == false) {
                Set<String> handlers = annotationToHandlers.get(annotation);
                for (String handler : handlers) {
                    reflectionClasses.produce(new ReflectiveClassBuildItem(false, false, handler));
                }
            }
        }
    }

    @BuildStep
    NativeImageProxyDefinitionBuildItem registerProxyForSqlObject(CombinedIndexBuildItem index) {
        Set<String> classes = new HashSet<>();

        Consumer<AnnotationInstance> recordClasses = ai -> {
            if (ai.target().kind() == AnnotationTarget.Kind.METHOD) {
                classes.add(ai.target().asMethod().declaringClass().name().toString());
            }
        };

        for (DotName proxyTrigger : proxyTriggers) {
            index.getIndex().
                    getAnnotations(proxyTrigger).
                    forEach(recordClasses);
        }

        return new NativeImageProxyDefinitionBuildItem(new ArrayList<>(classes));
    }
}
