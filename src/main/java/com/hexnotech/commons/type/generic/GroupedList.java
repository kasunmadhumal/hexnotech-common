package com.hexnotech.commons.type.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class GroupedList<T, I> {

    private Map<I, List<T>> groupedMap = new HashMap<>();
    private BiPredicate<Integer, T> masterElementFilter = (index, element) -> index == 0;

    public GroupedList(Map<I, List<T>> groupedMap) {
        this.groupedMap = groupedMap;
    }

    public GroupedList(Map<I, List<T>> groupedMap, BiPredicate<Integer, T> masterElementFilter) {
        this.groupedMap = groupedMap;
        this.masterElementFilter = masterElementFilter;
    }

    public GroupedList(List<T> items, Function<T, I> identifierFunction) {
        this(items, identifierFunction, (index, element) -> index == 0);
    }

    public GroupedList(List<T> items, Function<T, I> identifierFunction, BiPredicate<Integer, T> masterElementFilter) {
        this.groupedMap = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(identifierFunction));
        this.masterElementFilter = masterElementFilter;
    }

    public static <T, I> GroupedList<T, I> of(Map<I, List<T>> groupedMap) {
        return new GroupedList<>(groupedMap);
    }

    public static <T, I> GroupedList<T, I> of(Map<I, List<T>> groupedMap, BiPredicate<Integer, T> masterElementFilter) {
        return new GroupedList<>(groupedMap, masterElementFilter);
    }

    public static <T, I> GroupedList<T, I> of(List<T> items, Function<T, I> identifierFunction) {
        return new GroupedList<>(items, identifierFunction);
    }

    public static <T, I> GroupedList<T, I> of(List<T> items, Function<T, I> identifierFunction,
                                              BiPredicate<Integer, T> masterElementFilter) {
        return new GroupedList<>(items, identifierFunction, masterElementFilter);
    }

    public List<T> getUniques() {
        return getUniques((master, nonMaster) -> {});
    }

    public List<T> getUniques(BiConsumer<T, T> accumilator) {
        List<T> results = new ArrayList<>();
        groupedMap.values().forEach(list -> {
            List<T> nonMasterElements = new ArrayList<>();
            T masterElement = null;
            for (int i = 0; i < list.size(); i++) {
                T element = list.get(i);
                if (masterElementFilter.test(i, element)) {
                    masterElement = element;
                } else {
                    nonMasterElements.add(element);
                }
            }
            if (masterElement != null) {
                for (T nonMasterElement : nonMasterElements) {
                    accumilator.accept(masterElement, nonMasterElement);
                }
                results.add(masterElement);
            }

        });

        return results;
    }

}