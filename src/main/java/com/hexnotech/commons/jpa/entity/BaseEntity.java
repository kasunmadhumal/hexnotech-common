package com.hexnotech.commons.jpa.entity;

import java.io.Serializable;

public interface BaseEntity<I> extends IdProvider<I>, Serializable {
    void setId(I id);
}
