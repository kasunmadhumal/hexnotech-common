package com.hexnotech.commons.jpa.type;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.type.api.SearchFilter;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface PredicationResolver<Req, E extends BaseEntity<?>> {
    Predicate predicate(Req req, SearchFilter searchFilter,
                        Root<E> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder);

    default Specification<E> specification(Req req, SearchFilter searchFilter) {
        return (root, criteriaQuery, criteriaBuilder) ->
                predicate(req, searchFilter, root, criteriaQuery, criteriaBuilder);
    }
}
