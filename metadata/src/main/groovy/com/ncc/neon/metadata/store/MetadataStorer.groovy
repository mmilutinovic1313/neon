package com.ncc.neon.metadata.store
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.ActiveDatasetData
import com.ncc.neon.metadata.model.query.DefaultColumnData
import com.ncc.neon.metadata.model.widget.WidgetInitializationData
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
 * An api for storing metadata objects.
 */

class MetadataStorer {

    private final MongoObjectConverter converter
    private final def saveClosure

    MetadataStorer(MetadataConnection connection) {
        this.converter = new MongoObjectConverter()

        this.saveClosure = { String name, data ->
            DBObject document = converter.convertToMongo(data)

            MongoClient mongo = connection.client
            DB database = mongo.getDB("metadata")
            DBCollection widget = database.createCollection(name, null)

            widget.insert(document)
        }
    }

    void store(WidgetInitializationData data) {
        saveClosure("widget", data)
    }

    void store(DefaultColumnData data) {
        saveClosure("column", data)
    }

    void store(ActiveDatasetData data) {
        saveClosure("dataset", data)
    }

}