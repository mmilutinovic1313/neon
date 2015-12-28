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

package com.ncc.neon.query.jackson
import org.bson.types.ObjectId
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule


class NeonModule extends SimpleModule{

    NeonModule() {
        super("NeonModule", Version.unknownVersion())
        addSerializer(ObjectId, new ObjectIdSerializer())
        addSerializer(Date, new DateSerializer())
    }

}
