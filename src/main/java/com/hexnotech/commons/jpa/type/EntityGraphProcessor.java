/*
 *
 *  * Copyright(C) 2025 ACCELaero
 *  * All rights reserved.
 *  * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 *  * Information Systems Associates (pvt) Ltd.
 *  *
 *  * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 *  * use only and is intended for view by persons duly authorized by the management of
 *  * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 *  * in any form or by any means without the written approval of the Management of
 *  * Information Systems Associates (pvt) Ltd.
 *  
 */

package com.hexnotech.commons.jpa.type;

import org.hibernate.Hibernate;

import com.hexnotech.commons.exception.HexnotechProcessException;
import com.hexnotech.commons.util.HexnotechUtil;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import java.util.Arrays;
import java.util.Collection;

public class EntityGraphProcessor<T> {

    public void process(T entity, String graphName) {
        NamedEntityGraph namedEntityGraph = findEntityGraph(entity, graphName);
        if (namedEntityGraph != null) {
            processAttributeNodes(entity, namedEntityGraph.attributeNodes(), namedEntityGraph.subgraphs());
        }
    }

    private void processAttributeNodes(Object entity, NamedAttributeNode[] attributeNodes, NamedSubgraph[] subgraphs) {
        if (entity == null) return;

        for (NamedAttributeNode node : attributeNodes) {
            try {
                String fieldName = node.value();
                Object fieldValue = invokeGetter(entity, fieldName);

                if (null != fieldValue) {
                    if (fieldValue instanceof Collection<?>) {
                        processCollection((Collection<?>) fieldValue, node, subgraphs);
                    } else {
                        processSingleEntity(entity, fieldValue, fieldName, node, subgraphs);
                    }
                }

            } catch (Exception e) {
                throw new HexnotechProcessException("Error processing entity graph for field: " + node.value());
            }
        }
    }

    private Object invokeGetter(Object entity, String fieldName) {
        try {
            PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            return accessor.getPropertyValue(fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access property: " + fieldName, e);
        }
    }
    @SuppressWarnings("java:S5276") // fieldName comes from @NamedAttributeNode annotations (compile-time), value is an unproxied Hibernate entity — no untrusted input
    private void invokeSetter(Object entity, String fieldName, Object value) {
        try {
            PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            accessor.setPropertyValue(fieldName, value);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set property: " + fieldName, e);
        }
    }

    private void processCollection(Collection<?> collection, NamedAttributeNode node, NamedSubgraph[] subgraphs) {
        Hibernate.initialize(collection);
        HexnotechUtil.nvlList(collection).forEach(item -> {
            if (null != item) {
                processSingleEntity(null, item, null, node, subgraphs);
            }
        });
    }

    private void processSingleEntity(Object parentEntity, Object entity, String fieldName, NamedAttributeNode node, NamedSubgraph[] subgraphs) {
        if (null != entity) {
            Hibernate.initialize(entity);
            
            if (entity instanceof HibernateProxy) {
                Object unproxiedEntity = Hibernate.unproxy(entity);
                
                if (parentEntity != null && fieldName != null) {
                    invokeSetter(parentEntity, fieldName, unproxiedEntity);
                }
                entity = unproxiedEntity;
            }

            String subgraphName = node.subgraph();
            if (!subgraphName.isEmpty()) {
                NamedSubgraph subgraph = findSubgraph(subgraphs, subgraphName);
                if (subgraph != null) {
                    processAttributeNodes(entity, subgraph.attributeNodes(), subgraphs);
                }
            }
        }
    }

    private NamedSubgraph findSubgraph(NamedSubgraph[] subgraphs, String name) {
        return Arrays.stream(subgraphs)
                .filter(subgraph -> subgraph.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    private NamedEntityGraph findEntityGraph(T entity, String graphName) {
        if (entity.getClass().isAnnotationPresent(NamedEntityGraph.class)) {
            NamedEntityGraph graph = entity.getClass().getAnnotation(NamedEntityGraph.class);
            if (graph.name().equals(graphName)) {
                return graph;
            }
        }

        if (entity.getClass().isAnnotationPresent(NamedEntityGraphs.class)) {
            return Arrays.stream(entity.getClass().getAnnotation(NamedEntityGraphs.class).value())
                    .filter(graph -> graph.name().equals(graphName))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}