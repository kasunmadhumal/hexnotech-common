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

public interface RequestValidator<SearchReq, Req> {

    /**
     * Validates the search request object.
     * @param searchRequest
     */
    void validateSearchRequest(SearchReq searchRequest);

    /**
     * Validates the request object.
     *
     * @param request the request object to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    void validate(Req request);


}
