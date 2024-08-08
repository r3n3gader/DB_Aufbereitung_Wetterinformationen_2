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
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.mockito:mockito-core:5.0.0")
    testImplementation("org.mockito:mockito-inline:5.0.0")

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
