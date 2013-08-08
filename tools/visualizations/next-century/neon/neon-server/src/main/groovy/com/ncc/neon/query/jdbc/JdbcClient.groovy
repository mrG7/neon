package com.ncc.neon.query.jdbc

import java.sql.*

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

/**
 * Wrapper for JDBC API
 */
//We have to suppress this warning in order to load the the JDBC driver via DriverManager. See NEON-459
@SuppressWarnings('ClassForName')
class JdbcClient {

    private final Connection connection
    String databaseType
    String dbHostString

    JdbcClient(String driverName, String databaseType, String databaseName, String dbHostString) {
        this.databaseType = databaseType
        this.dbHostString = dbHostString

        Class.forName(driverName)
        this.connection = DriverManager.getConnection("jdbc:" + databaseType + "://" + dbHostString + "/" + databaseName, "", "")
    }

    public List executeQuery(String query) {
        Statement statement
        ResultSet resultSet
        try {
            statement = connection.createStatement()
            resultSet = statement.executeQuery(query)
            return createMappedValuesFromResultSet(resultSet)
        }
        finally {
            resultSet?.close()
            statement?.close()
        }

        return []
    }

    private List createMappedValuesFromResultSet(ResultSet resultSet) {
        List resultList = []
        ResultSetMetaData metadata = resultSet.metaData
        int columnCount = metadata.columnCount
        while (resultSet.next()) {
            def result = [:]
            for (ii in 1..columnCount) {
                result[metadata.getColumnName(ii)] = resultSet.getObject(ii)
            }
            resultList.add(result)
        }
        return resultList
    }

    public void execute(String query) {
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }

    public List<String> getColumnNames(String dataStoreName, String databaseName) {
        String query = "select * from ${dataStoreName}.${databaseName}"

        List list = executeQuery(query)
        if (!list) {
            return []
        }
        list[0].keySet().asList()
    }

    public void close() {
        connection.close()
    }
}
