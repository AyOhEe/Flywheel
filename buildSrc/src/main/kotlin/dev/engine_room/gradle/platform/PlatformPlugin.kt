package dev.engine_room.gradle.platform

import org.gradle.api.Plugin
import org.gradle.api.Project

class PlatformPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("platformLoom", LoomPlatformExtension::class.java, project)
        project.extensions.create("platformMDG", MDGPlatformExtension::class.java, project)
    }
}
