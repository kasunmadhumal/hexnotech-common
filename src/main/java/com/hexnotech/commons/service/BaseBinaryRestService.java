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
import com.hexnotech.commons.jpa.type.PredicationResolver;

public abstract class BaseBinaryRestService<Req, Res, E extends BaseEntity<I>, I>
        extends BaseHexnotechRestService<Req, Req, Res, E, I> {

    public BaseBinaryRestService(BaseHexnotechRestServiceContractV2<E, I> baseService,
                                 BaseHexnotechMapper<Req, Res, E, I> mapper,
                                 PredicationResolver<Req, E> predicationResolver,
                                 RequestValidator<Req, Req> requestValidator) {
        super(baseService, mapper, predicationResolver, requestValidator);
    }

    public BaseBinaryRestService(BaseHexnotechRestServiceContractV2<E, I> baseService,
                                 BaseHexnotechMapper<Req, Res, E, I> mapper,
                                 PredicationResolver<Req, E> predicationResolver) {
        super(baseService, mapper, predicationResolver);
    }
}
