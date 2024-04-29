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

pluginManagement {
		repositories {
				mavenCentral()
				gradlePluginPortal()
				maven("https://oss.sonatype.org/content/repositories/snapshots")
				maven("https://maven.architectury.dev/")
				maven("https://maven.fabricmc.net")
				maven(url = "https://jitpack.io/")
				maven(url = "https://maven.minecraftforge.net/")
				maven(url = "https://repo.spongepowered.org/maven/")
				maven(url = "https://repo.sk1er.club/repository/maven-releases/")
				maven(url = "https://maven.architectury.dev/")
		}
		resolutionStrategy {
				eachPlugin {
						when (requested.id.id) {
								"gg.essential.loom" -> useModule("gg.essential:architectury-loom:${requested.version}")
						}
				}
		}
}

include("oneconfigquarantine")
include("annotations")
rootProject.name = "NotEnoughUpdates"
