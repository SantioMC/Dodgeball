plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "me.santio"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    implementation(project("API"))
}

bukkit {
    main = "me.santio.dodgeball.Dodgeball"
    apiVersion = "1.19"
    description = "A simple dodgeball plugin made for TK"

    authors = listOf("Santio71")
    softDepend = listOf("PlaceholderAPI")

    commands {
        register("game") {
            description = "Main command for dodgeball"
        }
    }
}