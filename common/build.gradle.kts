dependencies {
    compileOnly(libs.mixin)
    annotationProcessor("${libs.mixin.get()}:processor")

    implementation(libs.extras)
    annotationProcessor(libs.extras)

    compileOnly(libs.onnxruntime)
    compileOnly(libs.tokenizers)
    implementation(libs.configurate.hocon)
}

neoForge {
    neoFormVersion = "${libs.versions.minecraft.get()}-${libs.versions.neoform.get()}"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
