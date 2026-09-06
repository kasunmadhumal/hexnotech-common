package com.hexnotech.commons.service;

import com.hexnotech.commons.jpa.entity.BaseEntity;

public interface BaseHexnotechSearchOnlyMapper<Res, E extends BaseEntity<I>, I>
        extends BaseHexnotechMapper<Void, Res, E, I> {

    default E byRequest(Void req) {
        return null;
    }
}
