package com.hexnotech.commons.type.comparison;

import com.hexnotech.commons.util.HexnotechUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;

public class ComparisonProvider<T> {
    private final Optional<T> existing;
    private final T updated;
    @Getter
    private final Map<String, String> updatedFields = new HashMap<>();

    @Getter
    private final Map<String, FieldDelta> deltas = new HashMap<>();

    private final List<FieldDelta> genericDeltas = new ArrayList<>();

    public ComparisonProvider(Optional<T> existing, T updated) {
        this.existing = existing;
        this.updated = updated;
    }

    public static <T> ComparisonProvider<T> by(T existing, T updated) {
        return by(Optional.ofNullable(existing), updated);
    }

    public static <T> ComparisonProvider<T> by(Optional<T> existing, T updated) {
        return new ComparisonProvider<>(existing, updated);
    }

    public ComparisonProvider<T> compare(String fieldName, Function<T, ?> elementFunction) {
        return compare(fieldName, elementFunction, false);
    }

    public ComparisonProvider<T> compareJson(String fieldName, Function<T, ?> elementFunction) {
        return compare(fieldName, elementFunction, true);
    }

    public ComparisonProvider<T> compare(String fieldName, Function<T, ?> elementFunction, boolean isJson) {
        Object oldValue = existing.map(elementFunction).orElse(null);
        Object newValue = elementFunction.apply(updated);
        if (isJson) {
            compareJsonAndAdd(fieldName, oldValue, newValue);
        } else {
            compareAndAdd(fieldName, oldValue, newValue);
        }
        return this;
    }

    private void compareAndAdd(String fieldName, Object oldValue, Object newValue) {
        if ((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue))) {
            updatedFields.put(fieldName, newValue != null ? newValue.toString() : "null");
            deltas.put(fieldName, FieldDelta.of(
                    HexnotechUtil.nvl(oldValue, Object::toString, null),
                    HexnotechUtil.nvl(newValue, Object::toString, null)));
        }
    }

    private void compareJsonAndAdd(String fieldName, Object oldValue, Object newValue) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode oldNode = mapper.valueToTree(oldValue);
        JsonNode newNode = mapper.valueToTree(newValue);
        compareJsonNodes(oldNode, newNode, fieldName);
    }

    private void compareJsonNodes(JsonNode oldNode, JsonNode newNode, String path) {
        if (oldNode == null && newNode == null) {
            return;
        }
        if (oldNode == null) {
            updatedFields.put(path, newNode.toString());
            deltas.put(path, FieldDelta.of(null, newNode.toString()));
            return;
        }
        if (newNode == null) {
            updatedFields.put(path, "null");
            deltas.put(path, FieldDelta.of(oldNode.toString(), null));
            return;
        }

        if (!oldNode.equals(newNode)) {
            if (oldNode.isObject() && newNode.isObject()) {
                Set<String> allFields = new HashSet<>();
                oldNode.fieldNames().forEachRemaining(allFields::add);
                newNode.fieldNames().forEachRemaining(allFields::add);

                Iterator<String> fieldNames = allFields.iterator();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode oldField = oldNode.get(fieldName);
                    JsonNode newField = newNode.get(fieldName);
                    compareJsonNodes(oldField, newField, path + "." + fieldName);
                }
            } else if (oldNode.isArray() && newNode.isArray()) {
                int maxSize = Math.max(oldNode.size(), newNode.size());
                for (int i = 0; i < maxSize; i++) {
                    JsonNode oldElement = i < oldNode.size() ? oldNode.get(i) : null;
                    JsonNode newElement = i < newNode.size() ? newNode.get(i) : null;
                    compareJsonNodes(oldElement, newElement, path + "[" + i + "]");
                }
            } else {
                updatedFields.put(path, newNode.toString());
                deltas.put(path, FieldDelta.of(oldNode.toString(), newNode.toString()));
            }
            }
       }

    public static ComparisonProvider<Object> empty() {
        return new ComparisonProvider<>(Optional.empty(), null);
    }

    public ComparisonProvider<T> compareValues(String fieldName, Object oldValue, Object newValue) {
        compareAndAdd(fieldName, oldValue, newValue);
        return this;
    }

    public <M> ComparisonProvider<T> compare(String fieldName,
                                              com.hexnotech.commons.type.generic.MergeList<M, ?> mergeList,
                                              Function<M, String> snapshotFn,
                                              Function<M, String> keyFn) {
        mergeList.getAddedItems().forEach(item ->
                genericDeltas.add(FieldDelta.newDelta(keyFn.apply(item), snapshotFn.apply(item))));
        mergeList.getRemovedItems().forEach(item ->
                genericDeltas.add(FieldDelta.removedDelta(keyFn.apply(item), snapshotFn.apply(item))));
        mergeList.forEachUpdated((orig, upd) -> {
            String oldSnap = snapshotFn.apply(orig);
            String newSnap = snapshotFn.apply(upd);
            if (!Objects.equals(oldSnap, newSnap)) {
                genericDeltas.add(FieldDelta.builder()
                        .recordName(keyFn.apply(orig))
                        .previousValue(oldSnap)
                        .newValue(newSnap)
                        .build());
            }
        });
        return this;
    }

    public List<FieldDelta> getDeltaList() {
        List<FieldDelta> result = new ArrayList<>(genericDeltas);
        deltas.forEach((name, delta) -> result.add(
                FieldDelta.builder()
                        .recordName(name)
                        .previousValue(delta.getPreviousValue())
                        .newValue(delta.getNewValue())
                        .build()));
        return result;
    }

}