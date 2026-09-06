package com.hexnotech.commons.jpa.repository;

import com.hexnotech.commons.jpa.entity.BaseTenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BaseHexnotechTenantRepository<Entity extends BaseTenantEntity<Id, TenantId>, Id, TenantId>
        extends JpaRepository<Entity, Id>, JpaSpecificationExecutor<Entity> {
    default List<Entity> findAll() {
        throw new RuntimeException("Not implemented");
    }
}