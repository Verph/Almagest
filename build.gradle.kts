import groovy.json.JsonSlurper

plugins {
    id("net.neoforged.moddev") version "2.0.137"
    id("com.almostreliable.almostgradle") version "1.0.+"
}

val minecraftVersion: String = "1.21.1"
val neoForgeVersion: String = "21.1.217"
val parchmentVersion: String = "2024.11.17"
val parchmentMinecraftVersion: String = "1.21.1"
val modJavaVersion: String = "21"
val modVersion: String = "1.0"
val modId: String = "almagest"

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val modReplacementProperties = mapOf(
        "modId" to modId,
        "modVersion" to modVersion
    )
    inputs.properties(modReplacementProperties)
    expand(modReplacementProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

base {
    archivesName.set("Almagest-$minecraftVersion")
    group = modId
    version = modVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://maven.fabricmc.net")
    maven(url = "https://dvs1.progwml6.com/files/maven/") // JEI
    maven(url = "https://modmaven.k-4u.nl") // Mirror for JEI
    maven(url = "https://maven.su5ed.dev/releases")
    maven(url = "https://api.modrinth.com/maven")
    maven(url = "https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
    flatDir {
        dirs("libs")
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateModMetadata)
        }
    }
}

dependencies {
    implementation("curse.maven:terrafirmacraft-302973:7476548")
    implementation("curse.maven:arborfirmacraft-877545:7314761")
    implementation("curse.maven:dynamictrees-252818:7340373")
    implementation("curse.maven:dynamictreestfc-362368:6681697")
    implementation("curse.maven:sodium-394468:6382651")
    implementation("curse.maven:forgified-fabric-api-889079:7377533")

    fun addEmbeddedFabricModule(dependency: String) {
        dependencies.implementation(dependency)
        dependencies.jarJar(dependency)
    }

    addEmbeddedFabricModule("curse.maven:picture-mode-1461755:7682759")
    addEmbeddedFabricModule("org.sinytra.forgified-fabric-api:fabric-api-base:0.4.42+d1308ded19")
    addEmbeddedFabricModule("org.sinytra.forgified-fabric-api:fabric-renderer-api-v1:3.4.0+9c40919e19")
    addEmbeddedFabricModule("org.sinytra.forgified-fabric-api:fabric-rendering-data-attachment-v1:0.3.48+73761d2e19")
    addEmbeddedFabricModule("org.sinytra.forgified-fabric-api:fabric-block-view-api-v2:1.0.10+9afaaf8c19")
}

neoForge {
    version = neoForgeVersion
    validateAccessTransformers = true

    parchment {
        minecraftVersion.set(parchmentMinecraftVersion)
        mappingsVersion.set(parchmentVersion)
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    ideSyncTask(generateModMetadata)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }

    processResources {}

    test {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }

    named("neoForgeIdeSync") {
        dependsOn(generateModMetadata)
    }
}
