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
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:5.0.0")
    testImplementation("org.mockito:mockito-inline:5.0.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
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

// Definiere eine neue Aufgabe "testSummary", die den "test" Task ausführt
tasks.register("testSummary") {
    description = "Run all test tasks and display a summary"
    group = "verification"
    dependsOn("test")
    finalizedBy("summary")
}

// Aufgabe zur Zusammenfassung der Testergebnisse
tasks.register("summary") {
    doLast {
        println("*******************************")
        println("*   Zusammenfassung der Tests  *")
        println("*******************************")

        // Detaillierte Testergebnisse anzeigen, basierend auf den generierten XML-Berichten
        val resultDirs = listOf(
            "build/test-results/test"
        )

        resultDirs.forEach { dir ->
            val resultDirFile = file(dir)
            if (resultDirFile.exists()) {
                resultDirFile.walkTopDown()
                    .filter { it.extension == "xml" }
                    .forEach { file ->
                        println("Test Resultat Datei: ${file.name}")
                    }
            } else {
                println("Keine Test-Ergebnisse gefunden im Verzeichnis: $dir")
            }
        }

        // Pfade zu den HTML-Berichten anzeigen und anklickbare Links erstellen
        val reportDirs = listOf(
            "build/reports/tests/testCSVProcessor",
            "build/reports/tests/testConfigLoader"
        )

        reportDirs.forEach { dir ->
            val indexHtmlPath = project.file("$dir/index.html").toURI().toString().replace("file:/", "file:///")
            println("Ergebnisse unter: $indexHtmlPath")
        }

        println("*******************************")
    }
}
