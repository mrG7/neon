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

/**
 * This is an end to end acceptance test to verify that queries can be executed against a mongo instance.
 * These tests parallel those in the MongoQueryExecutorIntegrationTest.
 */
/*global neontest*/
/*global host*/
/*global getJSONFixture*/

describe('query mapping', function() {
    // aliases for easier test writing
    var where = neon.query.where;
    var or = neon.query.or;
    var and = neon.query.and;

    var databaseName = 'acceptanceTest';
    var tableName = 'records';
    var allData;

    var dcStateFilter = baseFilter().where('state', '=', 'DC');

    jasmine.getJSONFixtures().fixturesPath = 'src/test-data';

    var connection = new neon.query.Connection();
    var messenger = new neon.eventing.Messenger();
    var filterId = "filterA";

    // helpers to run async tests against the correct objects
    var executeQueryAndWait = function(asyncFunction, query) {
        return neontest.executeAndWait(connection, asyncFunction, query);
    };

    var sendMessageAndWait = function(asyncFunction, args) {
        return neontest.executeAndWait(messenger, asyncFunction, args);
    };

    beforeEach(function() {
        // host is generated dynamically during the build and included in the acceptance test helper file
        connection.connect(neon.query.Connection.MONGO, host);

        if(!allData) {
            allData = getJSONFixture('data.json');
        }
    });

    it('get field names', function() {
        var result = executeQueryAndWait(connection.getFieldNames, [databaseName, tableName]);
        var expected = ['_id', 'firstname', 'lastname', 'city', 'state', 'salary', 'hiredate', 'location', 'tags'];
        runs(function() {
            expect(result.get()).toBeArrayWithSameElements(expected);
        });
    });

    it('query all data', function() {
        assertQueryResults(baseQuery(), allData);
    });

    it('select subset of fields from result', function() {
        var fields = ['firstname', 'lastname'];
        var expected = [];
        allData.forEach(function(row) {
            var expectedRow = {};
            // the _id field is always included from mongo
            expectedRow._id = row._id;
            fields.forEach(function(field) {
                // some rows do not have all fields, so skip those
                if(row[field]) {
                    expectedRow[field] = row[field];
                }
            });
            expected.push(expectedRow);
        });
        assertQueryResults(baseQuery().withFields(fields[0], fields[1]), expected);
    });

    it('select derived field', function() {
        var expectedData = getJSONFixture('groupByMonth.json');
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        // aggregate fields are automatically included in the select even though withFields is specified
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).withFields('hire_month');
        assertQueryResults(query, expectedData);
    });

    it('query WHERE', function() {
        var whereStateClause = or(where('state', '=', 'VA'), where('state', '=', 'DC'));
        var salaryAndStateClause = and(where('salary', '>=', 100000), whereStateClause);
        var query = baseQuery().where(salaryAndStateClause);
        assertQueryResults(query, rows(0, 2, 4));
    });

    it('query WHERE field is null or missing', function() {
        var whereLastNameNullClause = where('lastname', '=', null);
        var query = baseQuery().where(whereLastNameNullClause);
        var expectedData = getJSONFixture('null_or_missing.json');
        assertQueryResults(query, expectedData);
    });

    it('query WHERE field is not null and not missing', function() {
        var whereLastNameNotNullClause = where('lastname', '!=', null);
        var query = baseQuery().where(whereLastNameNotNullClause);
        var expectedData = getJSONFixture('not_null_and_not_missing.json');
        assertQueryResults(query, expectedData);
    });

    it('group by and sort', function() {
        var query = baseQuery()
            .groupBy('state', 'city')
            .sortBy('state', neon.query.ASCENDING, 'city', neon.query.DESCENDING)
            .aggregate(neon.query.SUM, 'salary', 'salary_sum');
        var expectedData = getJSONFixture('groupByStateAsc_cityDesc_aggregateSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by average', function() {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.AVG, 'salary', 'salary_avg');
        var expectedData = getJSONFixture('groupByStateAsc_avgSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by max', function() {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MAX, 'salary', 'salary_max');
        var expectedData = getJSONFixture('groupByStateAsc_maxSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by with generated aggregate field name', function() {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MAX, 'salary');
        var expectedData = getJSONFixture('groupByStateAsc_maxSalary_generated_field_name.json');
        assertQueryResults(query, expectedData);
    });

    it('group by min', function() {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MIN, 'salary', 'salary_min');
        var expectedData = getJSONFixture('groupByStateAsc_minSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by count', function() {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.COUNT, '*', 'counter');
        var expectedData = getJSONFixture('groupByStateAsc_count.json');
        assertQueryResults(query, expectedData);
    });

    it('count all fields', function() {
        var query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'counter');
        var expectedData = getJSONFixture('count.json');
        assertQueryResults(query, expectedData);
    });

    it('count all fields with different name', function() {
        var query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'test');
        var expectedData = getJSONFixture('count.json');
        expectedData[0].test = expectedData[0].counter;
        delete expectedData[0].counter;

        assertQueryResults(query, expectedData);
    });

    it('count all fields', function() {
        var query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'counter');
        var expectedData = getJSONFixture('count.json');
        assertQueryResults(query, expectedData);
    });

    it('count field with missing value', function() {
        // lastname has one record with no data, so count should return 1 less value
        var query = baseQuery()
            .aggregate(neon.query.COUNT, 'lastname', 'counter');
        var expectedData = getJSONFixture('count_missing_field.json');
        assertQueryResults(query, expectedData);
    });

    it('distinct fields', function() {
        var query = baseQuery().distinct().withFields('state').limit(2).sortBy('state', neon.query.ASCENDING);
        var expectedData = getJSONFixture('distinct_limit.json');
        assertQueryResults(query, expectedData);
    });

    it('apply and remove filter', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);

            // verify that if the query is supposed to include the filtered data, all data is returned
            runs(function() {
                assertQueryResults(baseQuery().ignoreFilters(), allData);
                runs(function() {
                    // apply another filter and make sure both are applied
                    var salaryFilter = baseFilter().where('salary', '>', 85000);
                    sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, salaryFilter]);
                    runs(function() {
                        expectedData = rows(2, 5);
                        assertQueryResults(baseQuery(), expectedData);
                        runs(function() {
                            // remove the filter key and re-execute the query
                            sendMessageAndWait(neon.eventing.Messenger.prototype.removeFilter, filterId);
                            runs(function() {
                                assertQueryResults(baseQuery(), allData);
                            });
                        });
                    });
                });
            });
        });
    });

    it('ignore specific filters', function() {
        var dcStateFilterId = "dcStateFilter";
        var salaryFilterId = "salaryFilterId";
        sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [dcStateFilterId, dcStateFilter]);
        runs(function() {
            runs(function() {
                // apply another filter and make sure both are applied
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [salaryFilterId, salaryFilter]);
                runs(function() {
                    assertQueryResults(baseQuery().ignoreFilters([dcStateFilterId, salaryFilterId]), allData);
                    sendMessageAndWait(neon.eventing.Messenger.prototype.clearFilters);
                    runs(function() {
                        assertQueryResults(baseQuery(), allData);
                    });
                });
            });
        });
    });

    it('clear filters', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter]);
        runs(function() {
            sendMessageAndWait(neon.eventing.Messenger.prototype.clearFilters);
            runs(function() {
                assertQueryResults(baseQuery(), allData);
            });
        });
    });

    it('replace filter', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.replaceFilter, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);

            runs(function() {
                // replace filter and make sure new one is applied.
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                sendMessageAndWait(neon.eventing.Messenger.prototype.replaceFilter, [filterId, salaryFilter]);
                runs(function() {
                    expectedData = rows(0, 2, 4, 5, 6);
                    assertQueryResults(baseQuery(), expectedData);
                    runs(function() {
                        // remove the filter key and re-execute the query
                        sendMessageAndWait(neon.eventing.Messenger.prototype.removeFilter, filterId);
                        runs(function() {
                            assertQueryResults(baseQuery(), allData);
                        });
                    });
                });
            });
        });
    });

    it('apply and remove selection', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery().selectionOnly(), expectedData);

            // verify that we can still get back all data the data
            runs(function() {
                assertQueryResults(baseQuery().ignoreFilters(), allData);
                runs(function() {
                    // apply another selection and make sure both are applied
                    var salaryFilter = baseFilter().where('salary', '>', 85000);
                    sendMessageAndWait(neon.eventing.Messenger.prototype.addSelection, [filterId, salaryFilter]);
                    runs(function() {
                        expectedData = rows(2, 5);
                        assertQueryResults(baseQuery().selectionOnly(), expectedData);
                        runs(function() {
                            // remove the selection and re-execute the query
                            sendMessageAndWait(neon.eventing.Messenger.prototype.removeSelection, filterId);
                            runs(function() {
                                assertQueryResults(baseQuery().selectionOnly(), []);
                            });
                        });
                    });
                });
            });
        });
    });

    it('clear selection', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter]);
        runs(function() {
            sendMessageAndWait(neon.eventing.Messenger.prototype.clearSelection);
            runs(function() {
                assertQueryResults(baseQuery().selectionOnly(), []);
            });
        });
    });

    it('replace selection', function() {
        sendMessageAndWait(neon.eventing.Messenger.prototype.replaceSelection, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery().selectionOnly(), expectedData);

            runs(function() {
                // replace filter and make sure new one is applied.
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                sendMessageAndWait(neon.eventing.Messenger.prototype.replaceSelection, [filterId, salaryFilter]);
                runs(function() {
                    expectedData = rows(0, 2, 4, 5, 6);
                    assertQueryResults(baseQuery().selectionOnly(), expectedData);
                    runs(function() {
                        // remove the selection and re-execute the query
                        sendMessageAndWait(neon.eventing.Messenger.prototype.removeSelection, filterId);
                        runs(function() {
                            assertQueryResults(baseQuery().selectionOnly(), []);
                        });
                    });
                });
            });
        });
    });

    it('sends filter events to the callback functions', function() {
        var addEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter]);
        runs(function() {
            assertEventType("ADD", addEvent.get());
            // replace filter and make sure new one is applied.
            var salaryFilter = baseFilter().where('salary', '>', 85000);
            var replaceEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.replaceSelection, [filterId, salaryFilter]);
            runs(function() {
                assertEventType("REPLACE", replaceEvent.get());
                runs(function() {
                    // remove the filter key and re-execute the query
                    var removeEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.removeSelection, filterId);
                    runs(function() {
                        assertEventType("REMOVE", removeEvent.get());
                    });
                });
            });
        });
    });

    it('sends filter events to the callback functions', function() {
        var addEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter]);
        runs(function() {
            assertEventType("ADD", addEvent.get());
            // replace filter and make sure new one is applied.
            var salaryFilter = baseFilter().where('salary', '>', 85000);
            var replaceEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.replaceFilter, [filterId, salaryFilter]);
            runs(function() {
                assertEventType("REPLACE", replaceEvent.get());
                runs(function() {
                    // remove the filter key and re-execute the query
                    var removeEvent = sendMessageAndWait(neon.eventing.Messenger.prototype.removeFilter, filterId);
                    runs(function() {
                        assertEventType("REMOVE", removeEvent.get());
                    });
                });
            });
        });
    });

    it('group by month', function() {
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING);
        var expectedData = getJSONFixture('groupByMonth.json');
        assertQueryResults(query, expectedData);
    });

    it('group by year', function() {
        var groupByYearClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, 'hiredate', 'hire_year');
        var query = baseQuery().groupBy(groupByYearClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_year', neon.query.ASCENDING);
        var expectedData = getJSONFixture('groupByYear.json');
        assertQueryResults(query, expectedData);
    });

    it('group by with limit', function() {
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).limit(1);
        // we should only get the first element since we're limiting the query to 1 result
        var expectedData = getJSONFixture('groupByMonth.json').slice(0, 1);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE less than', function() {
        var query = baseQuery().where('salary', '<', 61000);
        var expectedData = rows(3, 7);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE less than or equal', function() {
        var query = baseQuery().where('salary', '<=', 60000);
        var expectedData = rows(3, 7);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE greater than', function() {
        var query = baseQuery().where('salary', '>', 118000);
        var expectedData = rows(2);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE greater than or equal', function() {
        var query = baseQuery().where('salary', '>=', 118000);
        var expectedData = rows(2, 4);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE not equal', function() {
        var query = baseQuery().where('state', '!=', 'VA');
        var expectedData = rows(1, 2, 5, 6);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE IN', function() {
        var query = baseQuery().where('state', 'in', ['MD', 'DC']);
        var expectedData = rows(1, 2, 5, 6);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE not IN', function() {
        var query = baseQuery().where('state', 'notin', ['VA', 'DC']);
        var expectedData = rows(6);
        assertQueryResults(query, expectedData);
    });

    it('should call an error callback if an error occurs', function() {
        var query = baseQuery().groupBy('unknown').aggregate('unknown', 'unknown', 'unknown');
        var successCallback = jasmine.createSpy('success');
        var errorCallback = jasmine.createSpy('error');
        connection.executeQuery(query, successCallback, errorCallback);
        waitsFor(function() {
            return errorCallback.wasInvoked();
        });
        runs(function() {
            expect(successCallback).not.toHaveBeenCalled();
            expect(errorCallback).toHaveBeenCalled();
        });
    });

    it('query with limit', function() {
        var query = baseQuery().limit(2);
        var result = executeQueryAndWait(connection.executeQuery, query);
        runs(function() {
            expect(result.get().data.length).toEqual(2);
        });
    });

    it('query with offset', function() {
        var query = baseQuery().sortBy('salary', neon.query.ASCENDING).offset(4);
        var expectedData = getJSONFixture('offset.json');
        assertQueryResults(query, expectedData);
    });

    it('query near location', function() {
        var center = new neon.util.LatLon(11.95, 19.5);
        var query = baseQuery().withinDistance('location', center, 35, neon.query.MILE);
        var expectedData = rows(2, 0);
        assertQueryResults(query, expectedData);
    });

    it('query near location and filter on attributes', function() {
        var center = new neon.util.LatLon(11.95, 19.5);
        var withinDistanceClause = neon.query.withinDistance('location', center, 35, neon.query.MILE);
        var dcStateClause = neon.query.where('state', '=', 'DC');
        var whereClause = neon.query.and(withinDistanceClause, dcStateClause);
        var query = baseQuery().where(whereClause);

        var expectedData = rows(2);
        assertQueryResults(query, expectedData);
    });

    it('query with date clause as value', function() {
        var whereDateBetweenClause = and(where('hiredate', '>=', '2011-10-15T00:00:00Z'), where('hiredate', '<=', '2011-10-17T00:00:00Z'));
        var query = baseQuery().where(whereDateBetweenClause);
        assertQueryResults(query, rows(1, 2));
    });

    it('concatenates the results of a query group', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        var expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults(queryGroup, expectedData);
    });

    it('query group uses filters', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = getJSONFixture('queryGroupFiltered.json');
            assertQueryGroupResults(queryGroup, expectedData);
            runs(function() {
                sendMessageAndWait(neon.eventing.Messenger.prototype.removeFilter, filterId);
                runs(function() {
                    expectedData = getJSONFixture('queryGroup.json');
                    assertQueryGroupResults(queryGroup, expectedData);
                });
            });
        });
    });

    it('query group can ignore filters', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        queryGroup.ignoreFilters();
        sendMessageAndWait(neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter]);
        runs(function() {
            var expectedData = getJSONFixture('queryGroup.json');
            assertQueryGroupResults(queryGroup, expectedData);
            runs(function() {
                sendMessageAndWait(neon.eventing.Messenger.prototype.removeFilter, filterId);
            });
        });
    });

    it('query group uses selection', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        queryGroup.selectionOnly();
        sendMessageAndWait(neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter]);
        runs(function() {
            // queryGroupFiltered.json is used since we select the same data we filter on in the tests
            var expectedData = getJSONFixture('queryGroupFiltered.json');
            assertQueryGroupResults(queryGroup, expectedData);
            runs(function() {
                sendMessageAndWait(neon.eventing.Messenger.prototype.removeSelection, filterId);
            });
        });
    });

    it('gets column metadata', function() {
        var expected =
            [{
                columnName: "field1",
                numeric: true,
                logical: true,
                temporal: true,
                array: true,
                object: true,
                text: true,
                heterogeneous: true,
                nullable: true
            },{
                columnName: "field2",
                numeric: false,
                logical: false,
                temporal: false,
                array: false,
                object: false,
                text: false,
                heterogeneous: false,
                nullable: false
            }];
        var actual;
        connection.getColumnMetadata('database1', 'table1', function(columnMetadata) {
            actual = columnMetadata;
        });
        waitsFor(function() {
            return 'undefined' !== typeof actual;
        });
        runs(function() {
            expect(actual).toEqual(expected);
        });
    });

    it('query with transform added by transform loader', function() {
        var query = baseQuery();
        query.transform(new neon.query.Transform("Sample"));

        var transformedData = [];
        for(var i = 0; i < allData.length; i++) {
            transformedData[i] = allData[i];
            transformedData[i].foo = "bar";
        }

        assertQueryResults(query, transformedData);
    });

    /**
     * Executes the specified query and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryResults(query, expectedData) {
        doAssertQueryResults(connection.executeQuery, query, expectedData);
    }

    /**
     * Executes the specified query group and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryGroupResults(query, expectedData) {
        doAssertQueryResults(connection.executeQueryGroup, query, expectedData);
    }

    function doAssertQueryResults(queryMethod, query, expectedData) {
        assertAsyncQueryResults(queryMethod, query, expectedData);
    }

    /**
     * Executes the specified async function with the given argument. The function must also take a second
     * argument with a callback to invoke when the operation completes and a third argument that is an error callback
     * @param asyncFunction The asynchronous function to call
     * @param query The query to pass to the function
     * @param expectedData The data expected to be returned from the function
     */
    function assertAsyncQueryResults(asyncFunction, query, expectedData) {
        var result = executeQueryAndWait(asyncFunction, query);
        runs(function() {
            expect(result.get().data).toEqual(expectedData);
        });
    }

    /**
     * Asserts that the event has the expected type and the dataset is correct
     * @param eventType
     * @param event
     * @method assertEventType
     */
    function assertEventType(eventType, event) {
        expect(event.type).toEqual(eventType);
        if(event.addedFilter) {
            expect(event.addedFilter.databaseName).toEqual(databaseName);
            expect(event.addedFilter.tableName).toEqual(tableName);
        }
        if(event.removedFilter) {
            expect(event.removedFilter.databaseName).toEqual(databaseName);
            expect(event.removedFilter.tableName).toEqual(tableName);
        }
    }

    /**
     * Creates an array from the entire dataset that has the specified rows
     * @param indices The indices of the rows from the original data
     * @return
     */
    function rows() {
        var data = [];
        neon.util.arrayUtils.argumentsToArray(arguments).forEach(function(index) {
            data.push(allData[index]);
        });
        return data;
    }

    /**
     * Returns a query that is configured to select from the test data source
     * @return {neon.query.Query}
     */
    function baseQuery() {
        return new neon.query.Query().selectFrom(databaseName, tableName);
    }

    /**
     * Returns a filter that is configured to select from the test data source
     * @return {neon.query.Filter}
     */
    function baseFilter() {
        return new neon.query.Filter().selectFrom(databaseName, tableName);
    }
});
