package com.hexnotech.commons.type.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchFilter {
    private HexnotechPage page;

    public static SearchFilter empty() {
        return new SearchFilter(HexnotechPage.of());
    }

    public static SearchFilter of(int pageSize, int pageNumber, String sort, SortOrder sortOrder) {
        HexnotechPage hexnotechPage = HexnotechPage.of(pageSize, pageNumber, sort, sortOrder);
        return new SearchFilter(hexnotechPage);
    }

    public static SearchFilter of(int pageSize, int pageNumber, List<HexnotechSort> sorts) {
        HexnotechPage hexnotechPage = HexnotechPage.of(pageSize, pageNumber, sorts);
        return of(hexnotechPage);
    }

    public static SearchFilter of(int pageSize, int pageNumber, Pair<String, SortOrder>... sortPairs) {
        HexnotechPage hexnotechPage = HexnotechPage.of(pageSize, pageNumber, sortPairs);
        return of(hexnotechPage);
    }

    public static SearchFilter of(HexnotechPage hexnotechPage) {
        return new SearchFilter(hexnotechPage);
    }

    public static SearchFilter sortBy(String sort) {
        return new SearchFilter(HexnotechPage.bySort(sort));
    }

    public static SearchFilter sortBy(String sort, SortOrder sortOrder) {
        return new SearchFilter(HexnotechPage.bySort(sort, sortOrder));
    }

    public PageRequest toPageRequest() {
        return page.toPageRequest();
    }

    public Sort sort() {
        return page.toPageRequest().getSort();
    }
}
