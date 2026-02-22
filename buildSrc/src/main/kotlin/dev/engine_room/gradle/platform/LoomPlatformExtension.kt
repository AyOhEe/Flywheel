package dev.engine_room.gradle.platform

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.io.File

open class LoomPlatformExtension(val project: Project) {

    fun setupTestMod(sourceSet: SourceSet) {
        project.tasks.apply {
            val testModJar = register<Jar>("testModJar") {
                from(sourceSet.output)
                val file = File(project.layout.buildDirectory.asFile.get(), "devlibs");
                destinationDirectory.set(file)
                archiveClassifier = "testmod"
            }

            val remapTestModJar = register<RemapJarTask>("remapTestModJar") {
                dependsOn(testModJar)
                inputFile.set(testModJar.get().archiveFile)
                archiveClassifier = "testmod"
                addNestedDependencies = false
                classpath.from(sourceSet.compileClasspath)
            }

            named<Task>("build").configure {
                dependsOn(remapTestModJar)
            }
        }
    }
}
