package org.jdbi.quarkus.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import com.google.common.collect.HashMultimap;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class JdbiAnnotationsQuarkusProcessor {

    private static final HashMultimap<DotName, String> annotationToHandlers = HashMultimap.create();
    private static Set<DotName> proxyTriggers = new HashSet<>();
    private static final Set<DotName> registerAnnotations = new HashSet<>();

    static {
        addConfig("RegisterArgumentFactories");
        addConfig("RegisterArgumentFactory");
        addConfig("RegisterBeanMapper");
        addConfig("RegisterBeanMappers");
        addConfig("RegisterCollectorFactory");
        addConfig("RegisterColumnMapper");
        addConfig("RegisterColumnMapperFactories");
        addConfig("RegisterColumnMapperFactory");
        addConfig("RegisterColumnMappers");
        addConfig("RegisterConstructorMapper");
        addConfig("RegisterConstructorMappers");
        addConfig("RegisterFieldMapper");
        addConfig("RegisterFieldMappers");
        addConfig("RegisterJoinRowMapper");
        addConfig("RegisterObjectArgumentFactories");
        addConfig("RegisterObjectArgumentFactory");
        addConfig("RegisterRowMapper");
        addConfig("RegisterRowMapperFactories");
        addConfig("RegisterRowMapperFactory");
        addConfig("RegisterRowMappers");

        addConfig("UseEnumStrategy");
        addConfig("UseSqlParser");
        addConfig("UseTemplateEngine");

        addConfig("KeyColumn");
        addConfig("ValueColumn");

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

    static void addConfig(String name) {
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

    void recordClasses(BuildProducer<ReflectiveClassBuildItem> reflectionClasses, AnnotationInstance ai) {
        reflectionClasses.produce(ReflectiveClassBuildItem.builder(ai.value().asClass().name().toString()).build());
    }

    @BuildStep
    void findRegistrations(CombinedIndexBuildItem index, BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        // Register constructor for classes with a pointer in an annotation
        for (DotName registerAnnotation : registerAnnotations) {
            for (AnnotationInstance ai : index.getIndex().getAnnotations(registerAnnotation)) {
                if (ai.value().kind() == AnnotationValue.Kind.ARRAY
                        && ai.value().componentKind() == AnnotationValue.Kind.NESTED) {
                    for (AnnotationInstance inner : ai.value().asNestedArray()) {
                        recordClasses(reflectionClasses, inner);
                    }
                } else if (ai.value().kind() == AnnotationValue.Kind.NESTED) {
                    recordClasses(reflectionClasses, ai.value().asNested());
                } else if (ai.value().kind() == AnnotationValue.Kind.CLASS) {
                    recordClasses(reflectionClasses, ai);
                }
            }
        }
    }

    @BuildStep
    void findHandlers(CombinedIndexBuildItem index, BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        // Register handlers of the annptations. There might be more than one class to be registered for a certain handler
        for (DotName annotation : annotationToHandlers.keySet()) {
            if (index.getIndex().getAnnotations(annotation).isEmpty() == false) {
                Set<String> handlers = annotationToHandlers.get(annotation);
                for (String handler : handlers) {
                    reflectionClasses.produce(ReflectiveClassBuildItem.builder(handler).build());
                }
            }
        }
    }

    @BuildStep
    void registerProxyForSqlObject(CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses,
            BuildProducer<NativeImageProxyDefinitionBuildItem> proxyClasses) {
        Set<String> classes = new HashSet<>();
        Set<String> annotations = new HashSet<>();

        Consumer<AnnotationInstance> recordClasses = ai -> {
            if (ai.target().kind() == AnnotationTarget.Kind.METHOD) {
                classes.add(ai.target().asMethod().declaringClass().name().toString());
            }
        };

        for (DotName proxyTrigger : proxyTriggers) {
            index.getIndex().getAnnotations(proxyTrigger).forEach(recordClasses);
            recordInterface(annotations, index, proxyTrigger);
        }

        index.getIndex().getAllKnownImplementors("org.jdbi.v3.sqlobject.SqlObject").forEach((cls) -> {
            classes.add(cls.name().toString());
        });

        String cls[] = new ArrayList<>(classes).toArray(new String[classes.size()]);
        String ann[] = new ArrayList<>(annotations).toArray(new String[annotations.size()]);

        // Method of the interface must be visible
        reflectionClasses.produce(ReflectiveClassBuildItem.builder(cls).methods(true).build());
        reflectionClasses.produce(ReflectiveClassBuildItem.builder(ann).methods(true).build());

      // Interface should be available for dynamic proxy creation
        for (String _class : classes) {
            // NativeImageProxyDefinitionBuildItem is supposed to be able to take multiple classes,
            // but it doesn't work for some reason (tested on Quarkus 3.21.0)
            proxyClasses.produce(new NativeImageProxyDefinitionBuildItem(_class));
        }
    }

    private void recordInterface(Set<String> annotations, CombinedIndexBuildItem index, DotName iface) {
        annotations.add(iface.toString());

        ClassInfo cls = index.getIndex().getClassByName(iface);

        if (cls == null) {
            return;
        }

        cls.asClass().interfaceNames();
        List<DotName> ifs = cls.asClass().interfaceNames();
        if (ifs == null) {
            return;
        }
        ifs.forEach(dt -> recordInterface(annotations, index, dt));
    }
}
