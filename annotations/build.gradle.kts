/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

plugins {
		kotlin("jvm")
		java
}

repositories {
		mavenCentral()
}

tasks.withType<JavaCompile> {
		if (JavaVersion.current().isJava9Compatible) {
				options.release.set(8)
		}
}

dependencies {
		implementation(kotlin("stdlib-jdk8"))
		implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.8")
		implementation("com.squareup:kotlinpoet:1.12.0")
		implementation("com.squareup:kotlinpoet-ksp:1.12.0")
}




