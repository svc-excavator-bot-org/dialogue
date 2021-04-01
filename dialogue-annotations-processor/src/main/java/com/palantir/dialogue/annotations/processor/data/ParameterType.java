/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
 *
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
 */

package com.palantir.dialogue.annotations.processor.data;

import com.squareup.javapoet.TypeName;
import org.derive4j.Data;

@Data
public interface ParameterType {
    interface Cases<R> {

        R rawBody();

        R body(TypeName serializer, String serializerFieldName);

        R header(String headerName);

        R path();

        R query(String paramName);
    }

    <R> R match(Cases<R> cases);
}