package com.hexnotech.commons.jpa.entity;

public interface TenantIdProvider<T> {
    T getTenantId();

    void setTenantId(T tenantId);

    String getTenantAttribute();
}
