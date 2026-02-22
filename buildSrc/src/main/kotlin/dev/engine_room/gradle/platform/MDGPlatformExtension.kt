package dev.engine_room.gradle.platform

import net.neoforged.moddevgradle.dsl.ModModel
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the

open class MDGPlatformExtension(val project: Project) {
    fun setupMDGMod(vararg sourceSets: SourceSet) {
        val sourceSetsProperty = project.objects.listProperty(SourceSet::class)
        sourceSetsProperty.addAll(*sourceSets)

        // Gross, but I think I need to do it this way to have getModSourceSets return anything useful
        val mod: ModModel = object: ModModel() {
            override fun getName(): String {
                return "main"
            }

            override fun getModSourceSets(): ListProperty<SourceSet>? {
                return sourceSetsProperty
            }
        }
        project.the<NeoForgeExtension>().mods.add(mod)
    }

    fun setupMDGRuns() {
        val mod: ModModel = project.the<NeoForgeExtension>().mods.getByName("main")

        val client: RunModel = project.objects.newInstance(RunModel::class, "client", project, listOf(mod))
        client.client()
        // Turn on our own debug flags
        client.systemProperty("flw.dumpShaderSource", "true")
        client.systemProperty("flw.debugMemorySafety", "true")

        // Turn on mixin debug flags
        client.systemProperty("mixin.debug.export", "true")
        client.systemProperty("mixin.debug.verbose", "true")

        // 720p baby!
        listOf("--width", "1280", "--height", "720").forEach(client::programArgument)


        // We're a client mod, but we need to make sure we correctly render when playing on a server.
        val server: RunModel = project.objects.newInstance(RunModel::class, "server", project, listOf(mod))
        server.server()
        server.programArgument("--nogui")

        project.the<NeoForgeExtension>().runs.addAll(listOf(client, server))
    }

    //TODO archless: test mod
}
