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

public abstract class BaseHexnotechSearchRestService<SearchReq, Res, E extends BaseEntity<I>, I>
        extends BaseHexnotechRestService<SearchReq, Void, Res, E, I> {

    public BaseHexnotechSearchRestService(BaseHexnotechRestServiceContractV2<E, I> baseService,
                                          BaseHexnotechSearchOnlyMapper<Res, E, I> mapper,
                                          PredicationResolver<SearchReq, E> predicationResolver,
                                          RequestValidator<SearchReq, Void> requestValidator) {
        super(baseService, mapper, predicationResolver, requestValidator);
    }

    public BaseHexnotechSearchRestService(BaseHexnotechRestServiceContractV2<E, I> baseService,
                                          BaseHexnotechSearchOnlyMapper<Res, E, I> mapper,
                                          PredicationResolver<SearchReq, E> predicationResolver) {
        super(baseService, mapper, predicationResolver);
    }
}
