package com.hexnotech.commons.jpa.repository;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BaseHexnotechRepository<Entity extends BaseEntity<Id>, Id>
        extends JpaRepository<Entity, Id>, JpaSpecificationExecutor<Entity> {
}