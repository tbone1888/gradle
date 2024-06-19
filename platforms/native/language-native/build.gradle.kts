plugins {
    id("gradlebuild.distribution.api-java")
}

description = "Plugins and domain objects for building different native languages"

errorprone {
    disabledChecks.addAll(
        "DefaultCharset", // 1 occurrences
        "JavaLangClash", // 1 occurrences
        "MixedMutabilityReturnType", // 1 occurrences
        "StringCaseLocaleUsage", // 1 occurrences
        "UnusedMethod", // 2 occurrences
        "UnusedVariable", // 10 occurrences
    )
}

dependencies {
    api(projects.baseServices)
    api(projects.buildOperations)
    api(projects.concurrent)
    api(projects.core)
    api(projects.coreApi)
    api(projects.dependencyManagement)
    api(projects.files)
    api(projects.fileCollections)
    api(projects.fileTemp)
    api(projects.hashing)
    api(projects.stdlibJavaExtensions)
    api(projects.modelCore)
    api(projects.persistentCache)
    api(projects.platformBase)
    api(projects.platformNative)
    api(projects.serialization)
    api(projects.serviceLookup)
    api(projects.serviceProvider)
    api(projects.snapshots)

    api(libs.guava)
    api(libs.jsr305)
    api(libs.inject)

    implementation(projects.loggingApi)
    implementation(projects.maven)
    implementation(projects.processServices)
    implementation(projects.publish)
    implementation(projects.versionControl)

    implementation(libs.commonsLang)
    implementation(libs.groovy)
    implementation(libs.slf4jApi)

    testFixturesApi(projects.baseServices) {
        because("Test fixtures export the Named class")
    }
    testFixturesApi(projects.platformBase) {
        because("Test fixtures export the Platform class")
    }

    testFixturesImplementation(projects.internalIntegTesting)
    testFixturesImplementation(testFixtures(projects.platformNative))

    testImplementation(projects.native)
    testImplementation(projects.resources)
    testImplementation(projects.baseServicesGroovy)
    testImplementation(libs.commonsIo)
    testImplementation(testFixtures(projects.serialization))
    testImplementation(testFixtures(projects.core))
    testImplementation(testFixtures(projects.versionControl))
    testImplementation(testFixtures(projects.platformNative))
    testImplementation(testFixtures(projects.platformBase))
    testImplementation(testFixtures(projects.messaging))
    testImplementation(testFixtures(projects.snapshots))

    integTestImplementation(projects.native)
    integTestImplementation(projects.enterpriseOperations)
    integTestImplementation(projects.resources)
    integTestImplementation(libs.nativePlatform)
    integTestImplementation(libs.ant)
    integTestImplementation(libs.jgit)

    testRuntimeOnly(projects.distributionsCore) {
        because("ProjectBuilder tests load services from a Gradle distribution.")
    }
    integTestDistributionRuntimeOnly(projects.distributionsNative)
}

packageCycles {
    excludePatterns.add("org/gradle/language/nativeplatform/internal/**")
}

integTest.usesJavadocCodeSnippets = true

// Remove as part of fixing https://github.com/gradle/configuration-cache/issues/585
tasks.configCacheIntegTest {
    systemProperties["org.gradle.configuration-cache.internal.test-disable-load-after-store"] = "true"
}
