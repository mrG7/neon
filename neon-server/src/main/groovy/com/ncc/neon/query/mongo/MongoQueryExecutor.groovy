/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.query.mongo

import com.mongodb.DB
import com.mongodb.MongoClient
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Executes queries against a mongo data store
 */
@Component
class MongoQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    private static final GET_FIELD_NAMES_LIMIT = 1000

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)
        return getQueryResult(worker, mongoQuery)
    }

    QueryResult getQueryResult(AbstractMongoQueryWorker worker, MongoQuery mongoQuery) {
        QueryResult queryResult = SimpleQueryCache.getSimpleQueryCacheInstance().get(mongoQuery)
        if(!queryResult) {
            queryResult = worker.executeQuery(mongoQuery)
            SimpleQueryCache.getSimpleQueryCacheInstance().put(mongoQuery, queryResult)
        }
        return queryResult
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing getDatabaseNames")
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName) {
        DB database = mongo.getDB(dbName)
        LOGGER.debug("Executing getCollectionNames on database {}", dbName)
        database.getCollectionNames().collect { it }
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        def db = mongo.getDB(databaseName)
        def collection = db.getCollection(tableName)
        def resultSet = collection.find().limit(GET_FIELD_NAMES_LIMIT)
        Set<String> fieldNameSet = [] as Set
        resultSet.each { result ->
            fieldNameSet.addAll(result?.keySet())
        }
        return (fieldNameSet as List) ?: []
    }

    private AbstractMongoQueryWorker createMongoQueryWorker(Query query) {
        if (query.isDistinct) {
            LOGGER.trace("Using distinct mongo query worker")
            return new DistinctMongoQueryWorker(mongo)
        } else if (query.aggregates || query.groupByClauses) {
            if (query.groupByClauses.size() == 0 && query.aggregates.size() == 1) {
                def aggregate = query.aggregates[0]
                if (aggregate.operation == 'count' && aggregate.field == '*') {
                    LOGGER.trace("Using simple count query worker")
                    return new SimpleMongoCountWorker(mongo)
                }
            }
            LOGGER.trace("Using aggregate mongo query worker")
            return new AggregateMongoQueryWorker(mongo)
        }
        LOGGER.trace("Using simple mongo query worker")
        return new SimpleMongoQueryWorker(mongo)
    }

    private MongoClient getMongo() {
        connectionManager.connection.mongo
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit) {
        DB database = mongo.getDB(databaseName)
        ArrayCountQueryWorker worker = new ArrayCountQueryWorker(mongo).withDatabase(database)
        Query query = new Query(filter: new Filter(databaseName: databaseName, tableName: tableName))
        MongoQuery mongoQuery = worker.createArrayCountQuery(new MongoQuery(query: query), field, limit, filterState, selectionState)
        return getQueryResult(worker, mongoQuery).getData()
    }
}
