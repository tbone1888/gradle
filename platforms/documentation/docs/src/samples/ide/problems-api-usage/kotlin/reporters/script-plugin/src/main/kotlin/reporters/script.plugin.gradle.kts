package reporters
import org.gradle.api.problems.IdFactory
import org.gradle.kotlin.dsl.registering

interface Injected {
    @get:Inject val problems: Problems
}

val problems = project.objects.newInstance<Injected>().problems
val problemGroup = IdFactory.instance().createRootProblemGroup("root", "Root Group")

problems.getReporter().report(IdFactory.instance().createProblemId("adhoc-script-deprecation", "Deprecated script plugin", problemGroup)) {
    contextualLabel("Deprecated script plugin 'demo-script-plugin'")
        .severity(Severity.WARNING)
        .solution("Please use 'standard-plugin-2' instead of this plugin")
}

tasks {
    val warningTask by registering {
        doLast {
            problems.getReporter().report(IdFactory.instance().createProblemId("adhoc-task-deprecation", "Deprecated task", problemGroup)) {
                it.contextualLabel("Task 'warningTask' is deprecated")
                    .severity(Severity.WARNING)
                    .solution("Please use 'warningTask2' instead of this task")
            }
        }
    }

    val failingTask by registering {
        doLast {
            problems.getReporter().throwing(IdFactory.instance().createProblemId("broken-task", "Task should not be called", problemGroup), RuntimeException("The 'failingTask' should not be called")) {
                it.contextualLabel("Task 'failingTask' should not be called")
                    .severity(Severity.ERROR)
                    .solution("Please use 'successfulTask' instead of this task")
            }
        }
    }
}
