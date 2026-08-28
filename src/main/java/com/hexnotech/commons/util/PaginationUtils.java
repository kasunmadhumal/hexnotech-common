package com.hexnotech.commons.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility helpers for building Spring Data {@link Pageable} objects
 * from raw controller parameters.
 *
 * <pre>{@code
 * @GetMapping
 * public ResponseEntity<?> list(
 *         @RequestParam(defaultValue = "0")  int page,
 *         @RequestParam(defaultValue = "20") int size,
 *         @RequestParam(defaultValue = "id") String sortBy) {
 *
 *     Pageable pageable = PaginationUtils.of(page, size, sortBy, Sort.Direction.DESC);
 *     ...
 * }
 * }</pre>
 */
public final class PaginationUtils {

    /** Default page size used when {@code size} ≤ 0 or missing. */
    public static final int DEFAULT_SIZE = 20;

    /** Maximum allowed page size to prevent abuse. */
    public static final int MAX_SIZE = 100;

    private PaginationUtils() {}

    /**
     * Builds a {@link Pageable} with sorting.
     *
     * @param page      0-based page number (clamped to 0 if negative)
     * @param size      items per page (clamped to [{@code 1}, {@link #MAX_SIZE}])
     * @param sortBy    field name to sort by
     * @param direction sort direction
     */
    public static Pageable of(int page, int size, String sortBy, Sort.Direction direction) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), MAX_SIZE);
        Sort sort = Sort.by(direction, sortBy == null || sortBy.isBlank() ? "id" : sortBy);
        return PageRequest.of(safePage, safeSize, sort);
    }

    /**
     * Builds a {@link Pageable} sorted ascending by {@code sortBy}.
     */
    public static Pageable of(int page, int size, String sortBy) {
        return of(page, size, sortBy, Sort.Direction.ASC);
    }

    /**
     * Builds a {@link Pageable} with default size ({@link #DEFAULT_SIZE}) and no explicit sort.
     */
    public static Pageable ofDefault(int page) {
        return PageRequest.of(Math.max(0, page), DEFAULT_SIZE, Sort.by("id").ascending());
    }

    /**
     * Builds an unpaged {@link Pageable} — fetches everything. Use with caution on large tables.
     */
    public static Pageable unpaged() {
        return Pageable.unpaged();
    }
}
