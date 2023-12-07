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

package org.gradle.api.problems;

import org.gradle.api.Incubating;

import javax.annotation.Nullable;

/**
 * A basic problem location pointing to a specific part of a file.
 *
 * @since 8.6
 */
@Incubating
public interface FileLocation extends ProblemLocation {

    /**
     * The path to the file.
     *
     * @return the file path
     * @since 8.6
     */
    String getPath();

    /**
     * The line number within the file.
     *
     * @return the line number
     * @since 8.6
     */
    @Nullable
    Integer getLine();

    /**
     * The offset on the selected line.
     *
     * @return the column
     * @since 8.6
     */
    @Nullable
    Integer getColumn();

    /**
     * The content of the content starting from {@link #getColumn()}.
     *
     * @return the length
     * @since 8.6
     */
    @Nullable
    Integer getLength();
}