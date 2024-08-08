import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
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
    val csvFiles = Files.list(Paths.get(inputFolder))
        .filter { Files.isRegularFile(it) && it.toString().endsWith(".csv") }
        .collect(Collectors.toList()) // Umwandeln in eine Liste

    assertTrue(csvFiles.isNotEmpty(), "Keine CSV-Dateien im Input-Folder $inputFolder gefunden")

    val dataMap = TreeMap<String, MutableMap<String, String>>() // TreeMap für sortierte Datumsangaben
    val stations = mutableSetOf<String>()
    var processedFiles = 0

    val csvFormat = CSVFormat.Builder.create()
        .setHeader()   // Header wird automatisch gesetzt, wenn die CSV-Datei Header enthält
        .setDelimiter(';')
        .setSkipHeaderRecord(true)
        .build()

    try {
        for (csvFilePath in csvFiles) {
            val file = csvFilePath.toFile() // Konvertiert Path zu File
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

        FileWriter(outputFilePath).use { writer ->
            val outputCsvFormat = CSVFormat.Builder.create()
                .setHeader("date", *stations.toTypedArray())
                .setDelimiter(';')
                .build()

            // Erstellen des CSVPrinter-Objekts
            val csvPrinter = CSVPrinter(writer, outputCsvFormat)
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
