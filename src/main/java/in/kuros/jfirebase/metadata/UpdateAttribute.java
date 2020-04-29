package in.kuros.jfirebase.metadata;

import com.google.common.collect.Lists;
import in.kuros.jfirebase.util.CustomCollectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public final class UpdateAttribute<T> {

    private final List<AttributeValue<T, ?>> keys;
    private final List<AttributeValue<T, ?>> attributesToUpdate;
    private final List<ValuePath<?>> valuePaths;

    private UpdateAttribute(final AttributeValue<T, ?> key) {
        keys = new ArrayList<>();
        keys.add(key);
        attributesToUpdate = new ArrayList<>();
        valuePaths = new ArrayList<>();
    }

    public static <T, K, V> UpdateAttribute<T> withKeys(final Attribute<T, V> attribute, final V value) {
        return new UpdateAttribute<>(AttributeValue.of(attribute, value));
    }

    public <V> UpdateAttribute<T> withKey(final Attribute<T, V> attribute, final V value) {
        keys.add(AttributeValue.of(attribute, value));
        return this;
    }

    public <V> UpdateAttribute<T> update(final Attribute<T, V> attribute, final V value) {
        attributesToUpdate.add(AttributeValue.of(attribute, value));
        return this;
    }

    public <V> UpdateAttribute<T> update(final AttributeValue<T, V> attributeValue) {
        attributesToUpdate.add(attributeValue);
        return this;
    }

    public <V> UpdateAttribute<T> updateFieldValue(final Attribute<T, ?> attribute, final Supplier<V> valueSupplier) {
        attributesToUpdate.add(AttributeValueImpl.of(attribute, Value.of(valueSupplier.get())));
        return this;
    }

    public <K, V> UpdateAttribute<T> update(final MapAttribute<T, K, V> mapAttribute, final K key, final V value) {
        attributesToUpdate.add(MapAttributeValue.of(mapAttribute, key, value));
        return this;
    }

    public <K, V> UpdateAttribute<T> updateFieldValue(final MapAttribute<T, K, ?> mapAttribute, final K key, final Supplier<V> valueSupplier) {
        attributesToUpdate.add(new MapAttributeValueImpl<>(mapAttribute, key, Value.of(valueSupplier.get())));
        return this;
    }

    public <K, V> UpdateAttribute<T> update(final MapAttribute<T, K, V> mapAttribute, final Map<K, V> value) {
        attributesToUpdate.add(MapAttributeValue.of(mapAttribute, value));
        return this;
    }


    public <V> UpdateAttribute<T> update(final Attribute<T, ?> attribute, final ValuePath<V> valuePath) {
        final List<String> path = Lists.newArrayList(attribute.getName());
        path.addAll(Arrays.asList(valuePath.getPath()));

        final String joinedPath = String.join(".", path);
        valuePaths.add(new ValuePath<>(valuePath.getValue(), joinedPath));
        return this;
    }


    public interface Helper {
        static <T> List<AttributeValue<T, ?>> getKeys(UpdateAttribute<T> updateAttribute) {
            return updateAttribute.keys;
        }

        static <T> List<AttributeValue<T, ?>> getAttributeValues(UpdateAttribute<T> updateAttribute) {
            return updateAttribute
                    .attributesToUpdate;
        }

        static <T> Map<String, ?> getValuePaths(UpdateAttribute<T> updateAttribute) {
            return updateAttribute.valuePaths.stream()
                    .collect(CustomCollectors.toMap(vp -> vp.getPath()[0], ValuePath::getValue));
        }

        static <T> Class<T> getDeclaringClass(UpdateAttribute<T> updateAttribute) {
            if (updateAttribute.keys.isEmpty()) {
                throw new IllegalStateException("No Keys provided");
            }

            return updateAttribute.keys.get(0).getAttribute().getDeclaringType();
        }
    }
}
