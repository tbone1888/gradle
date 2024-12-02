/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.api.problems.internal.deprecation;

import org.gradle.api.problems.deprecation.DeprecationData;
import org.gradle.api.problems.deprecation.DeprecationDataSpec;
import org.gradle.api.problems.deprecation.DeprecationType;
import org.gradle.api.problems.deprecation.DeprecatedVersion;
import org.gradle.api.problems.internal.AdditionalDataBuilder;

import javax.annotation.Nullable;

public class DefaultDeprecationData implements DeprecationData {

    @Nullable
    private final DeprecationType type;
    @Nullable
    private final DeprecatedVersion removedIn;
    @Nullable
    private final String reason;

    public DefaultDeprecationData(@Nullable DeprecationType type, @Nullable DeprecatedVersion removedIn, @Nullable String reason) {
        this.type = type;
        this.removedIn = removedIn;
        this.reason = reason;
    }

    @Override
    public DeprecationType getType() {
        return type;
    }

    @Override
    public DeprecatedVersion getRemovedIn() {
        return removedIn;
    }

    @Override
    public String getReason() {
        return reason;
    }

    public static AdditionalDataBuilder<DeprecationData> builder(@Nullable DeprecationData from) {
        if(from == null) {
            return new DefaultDeprecationData.DefaultDeprecationDataBuilder();
        }
        return new DefaultDeprecationData.DefaultDeprecationDataBuilder(from);
    }

    private static class DefaultDeprecationDataBuilder implements DeprecationDataSpec, AdditionalDataBuilder<DeprecationData> {
        @Nullable
        private DeprecationType type;
        @Nullable
        private DeprecatedVersion removedIn;
        @Nullable
        private String reason;

        public DefaultDeprecationDataBuilder() {

        }

        public DefaultDeprecationDataBuilder(DeprecationData from) {
            this.type = from.getType();
            this.removedIn = from.getRemovedIn();
            this.reason = from.getReason();
        }

        @Override
        public DeprecationDataSpec type(DeprecationType type) {
            this.type = type;
            return this;
        }

        @Override
        public DeprecationDataSpec removedIn(DeprecatedVersion version) {
            this.removedIn = version;
            return this;
        }

        @Override
        public DeprecationDataSpec reason(String reason) {
            this.reason = reason;
            return this;
        }

        @Override
        public DeprecationData build() {
            return new DefaultDeprecationData(type, removedIn, reason);
        }
    }
}
