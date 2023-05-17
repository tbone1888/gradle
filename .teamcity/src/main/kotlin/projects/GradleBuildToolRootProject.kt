package projects

import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle

class GradleBuildToolRootProject() : Project({
    buildType {
        id("reproducer")
        name = "reproducer"
        vcs {
            root(DslContext.settingsRoot)
        }
        requirements {
            contains("teamcity.agent.jvm.os.name", "Linux")
        }
        steps {
            gradle {
                tasks = ":plugin-development:configCacheIntegTest --tests ValidatePluginsIntegrationTest"
                useGradleWrapper = true
            }
        }
    }
})
