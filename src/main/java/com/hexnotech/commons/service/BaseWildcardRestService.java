/*
 * Copyright(C) 2026 ACCELaero
 * All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 * Information Systems Associates (pvt) Ltd.
 *
 * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 * use only and is intended for view by persons duly authorized by the management of
 * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 * in any form or by any means without the written approval of the Management of
 * Information Systems Associates (pvt) Ltd.
 */

package com.hexnotech.commons.service;


import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.util.PredicateUtil;

public abstract class BaseWildcardRestService<Req, Res, E extends BaseEntity<I>, I>
        extends BaseHexnotechRestService<Void, Req, Res, E, I> {

    protected BaseWildcardRestService(BaseHexnotechRestServiceContractV2<E, I> jpaService,
                                      BaseHexnotechMapper<Req, Res, E, I> mapper) {
        super(jpaService, mapper, PredicateUtil.defaultTruePredicate());
    }

    protected BaseWildcardRestService(BaseHexnotechRestServiceContractV2<E, I> jpaService,
                                      BaseHexnotechMapper<Req, Res, E, I> mapper,
                                      PersistenceRequestValidator<Req> requestValidator) {
        super(jpaService, mapper, PredicateUtil.defaultTruePredicate(), requestValidator);
    }

}
