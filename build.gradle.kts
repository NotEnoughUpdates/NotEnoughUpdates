import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import net.minecraftforge.gradle.user.ReobfMappingType

plugins {
    java
    id("net.minecraftforge.gradle.forge") version "6f53277"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.spongepowered.mixin") version "d75e32e"
}

group = "io.github.moulberry"
val modId = "notenoughupdates"

// Toolchains:

java {
    // Forge Gradle currently prevents using the toolchain: toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    runDir = "run"
    mappings = "stable_22"
}

mixin {
    add(sourceSets.main.get(), "mixins.notenoughupdates.refmap.json")
}

// Dependencies:

repositories {
    mavenCentral()
    flatDir { dirs("deps/") }
    maven("https://repo.spongepowered.org/maven/")
}

val toShadow by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    toShadow("com.fasterxml.jackson.core:jackson-core:2.13.1")
    toShadow("info.bliki.wiki:bliki-core:3.1.0")
}


// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set("NotEnoughUpdates")
    manifest.attributes.run {
        this["Main-Class"] = "NotSkyblockAddonsInstallerFrame"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.notenoughupdates.json"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["FMLAT"] = "notenoughupdates_at.cfg"
    }
}

val autoRelocate by tasks.creating(ConfigureShadowRelocation::class) {
    target = tasks.shadowJar.get()
    prefix = "io.github.moulberry.notenoughdependencies"
}

tasks.shadowJar {
    archiveClassifier.set("dep")
    exclude(
        "module-info.class",
        "LICENSE.txt"
    )
    dependsOn(autoRelocate)
}

tasks.build.get().dependsOn(tasks.shadowJar)

reobf.all { mappingType = ReobfMappingType.SEARGE }

tasks.processResources {
    from(sourceSets.main.get().resources.srcDirs)
    filesMatching("mcmod.info") {
        expand(
            "version" to project.version,
            "mcversion" to minecraft.version
        )
    }
    rename("(.+_at.cfg)".toPattern(), "META-INF/$1")
}

val moveResources by tasks.creating(Copy::class) {
    from(tasks.processResources)
    into("${buildDir}/classes/java/main")
}

tasks.classes { dependsOn(moveResources) }

