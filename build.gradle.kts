/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

plugins {
		idea
		java
		id("gg.essential.loom") version "0.10.0.+"
		id("dev.architectury.architectury-pack200") version "0.1.3"
		id("com.github.johnrengelman.shadow") version "7.1.2"
}


// Build metadata

group = "io.github.moulberry"
val baseVersion = "2.1"


val buildExtra = mutableListOf<String>()
val buildVersion = properties["BUILD_VERSION"] as? String
if (buildVersion != null) buildExtra.add(buildVersion)
if (properties["CI"] as? String == "true") buildExtra.add("ci")

val stdout = ByteArrayOutputStream()
val execResult = exec {
		commandLine("git", "describe", "--always", "--first-parent", "--abbrev=7")
		standardOutput = stdout
		isIgnoreExitValue = true
}
if (execResult.exitValue == 0) {
		buildExtra.add(String(stdout.toByteArray()).trim())
}

val gitDiffStdout = ByteArrayOutputStream()
val gitDiffResult = exec {
		commandLine("git", "status", "--porcelain")
		standardOutput = gitDiffStdout
		isIgnoreExitValue = true
}
if (gitDiffStdout.toByteArray().isNotEmpty()) {
		buildExtra.add("dirty")
}

version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))


// Minecraft configuration:
loom {
		launchConfigs {
				"client" {
						property("mixin.debug", "true")
						property("asmhelper.verbose", "true")
						arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
						arg("--mixin", "mixins.notenoughupdates.json")
				}
		}
		runConfigs {
				"server" {
						isIdeConfigGenerated = false
				}
		}
		forge {
				pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
				mixinConfig("mixins.notenoughupdates.json")
		}
		mixin {
				defaultRefmapName.set("mixins.notenoughupdates.refmap.json")
		}
}


// Dependencies:
repositories {
		mavenCentral()
		mavenLocal()
		maven("https://repo.spongepowered.org/maven/")
		maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
		maven("https://jitpack.io")
}

dependencies {
		minecraft("com.mojang:minecraft:1.8.9")
		mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
		forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

		implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
		annotationProcessor("org.spongepowered:mixin:0.8.4-SNAPSHOT")
		implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
		implementation("info.bliki.wiki:bliki-core:3.1.0")
		testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
		testAnnotationProcessor("org.spongepowered:mixin:0.8.4-SNAPSHOT")
		//	modImplementation("io.github.notenoughupdates:MoulConfig:0.0.1")

		modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")
}



java {
		toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Tasks:

tasks.withType(JavaCompile::class) {
		options.encoding = "UTF-8"
}

tasks.named<Test>("test") {
		useJUnitPlatform()
}

tasks.withType(Jar::class) {
		archiveBaseName.set("NotEnoughUpdates")
		manifest.attributes.run {
				this["Main-Class"] = "NotSkyblockAddonsInstallerFrame"
				this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
				this["MixinConfigs"] = "mixins.notenoughupdates.json"
				this["FMLCorePluginContainsFMLMod"] = "true"
				this["ForceLoadAsMod"] = "true"
		}
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
		archiveClassifier.set("dep")
		from(tasks.shadowJar)
		input.set(tasks.shadowJar.get().archiveFile)
}

tasks.shadowJar {
		archiveClassifier.set("dep-dev")
		exclude(
				"module-info.class", "LICENSE.txt"
		)
		dependencies {
				include(dependency("org.spongepowered:mixin"))

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

tasks.assemble.get().dependsOn(remapJar)

tasks.remapJar{

		doLast{
				println("Jar name :" + archiveFileName.get())
		}
}

val generateBuildFlags by tasks.creating {
		outputs.upToDateWhen { false }
		val t = layout.buildDirectory.file("buildflags.properties")
		outputs.file(t)
		val props = project.properties.filter { (name, value) -> name.startsWith("neu.buildflags.") }
		doLast {
				val p = Properties()
				p.putAll(props)
				t.get().asFile.writer(StandardCharsets.UTF_8).use {
						p.store(it, "Store build time configuration for NEU")
				}
		}
}

tasks.processResources {
		from(generateBuildFlags)
		filesMatching("mcmod.info") {
				expand(
						"version" to project.version, "mcversion" to "1.8.9"
				)
		}
}

sourceSets.main {
		output.setResourcesDir(file("$buildDir/classes/java/main"))
}
