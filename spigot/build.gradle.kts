import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude

dependencies {
    compileOnly("org.spigotmc:spigot-api:${libs.versions.minecraft.get()}-R0.1-SNAPSHOT")

    compileOnly(libs.skript)
    compileOnly(libs.onnxruntime)
    compileOnly(libs.tokenizers)
    compileOnly(libs.configurate)
}

modrinth {
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(libs.versions.minecraft.get())
}

tasks.register<Copy>("copyCommonSources") {
    from("$rootDir/common/src/main/java") {
        exclude("me/gamerduck/${rootProject.property("modid")}/mixin/**")
        into("common/java")
    }
    from("$rootDir/common/src/main/resources") {
        exclude("META-INF/**")
        exclude("templates/**")
        exclude("${rootProject.property("modid")}.accesswidener")
        exclude("${rootProject.property("modid")}.mixins.json")
        into("common/resources")
    }
//    Fill in default config from commons
//    from("$rootDir/common/src/main/resources/templates") {
//        include("${project.property("modid")}.properties")
//        into("common/resources")
//        includeEmptyDirs = false
//
//        filesMatching("**/${project.property("modid")}.properties") {
//            expand(mapOf(
//                "default_path" to "plugins/${project.name}/storage",
//            ))
//        }
//    }

    into("${layout.buildDirectory}/generated/sources")
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory}/generated/sources/common/java")
        }
        resources {
            srcDir("${layout.buildDirectory}/generated/sources/common/resources")
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn("copyCommonSources")
}

tasks {
    processResources {
        dependsOn("copyCommonSources")
        val props = mapOf(
            "name" to rootProject.name,
            "group" to project.group,
            "version" to project.version,
            "description" to project.description,
            "mainFile" to "${rootProject.name}Plugin",
            "apiVersion" to libs.versions.minecraft.get()
        )

        from("src/main/templates") {
            listOf(
                "plugin.yml",
            ).forEach {
                filesMatching(it) {
                    expand(props)
                }
            }
        }
        into(layout.buildDirectory.dir("src/main/resources"))
    }
    build {
        dependsOn("shadowJar")
    }
//    shadowJar {
//        dependencies {
//            exclude(dependency("com.mojang:brigadier"))
//        }
//
//        relocate("me.lucko.commodore", "me.gamerduck.rules.bukkit.commodore")
//        archiveClassifier.set("")
//    }
}
