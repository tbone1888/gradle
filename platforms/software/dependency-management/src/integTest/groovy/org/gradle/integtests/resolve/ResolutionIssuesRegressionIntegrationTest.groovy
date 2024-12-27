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

package org.gradle.integtests.resolve


import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import spock.lang.Issue

/**
 * Location for complex reproducers for resolution issues, which do not have a more specific
 * home. Ideally, these tests would be more targeted, but minimizing them more is not
 * necessarily feasible or worth the time commitment. Since these tests are still valuable,
 * we can keep them here.
 */
class ResolutionIssuesRegressionIntegrationTest extends AbstractIntegrationSpec {

    @Issue("https://github.com/gradle/gradle/issues/26145#issuecomment-1957776331")
    def "partially minimized reproducer -- android project causes corrupt serialized resolution result"() {
        settingsFile << """
            pluginManagement {
                ${mavenCentralRepository()}
                repositories {
                    google()
                }
            }

            dependencyResolutionManagement {
                ${mavenCentralRepository()}
                repositories {
                    google()
                }
            }

            include(":lifecycle:lifecycle-livedata-core")
            include(":lifecycle:lifecycle-viewmodel")
            include(":lifecycle:lifecycle-viewmodel-savedstate")
        """

        file("lifecycle/lifecycle-livedata-core/build.gradle") << """
            version = "2.8.0-alpha02"
            group = "androidx.lifecycle"

            // There is some failure while resolving this component.
            // We can do this many ways, but one way to do it is with an ambiguous variant selection.
            configurations {
                consumable("debugApiElements") {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Attribute.of("build-type", String), "debug")
                    }
                }
                consumable("releaseApiElements") {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Attribute.of("build-type", String), "release")
                    }
                }
            }
        """

        file("lifecycle/lifecycle-viewmodel-savedstate/build.gradle") << """
            version = "2.8.0-alpha02"
            group="androidx.lifecycle"

            // There is some failure while resolving this component.
            // We can do this many ways, but one way to do it is with an ambiguous variant selection.
            configurations {
                consumable("debugApiElements") {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Attribute.of("build-type", String), "debug")
                    }
                }
                consumable("releaseApiElements") {
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_API))
                        attribute(Attribute.of("build-type", String), "release")
                    }
                }
            }
        """

        file("lifecycle/lifecycle-viewmodel/build.gradle") << """
            version = "2.8.0-alpha02"
            group = "androidx.lifecycle"

            configurations {
                dependencyScope("deps")
                consumable("metadataApiElements") {
                    extendsFrom(deps)
                    attributes {
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment, "non-jvm"))
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, "kotlin-metadata"))
                        attribute(Attribute.of("org.jetbrains.kotlin.platform.type", String), "common")
                    }
                }
            }

            dependencies {
                constraints {
                    deps project(":lifecycle:lifecycle-livedata-core")
                }
            }
        """

        buildFile << """
            plugins {
                // This plugin is needed in order to get the proper AttributeSchema compatibility/disambiguation
                // rules. Once we understand this reproducer better, we can likely construct a graph that does not
                // need these resolution rules.
                id("org.jetbrains.kotlin.jvm") version "2.0.0"
            }

            configurations {
                dependencyScope("deps")
                resolvable("res") {
                    extendsFrom(deps)
                    attributes {
                        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
                        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment, "non-jvm"))
                        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, "kotlin-metadata"))
                    }
                }
            }

            dependencies {
                deps "androidx.appcompat:appcompat:1.3.0"
                deps "androidx.fragment:fragment-testing:1.4.1"

                constraints {
                    deps project(":lifecycle:lifecycle-livedata-core")
                    deps project(":lifecycle:lifecycle-viewmodel")
                    deps project(":lifecycle:lifecycle-viewmodel-savedstate")
                }
            }

            tasks.register("resolve") {
                def root = configurations.res.incoming.resolutionResult.rootComponent
                doLast {
                    println root.get()
                }
            }
        """

        expect:
        succeeds(":resolve")
    }

    def "graphs causes unattached constraint edge to exist when module goes back to pending"() {

        //region repo

        mavenRepo.module("org.foo", "uber-bom", "1.0")
            .withModuleMetadata()
            .adhocVariants()
            .variant("apiElements", [
                "org.gradle.category": "platform",
                "org.gradle.usage": "java-api"
            ])
            .publish()

        mavenRepo.module("org.bar", "client", "1.1")
            .withModuleMetadata()
            .withVariant("api") {
                dependsOn("org.foo", "uber-bom", "1.0", [
                    "org.gradle.category": "platform"
                ])
                dependsOn("org.bar", "common", "1.1")
                dependsOn("org.baz", "main", null)
                dependsOn("org.baz", "actions", null)
                dependsOn("org.baz", "feature", null)
                dependsOn("org.baz", "feature", null) {
                    requestedCapability("org.baz", "feature-test-fixtures", null)
                }
            }
            .publish()

        mavenRepo.module("org.bar", "client-testing", "1.1")
            .withModuleMetadata()
            .withVariant("api") {
                dependsOn("org.bar", "client", "1.1")
            }
            .publish()

        mavenRepo.module("org.bar", "common", "1.1")
            .withModuleMetadata()
            .withVariant("api") {
                dependsOn("org.foo", "uber-bom", "1.0", [
                    "org.gradle.category": "platform"
                ])
            }
            .publish()

        mavenRepo.module("org.baz", "actions", "1.2")
            .withModuleMetadata()
            .publish()

        mavenRepo.module("org.baz", "bom", "1.2")
            .withModuleMetadata()
            .adhocVariants()
            .variant("apiElements", [
                "org.gradle.category": "platform",
                "org.gradle.usage": "java-api"
            ]) {
                constraint("org.baz", "actions", "1.2")
            }
            .publish()

        mavenRepo.module("org.baz", "feature", "1.2")
            .withModuleMetadata()
            .publish()

        mavenRepo.module("org.junit", "junit-bom", "5.10.2")
            .withModuleMetadata()
            .adhocVariants()
            .variant("apiElements", [
                "org.gradle.category": "platform",
                "org.gradle.usage": "java-api"
            ])
            .publish()

        mavenRepo.module("org.junit", "junit-bom", "5.11.3")
            .withModuleMetadata()
            .adhocVariants()
            .variant("apiElements", [
                "org.gradle.category": "platform",
                "org.gradle.usage": "java-api"
            ])
            .publish()

        mavenRepo.module("org.junit.jupiter", "junit-jupiter-params", "5.11.3")
            .withModuleMetadata()
            .withVariant("api") {
                dependsOn("org.junit", "junit-bom", "5.11.3") {
                    attribute("org.gradle.category", "platform")
                    endorseStrictVersions = true
                }
            }
            .publish()

        //endregion

        //region main

        file("main/main/build.gradle.kts") << """
            plugins {
              id("java-library")
            }

            dependencies {
              api(enforcedPlatform(project(":bom")))
            }
        """

        file("main/actions/build.gradle.kts") << """
            plugins {
              id("java-library")
            }

            group = "org.baz"
        """

        file("main/external/build.gradle.kts") << """
            plugins {
              id("java-library")
            }

            dependencies {
              api(enforcedPlatform(project(":bom")))
            }
        """

        file("main/bom/build.gradle.kts") << """
            plugins {
              id("java-platform")
            }

            group = "org.baz"

            dependencies {
              constraints {
                api(project(":actions"))
                api(project(":feature"))
              }
            }
        """

        file("main/feature/build.gradle.kts") << """
            plugins {
              id("java-library")
              id("java-test-fixtures")
            }

            group = "org.baz"

            dependencies {
              api(enforcedPlatform(project(":bom")))
            }
        """

        file("main/settings.gradle.kts") << """
            include(":main")
            include(":actions")
            include(":external")
            include(":bom")
            include(":feature")
        """

        //endregion

        file("settings.gradle.kts") << """
            includeBuild(".")

            dependencyResolutionManagement {
              repositories {
                maven(url = "${mavenRepo.uri.toString()}") {
                  metadataSources {
                    gradleMetadata()
                  }
                }
              }
            }

            includeBuild("main") {
              dependencySubstitution {
                substitute(module("org.baz:main")).using(project(":main"))
                substitute(module("org.baz:external")).using(project(":external"))
              }
            }

            include(":uber-bom")
            include(":base")
        """

        file("uber-bom/build.gradle.kts") << """
            plugins {
              id("java-platform")
            }

            group = "org.foo"

            javaPlatform {
              allowDependencies()
            }

            dependencies {
              api(platform("org.baz:bom:1.2")) {
                (this as ModuleDependency).doNotEndorseStrictVersions()
              }
            }
        """

        file("base/build.gradle.kts") << """
            plugins {
              id("java-library")
            }

            configurations.all {
              resolutionStrategy {
                preferProjectModules()
              }
            }

            dependencies {
                implementation(platform(project(":uber-bom")))
                implementation(platform("org.junit:junit-bom:5.10.2"))
                implementation("org.bar:client:1.1")
                implementation("org.baz:external")
                implementation("org.bar:client-testing:1.1")
                implementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
            }
        """

        expect:
        succeeds(":base:dependencies", "--configuration", "compileClasspath")
    }
}
