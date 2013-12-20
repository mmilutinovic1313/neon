package com.ncc.neon.result
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.QueryResult
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
 * Creates a client data object out of a query result and metadata.
 */

class AssembleClientData {

    QueryResult queryResult
    ColumnMetadataList columnMetadataList
    WidgetAndDatasetMetadataList initDataList

    /**
     * @return The query results and metadata packaged in an object
     */

    ClientData createClientData(){
        Map<String, Map<String, Boolean>> metadata = createMetadata(columnMetadataList)
        if(initDataList){
            Map<String, String> idToColumn = createInitData()
            return new InitializingClientData(data: queryResult.data, metadata: metadata, idToColumn: idToColumn)
        }

        new ClientData(data: queryResult.data, metadata: metadata)
    }

    private Map<String, String> createInitData() {
        Map initData = [:]
        initDataList.dataSet.each { WidgetAndDatasetMetadata init ->
            initData.put(init.elementId, init.value)
        }
        return initData
    }

    private Map<String, Map<String, Boolean>> createMetadata(ColumnMetadataList data) {
        Map<String, Map<String, Boolean>> metadata = [:]

        data.dataSet.each{ ColumnMetadata column ->
            Map map = createColumnMap(column)
            metadata.put(column.columnName, map)
        }

        return metadata
    }

    private Map<String, Boolean> createColumnMap(ColumnMetadata column) {
        Map<String, Boolean> map = [:]
        column.properties.each { k, v ->
            if (v == true) {
                map.put(k, v)
            }
        }
        return map
    }

}