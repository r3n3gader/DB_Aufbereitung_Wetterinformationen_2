plugins {
    kotlin("jvm") version "1.9.23"
}

group = "ch.heim-ag"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("test"))
    implementation("org.apache.commons:commons-csv:1.9.0")  // Aktuelle Version prüfen und ggf. aktualisieren
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}