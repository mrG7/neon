package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState

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

/**
 * Converts a Query object into a BasicDbObject
 */

class MongoConversionStrategy {

    private final FilterState filterState
    private final FilterState selectionState

    MongoConversionStrategy(FilterState filterState, FilterState selectionState) {
        this.filterState = filterState
        this.selectionState = selectionState
    }

    MongoQuery convertQueryDisregardingFilters(Query query) {
        helpConvertQuery(query, false)
    }

    MongoQuery convertQueryWithFilterState(Query query) {
        helpConvertQuery(query, true)
    }

    private MongoQuery helpConvertQuery(Query query, boolean includeFiltersFromFilterState) {
        MongoQuery mongoQuery = new MongoQuery(query: query)
        mongoQuery.selectParams = createSelectParams(query)
        List whereClauses = assembleWhereClauses(query)

        if (includeFiltersFromFilterState) {
            whereClauses.addAll(createWhereClausesForFilters(query))
        }

        mongoQuery.whereClauseParams = createWhereClauseParams(whereClauses)
        return mongoQuery
    }

    private static DBObject createWhereClauseParams(List whereClauses) {
        if (!whereClauses) {
            return new BasicDBObject()
        }
        if (whereClauses.size() == 1) {
            return MongoWhereClauseBuilder.build(whereClauses[0])
        }
        return MongoWhereClauseBuilder.build(new AndWhereClause(whereClauses: whereClauses))
    }

    private static List assembleWhereClauses(Query query) {
        def whereClauses = []

        if (query.filter.whereClause) {
            whereClauses << query.filter.whereClause
        }
        return whereClauses
    }

    private def createWhereClausesForFilters(query) {
        def whereClauses = []
        DataSet dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        List<Filter> filters = filterState.getFiltersForDataset(dataSet)
        filters.addAll(selectionState.getFiltersForDataset(dataSet))
        if (!filters.isEmpty()) {
            filters.each {
                if (it.whereClause) {
                    whereClauses << it.whereClause
                }
            }
        }
        return whereClauses
    }

    private static DBObject createSelectParams(Query query) {
        BasicDBObject params = new BasicDBObject()

        if (query.fields != SelectClause.ALL_FIELDS) {
            query.fields.each {
                params[it] = 1
            }
        }

        return params
    }

}
