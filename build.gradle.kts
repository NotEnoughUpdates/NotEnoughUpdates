/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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


import neubs.NEUBuildFlags
import neubs.applyPublishingInformation
import neubs.setVersionFromEnvironment
import java.net.URL

plugins {
	idea
	java
	id("gg.essential.loom") version "0.10.0.+"
	id("dev.architectury.architectury-pack200") version "0.1.3"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("io.github.juuxel.loom-quiltflower") version "1.7.3"
	`maven-publish`
	kotlin("jvm") version "1.8.21"
	id("io.gitlab.arturbosch.detekt") version "1.23.0"
	id("com.google.devtools.ksp") version "1.8.21-1.0.11"
}


apply<NEUBuildFlags>()

// Build metadata

group = "io.github.moulberry"

setVersionFromEnvironment("2.1.1")

// Minecraft configuration:
loom {
	launchConfigs {
		"client" {
			property("mixin.debug", "true")
			property("asmhelper.verbose", "true")
			arg("--tweakClass", "io.github.moulberry.notenoughupdates.loader.NEUDelegatingTweaker")
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
	@Suppress("UnstableApiUsage")
	mixin {
		defaultRefmapName.set("mixins.notenoughupdates.refmap.json")
	}
}


// Dependencies:
repositories {
		mavenCentral()
		mavenLocal()
		maven("https://maven.notenoughupdates.org/releases")
		maven("https://repo.spongepowered.org/maven/")
		maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
		maven("https://jitpack.io")
		maven("https://repo.polyfrost.cc/releases")
}

val shadowImplementation: Configuration by configurations.creating {
		configurations.implementation.get().extendsFrom(this)
}

val shadowOnly: Configuration by configurations.creating {

}

val shadowApi: Configuration by configurations.creating {
		configurations.api.get().extendsFrom(this)
}

val devEnv: Configuration by configurations.creating {
		configurations.runtimeClasspath.get().extendsFrom(this)
		isCanBeResolved = false
		isCanBeConsumed = false
		isVisible = false
}

val kotlinDependencies: Configuration by configurations.creating {
		configurations.implementation.get().extendsFrom(this)
}

val oneconfigQuarantineSourceSet: SourceSet = sourceSets.create("oneconfig") {
		java {
				srcDir(layout.projectDirectory.dir("src/main/oneconfig"))
		}
}

configurations {
		val main = getByName(sourceSets.main.get().compileClasspathConfigurationName)
		"oneconfigImplementation" {
				extendsFrom(main)
		}
}

dependencies {
		minecraft("com.mojang:minecraft:1.8.9")
		mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
		forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")


		if (project.findProperty("neu.buildflags.oneconfig") == "true") {
				shadowOnly("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+") // Should be included in jar
				runtimeOnly("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+") // Should be included in jar
		}

		"oneconfigCompileOnly"(project(":oneconfigquarantine", configuration = "namedElements"))
		"oneconfigImplementation"(sourceSets.main.get().output)
		"runtimeOnly"(oneconfigQuarantineSourceSet.output)

		// Please keep this version in sync with KotlinLoadingTweaker
		implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
		kotlinDependencies(kotlin("stdlib"))
		kotlinDependencies(kotlin("reflect"))

		ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
		implementation("com.google.auto.service:auto-service-annotations:1.0.1")

		compileOnly(ksp(project(":annotations"))!!)
		compileOnly("org.projectlombok:lombok:1.18.24")
		annotationProcessor("org.projectlombok:lombok:1.18.24")
		"oneconfigAnnotationProcessor"("org.projectlombok:lombok:1.18.24")

		shadowImplementation("com.mojang:brigadier:1.0.18")

		shadowImplementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
				isTransitive = false // Dependencies of mixin are already bundled by minecraft
		}
		annotationProcessor("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
		compileOnly("org.jetbrains:annotations:24.0.1")

		modImplementation(libs.moulconfig)
		shadowOnly(libs.moulconfig)

		@Suppress("VulnerableLibrariesLocal")
		shadowApi("info.bliki.wiki:bliki-core:3.1.0")
		testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
		testAnnotationProcessor("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
		detektPlugins("org.notenoughupdates:detektrules:1.0.0")
		devEnv("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")
}



java {
		withSourcesJar()
		toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Tasks:

tasks.withType(JavaCompile::class) {
		options.encoding = "UTF-8"
		options.isFork = true
}
tasks.named("compileOneconfigJava", JavaCompile::class) {
		doFirst {
				println("oneconfig args: ${this@named.options.compilerArgs}")
		}
}


tasks.named<Test>("test") {
		useJUnitPlatform()
}

tasks.named("jar", Jar::class) {
		from(oneconfigQuarantineSourceSet.output)
}

tasks.withType(Jar::class) {
		archiveBaseName.set("NotEnoughUpdates")
		manifest.attributes.run {
				this["Main-Class"] = "NotSkyblockAddonsInstallerFrame"
				this["TweakClass"] = "io.github.moulberry.notenoughupdates.loader.NEUDelegatingTweaker"
				this["MixinConfigs"] = "mixins.notenoughupdates.json"
				this["FMLCorePluginContainsFMLMod"] = "true"
				this["ForceLoadAsMod"] = "true"
				this["Manifest-Version"] = "1.0"
		}
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
		archiveClassifier.set("dep")
		from(tasks.shadowJar)
		input.set(tasks.shadowJar.get().archiveFile)
		doLast {
				println("Jar name: ${archiveFile.get().asFile}")
		}
}

tasks.remapSourcesJar {
		this.enabled = false
}

/* Bypassing https://github.com/johnrengelman/shadow/issues/111 */
// Use Zip instead of Jar as to not include META-INF
val kotlinDependencyCollectionJar by tasks.creating(Zip::class) {
	archiveFileName.set("kotlin-libraries-wrapped.jar")
	destinationDirectory.set(project.layout.buildDirectory.dir("kotlinwrapper"))
	from(kotlinDependencies)
	into("neu-kotlin-libraries-wrapped")
}

tasks.register("includeBackupRepo") {
	doLast {
		val url = URL("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/prerelease.zip")
		val destinationFolder = project.buildDir.resolve("classes/java/main/assets/notenoughupdates/")
		destinationFolder.mkdirs()
		val destination = destinationFolder.resolve("repo.zip")
		destination.createNewFile()

		destination.outputStream().use {
			url.openStream().copyTo(it)
		}
	}
}


tasks.shadowJar {
	archiveClassifier.set("dep-dev")
	configurations = listOf(shadowImplementation, shadowApi, shadowOnly)
	archiveBaseName.set("NotEnoughUpdates")
	exclude("**/module-info.class", "LICENSE.txt")
	dependencies {
		exclude {
			it.moduleGroup.startsWith("org.apache.") || it.moduleName in
				listOf("logback-classic", "commons-logging", "commons-codec", "logback-core")
		}
	}
	from(oneconfigQuarantineSourceSet.output)
	from(kotlinDependencyCollectionJar)
	dependsOn(kotlinDependencyCollectionJar)
	fun relocate(name: String) = relocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
	relocate("com.mojang.brigadier")
}

tasks.assemble.get().dependsOn(remapJar)

tasks.processResources {
	from(tasks["generateBuildFlags"])
	filesMatching(listOf("mcmod.info", "fabric.mod.json", "META-INF/mods.toml")) {
		expand(
			"version" to project.version, "mcversion" to "1.8.9"
		)
	}
}

val detektProjectBaseline by tasks.registering(io.gitlab.arturbosch.detekt.DetektCreateBaselineTask::class) {
	description = "Overrides current baseline."
	buildUponDefaultConfig.set(true)
	ignoreFailures.set(true)
	parallel.set(true)
	setSource(files(rootDir))
	config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
	baseline.set(file("$rootDir/config/detekt/baseline.xml"))
	include("**/*.kt")
	include("**/*.kts")
	exclude("**/resources/**")
	exclude("**/build/**")
}

idea {
	module {
		// Not using += due to https://github.com/gradle/gradle/issues/8749
		sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
		testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
		generatedSourceDirs =
			generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
	}
}

sourceSets.main {
		output.setResourcesDir(file("$buildDir/classes/java/main"))
}

applyPublishingInformation(
		"deobf" to tasks.jar,
		"all" to tasks.remapJar,
		"sources" to tasks["sourcesJar"],
)
