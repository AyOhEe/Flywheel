plugins {
    idea
    java
    id("net.fabricmc.fabric-loom")  apply false
    id("net.neoforged.moddev") version "2.0.140" apply false
}

println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")

idea {
    // Tell IDEA to always download sources/javadoc artifacts from maven.
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
