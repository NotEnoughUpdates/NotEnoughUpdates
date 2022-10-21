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

package neubs

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun Project.applyPublishingInformation(
    vararg artifacts: Pair<String, Any>
) {
    this.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                for((name, source) in artifacts) {
                    artifact(source) {
                        classifier = name
                    }
                }
                pom {
                    name.set("NotEnoughUpdates")
                    description.set("A feature rich 1.8.9 Minecraft forge mod for Hypixel Skyblock")
                    licenses {
                        license {
                            name.set("GNU Lesser General Public License")
                            url.set("https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/COPYING.LESSER")
                        }
                    }
                    developers {
                        developer {
                            name.set("Moulberry")
                        }
                        developer {
                            name.set("The NotEnoughUpdates Contributors and Maintainers")
                        }
                    }
                }
            }
        }
    }

}
