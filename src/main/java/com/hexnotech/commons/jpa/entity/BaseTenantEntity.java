package com.hexnotech.commons.jpa.entity;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.hexnotech.commons.security.carrier.CarrierSecurityConstant;


@FilterDef(
        name = CarrierSecurityConstant.Filter.FILTER_NAME
)
//@Filter(name = TenantSecurityConstant.Filter.PARAM_NAME, condition = "tenant_id IN (:tenantIds)")
public interface BaseTenantEntity<ID, TenantId> extends BaseEntity<ID>, TenantIdProvider<TenantId> {
}
