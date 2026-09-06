package com.hexnotech.commons.type.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HexnotechPage {

    private int start;
    private int limit;
    private int page;
    private String sort;
    @Builder.Default
    private SortOrder sortOrder = SortOrder.ASC;
    @Builder.Default
    private List<HexnotechSort> sorts = List.of();
    private boolean emptyPage = true;

    public static HexnotechPage of() {
        return HexnotechPage.builder().build();
    }

    public static HexnotechPage of(int start, int limit, int page, String sort) {
        return HexnotechPage.builder()
                .emptyPage(false)
                .start(start)
                .limit(limit)
                .page(page)
                .sort(sort)
                .build();
    }

    public static HexnotechPage of(int pageSize, int pageNumber, String sort, SortOrder sortOrder) {
        return HexnotechPage.builder()
                .emptyPage(false)
                .start(pageNumber * pageSize)
                .limit(pageSize)
                .page(pageNumber)
                .sort(sort)
                .sortOrder(sortOrder)
                .build();
    }

    public static HexnotechPage of(int start, int limit, String sort) {
        return HexnotechPage.builder().start(start).limit(limit).sort(sort).emptyPage(false).build();
    }

    public static HexnotechPage of(int pageSize, int pageNumber, List<HexnotechSort> sorts) {
        return HexnotechPage.builder()
                .emptyPage(false)
                .start(pageNumber * pageSize)
                .limit(pageSize)
                .page(pageNumber)
                .sorts(sorts)
                .build();
    }

    public static HexnotechPage of(int pageSize, int pageNumber, Pair<String, SortOrder>... sortPairs) {
        List<HexnotechSort> sorts = Arrays.stream(sortPairs)
                .map(pair -> new HexnotechSort(pair.getLeft(), pair.getRight()))
                .collect(Collectors.toList());
        return HexnotechPage.builder()
                .emptyPage(false)
                .start(pageNumber * pageSize)
                .limit(pageSize)
                .page(pageNumber)
                .sorts(sorts)
                .build();
    }

    public static HexnotechPage bySort(String sort) {
        return bySort(sort, SortOrder.ASC);
    }

    public static HexnotechPage bySort(String sort, SortOrder sortOrder) {
        return HexnotechPage.builder().start(0).limit(Integer.MAX_VALUE).sort(sort).sortOrder(sortOrder)
                .emptyPage(false).build();
    }

    public PageRequest toPageRequest() {
        if (emptyPage) {
            return PageRequest.of(0, Integer.MAX_VALUE);
        }
        int pageNumber = (limit > 0) ? start / limit : 0;
        if (HexnotechUtil.isNotEmpty(sort)) {
            return PageRequest.of(
                    pageNumber, limit,
                    sortOrder == SortOrder.ASC ?
                            Sort.by(sort).ascending() :
                            Sort.by(sort).descending()
            );
        }
        //Handling Multiple Sorts
        List<Sort.Order> orders = HexnotechUtil.nvlToList(sorts).stream()
                .filter(HexnotechSort::valid)
                .map(HexnotechSort::toSort)
                .collect(Collectors.toList());
        if (HexnotechUtil.isNotEmpty(orders)) {
            return PageRequest.of(pageNumber, limit, Sort.by(orders));
        } else {
            return PageRequest.of(pageNumber, limit);
        }
    }
}
