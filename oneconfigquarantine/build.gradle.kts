import net.fabricmc.loom.task.RemapJarTask

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

/**
 * This subproject is a stub project to hold oneconfig dependencies, to prevent those dependencies bleeding over into
 * our the normal NEU codebase. Usually this could be done using just another source set, however due to using legacy
 * arch loom (for now!!!!) we cannot depend on remapped dependencies from only in one source set.
 * */
plugins {
		java
		id("gg.essential.loom")
		id("dev.architectury.architectury-pack200")
}

loom.forge.pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())

repositories {
		mavenCentral()
		maven("https://repo.polyfrost.cc/releases")
}

dependencies {
		minecraft("com.mojang:minecraft:1.8.9")
		mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
		forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

		modApi("cc.polyfrost:oneconfig-1.8.9-forge:0.1.0-alpha+") // Don't you just love 0.1.0-alpha+
}

tasks.withType<JavaCompile> {
		this.enabled = false
}
tasks.withType<RemapJarTask> {
		println(this)
		this.enabled = false
}
