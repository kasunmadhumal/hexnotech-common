/*
 * Copyright(C) 2025 ACCELaero
 * All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 * Information Systems Associates (pvt) Ltd.
 *
 * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 * use only and is intended for view by persons duly authorized by the management of
 * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 * in any form or by any means without the written approval of the Management of
 * Information Systems Associates (pvt) Ltd.
 */

package com.hexnotech.commons.type.generic;

import org.apache.commons.lang3.tuple.Pair;

import com.hexnotech.commons.jpa.entity.IdProvider;
import com.hexnotech.commons.util.HexnotechUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MergeList<T, I> {

    private final List<T> addedItems = new ArrayList<>();
    private final Map<I, T> removedMap = new HashMap<>();
    private final Map<I, T> updatedMap = new HashMap<>();
    private final Map<I, T> originalUpdatedMap = new HashMap<>();

    private final Collection<T> originalItems;
    private final Function<T, I> identifierFunction;
    private final Map<I, T> originalItemMap;
    private final Predicate<I> validId;

    public MergeList(Collection<T> originalItems, Function<T, I> identifierFunction) {
        this(originalItems, identifierFunction, Objects::nonNull);
    }

    public MergeList(Collection<T> originalItems, Function<T, I> identifierFunction, Predicate<I> validId) {
        this.originalItems = originalItems;
        this.identifierFunction = identifierFunction;
        this.validId = validId;
        this.originalItemMap = HexnotechUtil.nvlList(originalItems).stream()
                .collect(Collectors.toMap(identifierFunction, item -> item));
    }

    public static <T extends IdProvider<I>, I> MergeList<T, I> of(Collection<T> originalItems) {
        return new MergeList<>(originalItems, IdProvider::getId);
    }

    public static <T, I> MergeList<T, I> of(Collection<T> originalItems, Function<T, I> identifierFunction) {
        return new MergeList<>(originalItems, identifierFunction);
    }

    public static <T, I> MergeList<T, I> of(Collection<T> originalItems, Function<T, I> identifierFunction,
                                            Predicate<I> idValidation) {
        return new MergeList<>(originalItems, identifierFunction, idValidation);
    }

    public MergeList<T, I> process(Collection<T> newItems) {
        return process(newItems, Function.identity());

    }
    public <X> MergeList<T, I> process(Collection<X> newItems, Function<X, T> converter) {
        newItems.forEach(newTypeItem -> {
            T newItem = converter.apply(newTypeItem);
            I key = identifierFunction.apply(newItem);
            if (validId.test(key) && originalItemMap.containsKey(key)) {
                updatedMap.put(key, newItem);
                originalUpdatedMap.put(key, originalItemMap.get(key));
            } else {
                addedItems.add(newItem);
            }
        });
        originalItemMap.forEach((key, originalItem) -> {
            if (!updatedMap.containsKey(key)) {
                removedMap.put(key, originalItem);
            }
        });
        return this;
    }

    public List<T> mergeByUpdated() {
        List<T> finalResult = new ArrayList<>();
        finalResult.addAll(updatedMap.values());
        finalResult.addAll(addedItems);
        return finalResult;
    }

    /**
     * Merge the lists and populate the data from the updated items to the original items using the provided dataPopulate.
     * @param dataPopulate : First parameter is the original item, second parameter is the updated item.
     * @return
     */
    public List<T> merge(BiConsumer<T, T> dataPopulate) {
        return merge(dataPopulate, true);
    }

    public List<T> mergeAsNotRemoved(BiConsumer<T, T> dataPopulate) {
        return merge(dataPopulate, false);
    }

    public List<T> merge(BiConsumer<T, T> dataPopulate, boolean deleteSupported) {
        List<T> finalResult = new ArrayList<>();
        updatedMap.forEach((key, value) -> {
            T originalItem = originalItemMap.get(key);
            if (originalItem != null) {
                dataPopulate.accept(originalItem, value);
                finalResult.add(originalItem);
            }
        });
        finalResult.addAll(addedItems);
        HexnotechUtil.consume(!deleteSupported, () -> finalResult.addAll(getRemovedItems()));
        return finalResult;
    }

    public Pair<Map<I, T>, Map<I, T>> getUpdatedMap() {
        return Pair.of(originalUpdatedMap, updatedMap);
    }

    public void forEachUpdated(BiConsumer<T, T> consumer) {
        originalUpdatedMap.forEach((key, orig) -> consumer.accept(orig, updatedMap.get(key)));
    }

    public Collection<T> getRemovedItems() {
        return Collections.unmodifiableCollection(removedMap.values());
    }

    public Collection<I> getRemovedKeys() {
        return Collections.unmodifiableCollection(removedMap.keySet());
    }

    public Collection<T> getAddedItems() {
        return Collections.unmodifiableCollection(addedItems);
    }

    public Collection<T> getUpdatedItems() {
        return Collections.unmodifiableCollection(updatedMap.values());
    }

    public Collection<T> getOriginalItems() {
        return Collections.unmodifiableCollection(originalItems);
    }

    public Collection<I> getExistingKeys() {
        return originalItemMap.keySet();
    }
    
    public Collection<T> getExistingItems() {
        return originalItemMap.values();
    }

    /**
     * Returns the valid elements from {@code requested} based on {@code originalItems} and the identifier function
     * throwing an exception if any invalid elements are found on {@code requested}
     * If only one collection is present, returns it; returns empty if both are empty.
     */
    public <C extends Collection<T>> C getFromAvailableOrThrow(
            Collection<T> requested,
            Supplier<C> collectionSupplier,
            Function<Collection<T>, ? extends RuntimeException> exceptionProvider
    ) {
        C result = getFromAvailable(requested, collectionSupplier);
        
        if (HexnotechUtil.isNotEmpty(addedItems)) {
            throw exceptionProvider.apply(addedItems); // invalid requestedItems
        }
        
        return result;
        
    }

    /**
     * Returns the valid elements from {@code requested} based on {@code originalItems} and the identifier function
     * silently dropping any invalid elements found on {@code requested}
     * If only one collection is present, returns it; returns empty if both are empty.
     */
    public <C extends Collection<T>> C getFromAvailable(
            Collection<T> requested,
            Supplier<C> collectionSupplier
    ) {

        boolean hasAllowed = HexnotechUtil.isNotEmpty(originalItems);
        boolean hasRequested = HexnotechUtil.isNotEmpty(requested);
        C result = collectionSupplier.get();

        if (hasAllowed && hasRequested) {
            process(requested); 
            result.addAll(updatedMap.values()); // invalid RequestedItems silently dropped
            return result;
        }

        if (hasAllowed) {
            result.addAll(originalItems);
            return result;
        }

        if (hasRequested) {
            result.addAll(requested);
            return result;
        }
        return result;
    }

    /**
     * Returns elements from {@code requested} that exist in {@code originalItems},
     * throwing an exception if any requested elements are not in the originalItems.
     * If no originalItems is defined (empty {@code originalItems}), return empty
     * Returns all allowed items if {@code requested} is empty.
     */
    public <C extends Collection<T>> C getFromOriginalItemsOrThrowOnInvalid(
            Collection<T> requested,
            Supplier<C> collectionSupplier,
            Function<Collection<T>, ? extends RuntimeException> exceptionProvider
    ) {
        C result = getFromOriginalItems(requested, collectionSupplier);
        
        if (HexnotechUtil.isNotEmpty(addedItems)) {
            throw exceptionProvider.apply(addedItems); // invalid requestedItems
        }
        return result;
    }

    /**
     * Returns only the elements from {@code requested} that exist in {@code originalItems} ,
     * silently dropping any requested elements that are not in the originalItems.
     * If no originalItems is defined (empty {@code originalItems}), return empty
     * Returns all allowed items {@code originalItems} if {@code requested} is empty.
     */
    public <C extends Collection<T>> C getFromOriginalItems(
            Collection<T> requested,
            Supplier<C> collectionSupplier
    ) {
        C result = collectionSupplier.get();

        if (HexnotechUtil.isEmpty(originalItems)) {
            return result;
        }

        if (HexnotechUtil.isEmpty(requested)) {
            result.addAll(originalItems);
            return result;
        }

        process(requested);
        result.addAll(updatedMap.values());  // invalid RequestedItems silently dropped
        return result;
    }
}
