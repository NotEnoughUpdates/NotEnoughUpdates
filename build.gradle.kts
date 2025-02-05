/*
 * Copyright (C) 2022-2024 NotEnoughUpdates contributors
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


import com.xpdustry.ksr.kotlinRelocate
import neubs.CustomSignTask
import neubs.DownloadBackupRepo
import neubs.NEUBuildFlags
import neubs.applyPublishingInformation
import neubs.setVersionFromEnvironment
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
	id("net.kyori.blossom") version "2.1.0"
	id("com.xpdustry.ksr") version "1.0.0"
}


apply<NEUBuildFlags>()

// Build metadata

group = "io.github.moulberry"

val baseVersion = setVersionFromEnvironment()

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
		"client" {
			if (SystemUtils.IS_OS_MAC_OSX) {
				vmArgs.remove("-XstartOnFirstThread")
			}
			vmArgs.add("-Xmx4G")
		}
		"server" {
			isIdeConfigGenerated = false
		}
	}
	forge {
		accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
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
	maven("https://repo.nea.moe/releases")
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

val mixinRTDependencies: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}

configurations {
	val main = getByName(sourceSets.main.get().compileClasspathConfigurationName)
}

dependencies {
	minecraft("com.mojang:minecraft:1.8.9")
	mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
	forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")


	// Please keep this version in sync with KotlinLoadingTweaker
	implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
	kotlinDependencies(kotlin("stdlib"))
	kotlinDependencies(kotlin("reflect"))

	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.0.0")
	implementation("com.google.auto.service:auto-service-annotations:1.0.1")

	compileOnly(ksp(project(":annotations"))!!)
	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor("org.projectlombok:lombok:1.18.24")

	shadowImplementation("com.mojang:brigadier:1.0.18")
	shadowImplementation("moe.nea:libautoupdate:1.3.1")
	shadowImplementation(libs.nealisp) {
		exclude("org.jetbrains.kotlin")
	}

	mixinRTDependencies("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
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
	devEnv("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
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

tasks.named<Test>("test") {
	useJUnitPlatform()
	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
	this.javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
}
val badJars = layout.buildDirectory.dir("badjars")

tasks.named("jar", Jar::class) {
	archiveClassifier.set("named")
	destinationDirectory.set(badJars)
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
		this["FMLAT"] = "accesstransformer.cfg"
	}
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
	archiveClassifier.set("")
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
	destinationDirectory.set(project.layout.buildDirectory.dir("wrapperjars"))
	from(kotlinDependencies)
	into("neu-kotlin-libraries-wrapped")
}
val mixinDependencyCollectionJar by tasks.creating(Zip::class) {
	archiveFileName.set("mixin-libraries-wrapped.jar")
	destinationDirectory.set(project.layout.buildDirectory.dir("wrapperjars"))
	from(mixinRTDependencies)
	into("neu-mixin-libraries-wrapped")
}

val includeBackupRepo by tasks.registering(DownloadBackupRepo::class) {
	this.branch.set("master")
	this.outputDirectory.set(layout.buildDirectory.dir("downloadedRepo"))
}


tasks.shadowJar {
	archiveClassifier.set("dep-dev")
	configurations = listOf(shadowImplementation, shadowApi, shadowOnly)
	destinationDirectory.set(badJars)
	archiveBaseName.set("NotEnoughUpdates")
	exclude("**/module-info.class", "LICENSE.txt")
	dependencies {
		exclude {
			it.moduleGroup.startsWith("org.apache.") || it.moduleName in
				listOf("logback-classic", "commons-logging", "commons-codec", "logback-core")
		}
	}
	from(kotlinDependencyCollectionJar)
	from(mixinDependencyCollectionJar)
	dependsOn(kotlinDependencyCollectionJar)
	dependsOn(mixinDependencyCollectionJar)
	fun relocate(name: String) = kotlinRelocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
	relocate("com.mojang.brigadier")
	relocate("io.github.moulberry.moulconfig")
	relocate("moe.nea.libautoupdate")
	relocate("moe.nea.lisp")
	mergeServiceFiles()
}

tasks.assemble.get().dependsOn(remapJar)

tasks.processResources {
	from(tasks["generateBuildFlags"])
	from(includeBackupRepo)
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
	this.blossom {
		this.javaSources {
			this.property("neuVersion", baseVersion)
		}
	}
}

tasks.register("signRelease", CustomSignTask::class)

applyPublishingInformation(
	"deobf" to tasks.jar,
	"all" to tasks.remapJar,
	"sources" to tasks["sourcesJar"],
)
