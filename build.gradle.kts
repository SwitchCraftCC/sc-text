import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("fabric-loom") version "0.12-SNAPSHOT"
  id("io.github.juuxel.loom-quiltflower") version "1.7.1"

  val kotlinVersion: String by System.getProperties()
  kotlin("jvm").version(kotlinVersion)
}

val modVersion: String by project
val mavenGroup: String by project

val minecraftVersion: String by project
val minecraftTargetVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricKotlinVersion: String by project
val fabricVersion: String by project

val archivesBaseName = "sc-text"

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "17"
    apiVersion = "1.7"
    languageVersion = "1.7"
  }
}

repositories {
}

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
  modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
  modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)
  modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)
}

tasks {
  processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") { expand(mutableMapOf(
      "version" to project.version,
      "minecraft_target_version" to minecraftTargetVersion,
      "fabric_kotlin_version" to fabricKotlinVersion,
      "loader_version" to loaderVersion
    )) }
  }

  jar {
    from("LICENSE") {
      rename { "${it}_${archivesBaseName}" }
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
  }

  remapJar {
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    destinationDirectory.set(file("${rootDir}/build/final"))
  }

  loom {
    accessWidenerPath.set(file("src/main/resources/sc-text.accesswidener"))
  }
}
