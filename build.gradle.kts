plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ch.heim-ag"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.commons:commons-csv:1.11.0")
    implementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("ch.heim.ag.AppKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "ch.heim.ag.AppKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.named<CreateStartScripts>("startScripts") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<CreateStartScripts>("startShadowScripts") {
    dependsOn(tasks.named("jar"))
}

tasks.named<Zip>("distZip") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<Tar>("distTar") {
    dependsOn(tasks.named("shadowJar"))
}
