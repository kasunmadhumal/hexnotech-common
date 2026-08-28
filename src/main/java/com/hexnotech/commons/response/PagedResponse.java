package com.hexnotech.commons.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 * <p>
 * Typically returned alongside {@link ApiResponse} for paginated queries:
 *
 * <pre>{@code
 * Page<UserDto> page = userService.findAll(pageable);
 * return ResponseEntity.ok(
 *     ApiResponse.success("Users fetched", PagedResponse.of(page))
 * );
 * }</pre>
 *
 * @param <T> the type of items in the page
 */
@Getter
@Builder
public class PagedResponse<T> {

    /** The items on the current page. */
    private final List<T> content;

    /** Current page number (0-indexed). */
    private final int page;

    /** Number of items per page. */
    private final int size;

    /** Total number of pages. */
    private final int totalPages;

    /** Total number of records across all pages. */
    private final long totalElements;

    /** Whether this is the first page. */
    private final boolean first;

    /** Whether this is the last page. */
    private final boolean last;

    /**
     * Convenience factory — builds a {@code PagedResponse} directly from a Spring
     * Data {@link Page}.
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
