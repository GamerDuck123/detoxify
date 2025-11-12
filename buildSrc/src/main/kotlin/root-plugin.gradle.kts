plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven("https://jitpack.io/")

    mavenCentral()
}

base {
    archivesName.set("${rootProject.name}-${project.name}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of("21"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
}