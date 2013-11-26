package com.ncc.neon.services

import com.ncc.neon.query.filter.FilterContainer
import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.query.filter.FilterState
import org.springframework.stereotype.Component

import javax.annotation.Resource
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

@Component
@Path("/selectionservice")
class SelectionService {

    @Resource
    FilterState selectionState

    @POST
    @Path("addselection")
    @Consumes(MediaType.APPLICATION_JSON)
    void addSelection(FilterContainer container) {
        selectionState.addFilter(container.filterKey, container.filter)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removeselection")
    void removeSelection(FilterKey filterKey) {
        selectionState.removeFilter(filterKey)
    }

    @POST
    @Path("replaceselection")
    @Consumes(MediaType.APPLICATION_JSON)
    void replaceSelection(FilterContainer container) {
        removeSelection(container.filterKey)
        addSelection(container)
    }

    @POST
    @Path("clearselection")
    void clearSelection() {
        selectionState.clearAllFilters()
    }

}
