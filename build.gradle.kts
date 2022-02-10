plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.lorenzo0111"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.md-5:bungeecord-config:1.16-R0.4")
    implementation("org.yaml:snakeyaml:1.30")
    implementation("org.slf4j:slf4j-api:1.7.35")
    implementation("ch.qos.logback:logback-classic:1.2.10")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "me.lorenzo0111.qav.converter.QAVConverter"
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    withType<Jar>().configureEach {
        archiveVersion.set("")
        archiveClassifier.set("")
    }
}