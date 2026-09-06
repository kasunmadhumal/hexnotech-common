package com.hexnotech.commons.type.api;

import lombok.Getter;

import java.util.List;

@Getter
public class HexnotechResponse<E> {

    private E data;
    private int pageNumber;
    private int totalPages;
    private List<E> dataList;
    private String message;

    public HexnotechResponse(E data) {
        this.data = data;
    }

    public HexnotechResponse(E data, String message) {
        this.data = data;
        this.message = message;
    }

    public HexnotechResponse(List<E> dataList) {
        this.dataList = dataList;
    }

    public HexnotechResponse(List<E> dataList, int pageNumber, int totalPages) {
        this.dataList = dataList;
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
    }

    public HexnotechResponse(List<E> dataList, String message) {
        this.dataList = dataList;
        this.message = message;
    }

}
