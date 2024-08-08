package ch.heim.ag

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import java.util.TreeMap
import kotlin.test.assertTrue
import kotlin.test.fail

fun main() {
    val startTime = System.currentTimeMillis()
    val configFile = "config/config.properties"
    val properties = loadProperties(configFile)
    val inputFolder = properties.getProperty("input.folder")
    val outputFolderPath = properties.getProperty("output.folder")
    val outputFileName = "merged_output.csv"
    val outputFilePath = "$outputFolderPath$outputFileName"

    // Initialisiere Telemetrie-Logging
    val telemetryLog = FileWriter("telemetry.log", true)

    logTelemetry(telemetryLog, "Program start")

    // Test: Config-Datei existiert
    assertTrue(File(configFile).exists(), "Config-Datei $configFile existiert nicht")

    // Test: Input- und Outputfolder existieren
    assertTrue(File(inputFolder).exists(), "Input-Folder $inputFolder existiert nicht")
    assertTrue(File(outputFolderPath).exists(), "Output-Folder $outputFolderPath existiert nicht")

    // Test: Dateien im Input-Folder im richtigen Format vorhanden
    val csvFiles = File(inputFolder).listFiles { _, name -> name.endsWith(".csv") }
    assertTrue(csvFiles?.isNotEmpty() ?: false, "Keine CSV-Dateien im Input-Folder $inputFolder gefunden")

    val dataMap = TreeMap<String, MutableMap<String, String>>() // TreeMap für sortierte Datumsangaben
    val stations = mutableSetOf<String>()
    var processedFiles = 0

    val csvFormat = CSVFormat.RFC4180
        .withHeader()
        .withDelimiter(';')  // Setzt das Trennzeichen auf Semikolon
        .withSkipHeaderRecord(true)

    try {
        val folder = File(inputFolder)
        val csvFiles = folder.listFiles { _, name -> name.endsWith(".csv") }

        if (csvFiles != null) {
            for (file in csvFiles) {
                val reader: Reader = FileReader(file)
                val records: Iterable<CSVRecord> = csvFormat.parse(reader)

                for (record in records) {
                    val id = record["station/location"]
                    val date = record["date"]
                    val temperature = record["tre200d0"]
                    stations.add(id)

                    if (!dataMap.containsKey(date)) {
                        dataMap[date] = mutableMapOf()
                    }
                    dataMap[date]!![id] = temperature
                }

                processedFiles++
                logTelemetry(telemetryLog, "Processed file: ${file.name}")
            }
        }

        FileWriter(outputFilePath).use { writer ->
            val outputCsvFormat = CSVFormat.RFC4180
                .withHeader("date", *stations.toTypedArray())
                .withDelimiter(';')

            val csvPrinter = outputCsvFormat.print(writer)
            for ((datum, stationData) in dataMap) {
                val row = mutableListOf(datum)
                for (station in stations) {
                    row.add(stationData[station] ?: "")
                }
                csvPrinter.printRecord(row)
            }
            csvPrinter.flush()
        }

        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - startTime) / 1000.0

        logTelemetry(telemetryLog, "Processing completed in $durationSeconds seconds")

        // Test: Überwachung der Durchlaufzeit (maximal 20 Sekunden)
        assertTrue(durationSeconds <= 20.0, "Die Verarbeitung dauerte länger als erwartet: $durationSeconds Sekunden")

        println("Erfolgreich $processedFiles Dateien verarbeitet in $durationSeconds Sekunden. Ausgabe nach: $outputFilePath")
    } catch (e: IOException) {
        e.printStackTrace()
        logTelemetry(telemetryLog, "IOException: ${e.message}")
        fail("Fehler beim Lesen/Schreiben von Dateien: ${e.message}")
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        logTelemetry(telemetryLog, "IllegalArgumentException: ${e.message}")
        fail("Ungültige Argumente beim Parsen der CSV-Datei: ${e.message}")
    } finally {
        logTelemetry(telemetryLog, "Program end")
        telemetryLog.close()
    }
}

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    properties.load(FileReader(filename))
    return properties
}

// Funktion zur Erfassung von Telemetriedaten
fun logTelemetry(writer: FileWriter, message: String) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    writer.write("[$timestamp] $message\n")
}
