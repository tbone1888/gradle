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

plugins {
    id("gradlebuild.distribution.api-java")
}

description = "Class loader scopes, registries and instrumentation"

errorprone {
    disabledChecks.addAll(
        "DefaultCharset", // 4 occurrences
        "ImmutableEnumChecker", // 2 occurrences
        "StreamResourceLeak", // 6 occurrences
        "TypeParameterUnusedInFormals", // 1 occurrences
        "OptionalMapUnusedValue", // 1 occurrences
        "ReturnValueIgnored", // 1 occurrences
        "UnusedMethod", // 1 occurrences
    )
}

strictCompile {
    ignoreRawTypes() // some raw types remain, though they are required
}

packageCycles {
    excludePatterns.add("org/gradle/**")
}

dependencies {
    api(projects.baseServices)
    api(projects.buildProcessServices)
    api(projects.concurrent)
    api(projects.coreApi)
    api(projects.execution)
    api(projects.fileCollections)
    api(projects.fileTemp)
    api(projects.files)
    api(projects.functional)
    api(projects.hashing)
    api(projects.instrumentationReporting)
    api(projects.logging)
    api(projects.modelCore)
    api(projects.normalizationJava)
    api(projects.persistentCache)
    api(projects.snapshots)

    api(libs.groovy)
    api(libs.guava)
    api(libs.inject)
    api(libs.jsr305)

    implementation(projects.inputTracking)
    implementation(projects.loggingApi)

    implementation(libs.asmCommons)
    implementation(libs.commonsCompress)
    implementation(libs.commonsIo)
    implementation(libs.commonsLang)
    implementation(libs.commonsLang3)
    implementation(libs.fastutil)
    implementation(libs.groovyJson)
    implementation(libs.slf4jApi)

    compileOnly(libs.kotlinStdlib) {
        because("it needs to forward calls from instrumented code to the Kotlin standard library")
    }
}

