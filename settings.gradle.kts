pluginManagement {
    repositories {
        jcenter()
        maven("https://jitpack.io")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/maven")
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "net.minecraftforge.gradle.forge" -> useModule("com.github.asbyth:ForgeGradle:${requested.version}")
                "org.spongepowered.mixin" -> useModule("com.github.LxGaming:MixinGradle:${requested.version}")
            }
        }
    }
}