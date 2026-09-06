package com.hexnotech.commons.service;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @deprecated This will be replace with {@link BaseHexnotechRestServiceContractV2}
 * @param <E>
 * @param <I>
 */
@Deprecated
public interface BaseHexnotechJpaServiceContract<E extends BaseEntity<I>, I> {

    List<E> findAll(Specification<E> specification);

    Page<E> findAll(Specification<E> specification, PageRequest pageRequest);

    Optional<E> findOne(Specification<E> specification);

    Optional<E> findById(I id);

    List<E> findByIds(Collection<I> id);

    default Map<I, E> findAndMapByIds(Collection<I> ids) {
        if (HexnotechUtil.isNotEmpty(ids)) {
            return findByIds(ids).stream().collect(
                    Collectors.toMap(BaseEntity::getId, entity -> entity)
            );
        } else {
            return Map.of();
        }
    }

    E save(E entity);

    List<E> saveAll(List<E> entityList);

    E update(I id, E entity);

    void deleteById(I id);

    E deleteOrThrow(I id);



}
