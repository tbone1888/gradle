/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.build.event.types;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.NonNullApi;
import org.gradle.tooling.internal.protocol.problem.InternalAdditionalDataV2;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

@NonNullApi
public class DefaultAdditionalData implements InternalAdditionalDataV2, Serializable {

    private final Map<String, Object> additionalData;
    private final Object additionalDataCustomInstance;

    public DefaultAdditionalData(Map<String, Object> additionalData, @Nullable Object additionalDataCustomInstance) {
        this.additionalData = ImmutableMap.copyOf(additionalData);
        this.additionalDataCustomInstance = additionalDataCustomInstance;
    }

    @Override
    public Map<String, Object> getAsMap() {
        return additionalData;
    }

    @Nullable
    @Override
    public Object get() {
        return additionalDataCustomInstance;
    }
}
