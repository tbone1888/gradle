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

package org.gradle.api.problems

import org.gradle.api.problems.deprecation.DeprecationReporter
import org.gradle.api.problems.internal.Problem
import org.gradle.util.TestUtil
import spock.lang.Specification

class DeprecationReporterTest extends Specification {

    DeprecationReporter reporter

    def setup() {
        Problems problems = TestUtil.problemsService()
        reporter = problems.deprecationReporter
    }

    def "deprecate generic"() {
        when:
        def problem = reporter.deprecate {
            it.withReason("This is a generic deprecation")
            it.withDetails("""
                This is a generic deprecation,
                with a longer description.
            """.stripIndent())
        }

        then:
        problem instanceof Problem
        verifyAll(problem.definition.id) {
            it.name == "generic"
            it.displayName == "Generic deprecation"
            verifyAll(it.group) {
                it.name == "deprecation"
                it.displayName == "Deprecation"
            }
        }

        problem.contextualLabel == "This is a generic deprecation"
        problem.details == """
            This is a generic deprecation,
            with a longer description.
        """.stripIndent()
    }

    def "deprecate behavior"() {
        reporter.deprecateBehavior("This shouldn't called with that anymore") {

        }
    }

    def "deprecate this method"() {
        reporter.deprecateMethod() {

        }
    }

    def "deprecate other method"() {

    }

}
