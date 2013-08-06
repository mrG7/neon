package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.clauses.SelectClause

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

class AggregateMongoQueryWorker extends AbstractMongoQueryWorker{

    AggregateMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        def match = new BasicDBObject('$match', mongoQuery.whereClauseParams)
        def additionalClauses = MongoAggregationClauseBuilder.buildAggregateClauses(createSelectClause(mongoQuery), mongoQuery.query.aggregates, mongoQuery.query.groupByClauses)
        if (mongoQuery.query.sortClauses) {
            additionalClauses << new BasicDBObject('$sort', createSortDBObject(mongoQuery.query.sortClauses))
        }
        if (mongoQuery.query.limitClause) {
            additionalClauses << new BasicDBObject('$limit', mongoQuery.query.limitClause.limit)
        }
        def results = getCollection(mongoQuery).aggregate(match, additionalClauses as DBObject[]).results()
        return new MongoQueryResult(mongoIterable: results)
    }

    private def createSelectClause(MongoQuery mongoQuery){
        def selectClause = new SelectClause(dataStoreName: mongoQuery.query.dataStoreName, databaseName: mongoQuery.query.databaseName)
        if (mongoQuery.query.fields) {
            selectClause.fields = mongoQuery.query.fields
        }
        return selectClause
    }

}
