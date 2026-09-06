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

package com.hexnotech.commons.service;

import com.hexnotech.commons.type.generic.HexnotechValidator;

/**
 * TODO: This is replace for {@link DefaultRequestValidator}
 * @param <SearchReq>
 * @param <Req>
 */
public class DefaultRequestValidatorV2<SearchReq, Req> implements RequestValidator<SearchReq, Req> {

    @Override
    public void validateSearchRequest(SearchReq searchRequest) {

    }

    @Override
    public void validate(Req request) {
        HexnotechValidator.of().validateNotNull(request, "Request").validate();
    }

}