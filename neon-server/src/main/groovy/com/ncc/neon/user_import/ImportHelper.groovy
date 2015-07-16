/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.user_import

import org.apache.commons.io.LineIterator

/**
 * Adds, removes, and modifies fields of user-given data inside a generic data store.
 */
public interface ImportHelper {
    /**
     * Takes a LineIterator that points to a spreadsheet, and adds the data from the spreadsheet
     * into a data store running at the given host, with the given identifier for the added data.
     * Also guesses at the data type of each field found for entries in the spreadsheet, and
     * returns those guesses on completion.
     * @param host The host on which the data store is running.
     * @param identifier The identifier with which to associate the added data.
     * @param reader A LineIterator pointing to the data that should be added.
     * @return A list of {@link FieldTypePair} objects, containing record fields and guesses as to their types.
     */
    public List uploadData(String host, String identifier, LineIterator reader)

    /**
     * Drops a set of user-given data from a data store, given its identifier.
     * @paramhost The host on which the data store is running.
     * @param identifier The identifier associated with the data to be dropped.
     * @return True if the data was successfully dropped, false (or an exception) otherwise.
     */
    public boolean dropData(String host, String identifier)

    public List convertFields(String host, String identifier, UserFieldDataBundle bundle)
}