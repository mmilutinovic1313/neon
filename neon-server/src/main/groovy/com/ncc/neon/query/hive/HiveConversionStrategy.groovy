package com.ncc.neon.query.hive

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState

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
 * Converts a Query object into a hive based query.
 */

class HiveConversionStrategy {

    private final FilterState filterState
    private final SelectionState selectionState

    HiveConversionStrategy(FilterState filterState, SelectionState selectionState) {
        this.filterState = filterState
        this.selectionState = selectionState
    }

    String convertQueryDisregardingFilters(Query query) {
        helpConvertQuery(query, false)
    }

    String convertQueryWithFilterState(Query query) {
        helpConvertQuery(query, true)
    }

    private String helpConvertQuery(Query query, boolean includeFiltersFromFilterState) {
        StringBuilder builder = new StringBuilder()
        applySelectFromStatement(builder, query)
        applyWhereStatement(builder, query, includeFiltersFromFilterState)
        applyGroupByStatement(builder, query)
        applySortByStatement(builder, query)
        applyLimitStatement(builder, query)
        return builder.toString()
    }

    private static void applySelectFromStatement(StringBuilder builder, Query query) {
        def modifier = query.isDistinct ? "DISTINCT " : ""
        builder << "select ${modifier}" << buildFieldList(query) << " from " << query.filter.databaseName << "." << query.filter.tableName
    }

    private static def buildFieldList(Query query) {
        def fields = []
        query.aggregates.each { aggregate ->
            fields << functionToString(aggregate)
        }
        query.groupByClauses.each { groupBy ->
            fields << groupByClauseToString(groupBy)
        }
        // if there are aggregates in the field, those and the group by fields are the only valid values to return
        // and the hive - jdbc drivers can return some strange results
        // https://issues.apache.org/jira/browse/HIVE-4392,
        // https://issues.apache.org/jira/browse/HIVE-4522
        // so don't allow fields not grouped on
        if (!fields) {
            fields.addAll(query.fields.collect { escapeFieldName(it) })
        }
        return fields.join(", ")
    }

    private static String escapeFieldName(String fieldName) {
        // TODO: NEON-151 field may be null when doing a count operation
        return fieldName?.startsWith("_") ? "`${fieldName}`" : fieldName
    }

    private static String groupByClauseToString(GroupByClause groupBy) {
        groupBy instanceof GroupByFieldClause ? escapeFieldName(groupBy.field) : functionToString(groupBy)
    }


    private static String functionToString(FieldFunction fieldFunction) {
        return "${fieldFunction.operation}(${escapeFieldName(fieldFunction.field)}) as ${fieldFunction.name}"
    }

    private void applyWhereStatement(StringBuilder builder, Query query, boolean includeFiltersFromFilterState) {
        List whereClauses = assembleWhereClauses(query)
        if (includeFiltersFromFilterState) {
            whereClauses.addAll(createWhereClausesForFilters(query))
        }
        HiveWhereClause clause = createWhereClauseParams(whereClauses)
        if (clause) {
            builder << " where " << clause.toString()
        }

    }

    private static void applyGroupByStatement(StringBuilder builder, Query query) {
        def groupByClauses = []
        groupByClauses.addAll(query.groupByClauses)

        if (groupByClauses) {
            // hive doesn't support grouping by the field alias so we actually need to provide the field function again
            builder << " group by " << groupByClauses.collect { groupByClauseToString(it).split(" ")[0] }.join(", ")
        }

    }

    private static void applySortByStatement(StringBuilder builder, Query query) {
        List sortClauses = query.sortClauses
        if (sortClauses) {
            builder << " order by " << sortClauses.collect { escapeFieldName(it.fieldName) + ((it.sortOrder == SortOrder.ASCENDING) ? " ASC" : " DESC") }.join(", ")
        }
    }

    private static void applyLimitStatement(StringBuilder builder, Query query) {
        if (query.limitClause != null) {
            builder << " limit " << query.limitClause.limit
        }
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


    private static HiveWhereClause createWhereClauseParams(List whereClauses) {
        if (!whereClauses) {
            return null
        }
        if (whereClauses.size() == 1) {
            return new HiveWhereClause(whereClause: whereClauses[0])
        }
        return new HiveWhereClause(whereClause: new AndWhereClause(whereClauses: whereClauses))
    }
}
