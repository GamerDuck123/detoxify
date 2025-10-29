rootProject.name = "Detoxify"
include("common", "paper", "fabric", "neoforge", "spigot", "bungeecord", "velocity")

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    }
}