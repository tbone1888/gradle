/*
 * Copyright 2025 the original author or authors.
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

package org.gradle.integtests.tooling.r813


import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.test.precondition.Requires
import org.gradle.test.preconditions.IntegTestPreconditions
import org.gradle.tooling.ProjectConnection

@ToolingApiVersion(">=8.13")
class AbstractLongRunningOperationCrossVersionTest extends ToolingApiSpecification {

    @Requires(
        value = IntegTestPreconditions.NotEmbeddedExecutor,
        reason = "In order to pass JVM arguments to the Gradle daemon, we need to use the external executor."
    )
    def "jvm argument added by #addJvmArguments should not reset lists of JVM arguments"() {
        given:
//        requireIsolatedUserHome()
//        file("user-home-dir").file("gradle.properties") << "org.gradle.jvmargs=-Dgradle-properties-arg -Xmx2g"

        buildFile """
import java.lang.management.ManagementFactory

List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().inputArguments
println("JVM arguments")
println(jvmArgs)
file("jvm-args.txt").text = jvmArgs.join("\\n")

println("------")

List<String> systemProperties = System.getProperties().keySet().toList()
println("System properties")
println(systemProperties)
file("system-properties.txt").text = System.getProperties().keySet().join("\\n")
        """
        propertiesFile << "org.gradle.jvmargs=-Dgradle-properties-arg -Xmx2g"

        when:
        withConnection { ProjectConnection connection ->
            connection
                .newBuild()
                .forTasks("help")
                .addJvmArguments("-Dadd-jvm-arg")
                .run()
        }

        then:
        def args = file("system-properties.txt").text.split("\n")
        verifyAll {
            args.contains("gradle-properties-arg")
            args.contains("add-jvm-arg")
        }
    }

}
