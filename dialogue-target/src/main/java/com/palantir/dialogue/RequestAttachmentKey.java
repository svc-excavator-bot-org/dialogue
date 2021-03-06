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

package com.palantir.dialogue;

import com.google.common.annotations.Beta;
import com.palantir.logsafe.Preconditions;

@Beta
public final class RequestAttachmentKey<V> {

    private final Class<V> valueClazz;

    private RequestAttachmentKey(Class<V> valueClazz) {
        this.valueClazz = valueClazz;
    }

    void checkIsInstance(V value) {
        assert valueClazz.isInstance(value) : "Value not instance of class " + valueClazz;
    }

    @SuppressWarnings({"unchecked", "RawTypes"})
    public static <T> RequestAttachmentKey<T> create(Class<? super T> valueClazz) {
        Preconditions.checkNotNull(valueClazz, "valueClazz");
        return new RequestAttachmentKey(valueClazz);
    }
}
