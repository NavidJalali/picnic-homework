package tech.picnic.assignment.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Constructs {@link ObjectMapper} instances with custom features enabled or disabled.
 */
final class ObjectMapperFactory {
    private ObjectMapperFactory() {}

    /* You may tweak the configuration of the mapper returned by this method as needed. */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new RecordNamingStrategyPatchModule());
        objectMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        return objectMapper;
    }

    /**
     * Records, being a relatively new Java feature, don't work with all Jackson features yet. In
     * particular, custom property naming strategies currently fail with records, as discussed on <a
     * href="https://github.com/FasterXML/jackson-databind/issues/2992">GitHub</a>.
     *
     * <p>The workaround provided here is taken from a <a
     * href="https://github.com/FasterXML/jackson-databind/issues/2992#issuecomment-799213433">comment</a>
     * on the issue. There's no need to edit (or even understand) this code.
     *
     * <p>We'll drop this once the Jackson issue is resolved. One of the consequences of living on the
     * bleeding edge of tech! :)
     */
    private static class RecordNamingStrategyPatchModule extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            context.addValueInstantiators(new ValueInstantiatorsModifier());
            super.setupModule(context);
        }

        private static class ValueInstantiatorsModifier extends ValueInstantiators.Base {
            @Override
            public ValueInstantiator findValueInstantiator(
                    DeserializationConfig config,
                    BeanDescription beanDesc,
                    ValueInstantiator defaultInstantiator) {
                if (!beanDesc.getBeanClass().isRecord()
                        || !(defaultInstantiator instanceof StdValueInstantiator)
                        || !defaultInstantiator.canCreateFromObjectWith()) {
                    return defaultInstantiator;
                }
                Map<String, BeanPropertyDefinition> map =
                        beanDesc.findProperties().stream()
                                .collect(
                                        Collectors.toMap(BeanPropertyDefinition::getInternalName, Function.identity()));
                SettableBeanProperty[] renamedConstructorArgs =
                        Arrays.stream(defaultInstantiator.getFromObjectArguments(config))
                                .map(
                                        p -> {
                                            BeanPropertyDefinition prop = map.get(p.getName());
                                            return prop != null ? p.withName(prop.getFullName()) : p;
                                        })
                                .toArray(SettableBeanProperty[]::new);

                return new PatchedValueInstantiator(
                        (StdValueInstantiator) defaultInstantiator, renamedConstructorArgs);
            }
        }

        private static class PatchedValueInstantiator extends StdValueInstantiator {
            protected PatchedValueInstantiator(
                    StdValueInstantiator src, SettableBeanProperty[] constructorArguments) {
                super(src);
                _constructorArguments = constructorArguments;
            }
        }
    }
}
