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

package org.gradle.configurationcache


import org.gradle.integtests.fixtures.configurationcache.ConfigurationCacheProblemsFixture
import org.gradle.internal.Pair
import org.gradle.test.fixtures.file.TestFile
import org.hamcrest.Matcher

import javax.annotation.Nullable
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

import static org.hamcrest.CoreMatchers.startsWith

class IdeSyncFixture {

    private final ConfigurationCacheProblemsFixture configurationCacheProblemsFixture
    private final TestFile rootDir

    IdeSyncFixture(TestFile rootDir) {
        this.configurationCacheProblemsFixture = new ConfigurationCacheProblemsFixture(rootDir)
        this.rootDir = rootDir
    }

    void assertHtmlReportHasProblems(
        @DelegatesTo(value = HasConfigurationCacheProblemsInReportSpec, strategy = Closure.DELEGATE_FIRST) Closure<?> specClosure
    ) {
        def spec = new HasConfigurationCacheProblemsInReportSpec()
        specClosure.delegate = spec
        specClosure()

        spec.validateSpec()

        def reportDir = resolveFirstConfigurationCacheReportDir(rootDir)
        def jsModel = configurationCacheProblemsFixture.readJsModelFromReportDir(reportDir)

        def problemsDiagnostics = jsModel.diagnostics.findAll { it['problem'] != null && it['trace'] != null }

        def actualLocationsWithProblems = problemsDiagnostics.collect {
            def message = configurationCacheProblemsFixture.formatStructuredMessage(it['problem'])
            def location = configurationCacheProblemsFixture.formatTrace(it['trace'])
            Pair.of(location, message)
        }.unique()

        assert jsModel.totalProblemCount == spec.totalProblemsCount
        assert actualLocationsWithProblems.size() == spec.locationsWithProblems.size()
        assert spec.locationsWithProblems.every { expectedLocation ->
            actualLocationsWithProblems.any { actualLocation ->
                if (expectedLocation.left.matches(actualLocation.left)) {
                    expectedLocation.right == actualLocation.right
                } else {
                    false
                }
            }
        }
    }

    private static TestFile resolveFirstConfigurationCacheReportDir(TestFile rootDir) {
        TestFile reportsDir = rootDir.file("build/reports/configuration-cache")
        TestFile reportDir = null
        Files.walkFileTree(reportsDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString() == "configuration-cache-report.html") {
                    reportDir = new TestFile(file.parent.toString())
                    return FileVisitResult.TERMINATE
                }
                return FileVisitResult.CONTINUE
            }
        })

        if (reportDir == null) {
            throw new RuntimeException()
        }

        return reportDir
    }
}

class HasConfigurationCacheProblemsInReportSpec {

    final List<Pair<Matcher<String>, String>> locationsWithProblems = []

    @Nullable
    Integer totalProblemsCount

    void validateSpec() {
        def totalCount = totalProblemsCount ?: locationsWithProblems.size()
        if (totalCount < locationsWithProblems.size()) {
            throw new IllegalArgumentException("Count of total problems can't be lesser than count of unique problems.")
        }
    }

    HasConfigurationCacheProblemsInReportSpec withLocatedProblem(String location, String problem) {
        withLocatedProblem(startsWith(location), problem)
        return this
    }

    HasConfigurationCacheProblemsInReportSpec withLocatedProblem(Matcher<String> location, String problem) {
        locationsWithProblems.add(Pair.of(location, problem))
        return this
    }

    HasConfigurationCacheProblemsInReportSpec withTotalProblemsCount(int totalProblemsCount) {
        this.totalProblemsCount = totalProblemsCount
        return this
    }
}