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
