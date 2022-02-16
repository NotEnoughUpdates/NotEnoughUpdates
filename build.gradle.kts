import java.io.ByteArrayOutputStream
import net.minecraftforge.gradle.user.ReobfMappingType
plugins {
    java
    id("net.minecraftforge.gradle.forge") version "6f5327738df"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.spongepowered.mixin") version "d75e32e"
}

group = "io.github.moulberry"
val baseVersion = "2.1"


var buildVersion = properties["BUILD_VERSION"]
if (buildVersion == null) {
    val stdout = ByteArrayOutputStream()
    val execResult = exec {
        commandLine("git", "describe", "--always", "--first-parent", "--abbrev=7")
        standardOutput = stdout
    }
    if (execResult.exitValue == 0)
        buildVersion = String(stdout.toByteArray()).trim()
}

version = baseVersion + (buildVersion?.let { "+$it" } ?: "")


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
    clientJvmArgs.addAll(
        listOf(
            "-Dmixin.debug=true",
            "-Dasmhelper.verbose=true"
        )
    )
    clientRunArgs.addAll(
        listOf(
            "--tweakClass org.spongepowered.asm.launch.MixinTweaker",
            "--mixin mixins.notenoughupdates.json"
        )
    )
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

dependencies {
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("info.bliki.wiki:bliki-core:3.1.0")
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

tasks.shadowJar {
    archiveClassifier.set("dep")
    exclude(
        "module-info.class",
        "LICENSE.txt"
    )
    dependencies {
        include(dependency("org.spongepowered:mixin:0.7.11-SNAPSHOT"))

        include(dependency("commons-io:commons-io"))
        include(dependency("org.apache.commons:commons-lang3"))
        include(dependency("com.fasterxml.jackson.core:jackson-databind:2.10.2"))
        include(dependency("com.fasterxml.jackson.core:jackson-annotations:2.10.2"))
        include(dependency("com.fasterxml.jackson.core:jackson-core:2.10.2"))

        include(dependency("info.bliki.wiki:bliki-core:3.1.0"))
        include(dependency("org.slf4j:slf4j-api:1.7.18"))
        include(dependency("org.luaj:luaj-jse:3.0.1"))
    }
    fun relocate(name: String) = relocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
    relocate("com.fasterxml.jackson")
    relocate("org.eclipse")
    relocate("org.slf4j")
}

tasks.build.get().dependsOn(tasks.shadowJar)

reobf {
    create("shadowJar") {
        mappingType = ReobfMappingType.SEARGE
    }
}

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

val moveResources by tasks.creating {
    doLast {
        ant.withGroovyBuilder {
            "move"(
                "file" to "$buildDir/resources/main",
                "todir" to "$buildDir/classes/java"
            )
        }
    }
    dependsOn(tasks.processResources)
}

tasks.classes { dependsOn(moveResources) }

