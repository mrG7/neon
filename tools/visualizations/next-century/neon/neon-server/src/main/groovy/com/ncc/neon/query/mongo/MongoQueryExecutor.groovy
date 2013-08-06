package com.ncc.neon.query.mongo

import com.mongodb.DB
import com.mongodb.MongoClient
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryGroupResult
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.QueryUtils
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.selection.SelectionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
 */

/**
 * Executes queries against a mongo data store
 */
class MongoQueryExecutor implements QueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    private final SelectionManager selectionManager = new SelectionManager()
    private final FilterState filterState = new FilterState()
    private final MongoClient mongo

    MongoQueryExecutor(MongoClient mongo) {
        this.mongo = mongo
    }

    @Override
    QueryResult execute(Query query, boolean includedFiltered) {
        MongoQuery mongoQuery = convertQueryIntoMongoQuery(query, includedFiltered)
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        worker.executeQuery(mongoQuery)
    }

    private AbstractMongoQueryWorker createMongoQueryWorker(Query query) {
        if (query.distinctClause) {
            LOGGER.debug("Using distinct mongo query worker")
            return new DistinctMongoQueryQueryWorker(mongo)
        }
        else if (query.aggregates || query.groupByClauses) {
            LOGGER.debug("Using aggregate mongo query worker")
            return new AggregateMongoQueryWorker(mongo)
        }
        LOGGER.debug("Using simple mongo query worker")
        return new SimpleMongoQueryQueryWorker(mongo)
    }

    @Override
    QueryResult execute(QueryGroup query, boolean includeFiltered) {
        QueryGroupResult queryGroupResult = new QueryGroupResult()
        query.namedQueries.each {
            def result = execute(it.query, includeFiltered)
            queryGroupResult.namedResults[it.name] = result
        }
        return queryGroupResult
    }

    @Override
    Collection<String> getFieldNames(String dataStoreName, String databaseName) {
        def db = mongo.getDB(dataStoreName)
        def collection = db.getCollection(databaseName)
        def result = collection.findOne()
        return result.keySet()
    }

    @Override
    UUID addFilter(Filter filter) {
        return filterState.addFilter(filter)
    }

    @Override
    void removeFilter(UUID id) {
        filterState.removeFilter(id)
    }

    @Override
    void clearFilters() {
        filterState.clearFilters()
    }

    @Override
    void setSelectionWhere(Filter filter) {
        def res = execute(QueryUtils.queryFromFilter(filter), false)
        def idField = this.idFieldName
        def ids = res.collect { it.getFieldValue(idField) }
        selectionManager.replaceSelectionWith(ids)
    }

    @Override
    void setSelectedIds(Collection<Object> ids) {
        selectionManager.replaceSelectionWith(transformIdFields(ids))
    }

    @Override
    void addSelectedIds(Collection<Object> ids) {
        selectionManager.addIds(transformIdFields(ids))
    }

    @Override
    void removeSelectedIds(Collection<Object> ids) {
        selectionManager.removeIds(transformIdFields(ids))
    }

    @Override
    void clearSelection() {
        selectionManager.clear()
    }

    @Override
    QueryResult getSelectionWhere(Filter filter) {
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState)
        Query query = QueryUtils.queryFromFilter(filter)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, createWhereClauseFromSelection.curry(getIdFieldName()))
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        worker.executeQuery(mongoQuery)
    }

    @Override
    List<String> showDatabases() {
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName) {
        DB database = mongo.getDB(dbName)
        database.getCollectionNames().collect { it }
    }

    private String getIdFieldName() {
        return "_id"
    }

    private def transformIdFields(Collection<Object> ids) {
        return MongoUtils.oidsToObjectIds(ids)
    }

    private MongoQuery convertQueryIntoMongoQuery(Query query, boolean includedFiltered){
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState)
        if(includedFiltered){
            return mongoConversionStrategy.convertQuery(query)
        }
        return mongoConversionStrategy.convertQueryWithFilters(query)
    }

    private final def createWhereClauseFromSelection = { String idFieldName ->
        def selectedIds = selectionManager.selectedIds
        return new SingularWhereClause(lhs: idFieldName, operator: 'in', rhs: selectedIds)
    }
}
