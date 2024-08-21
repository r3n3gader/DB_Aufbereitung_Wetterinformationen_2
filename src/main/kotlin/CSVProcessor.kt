import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

class CSVProcessor(
    private val inputFolderPath: String,
    private val outputFilePath: String,
    private val telemetryLogger: TelemetryLogger,
    private val errorHandler: ErrorHandler
) {
    fun process() {
        val startTime = System.currentTimeMillis()
        val dataMap = TreeMap<String, MutableMap<String, String>>()
        val stations = mutableSetOf<String>()
        var processedFiles = 0

        try {
            // Überprüfe, ob der Input-Ordner existiert
            val inputFolder = File(inputFolderPath)
            if (!inputFolder.exists()) {
                throw FileNotFoundException("Input-Folder $inputFolderPath existiert nicht.")
            }

            // Suche nach CSV-Dateien im Input-Ordner
            val csvFiles = Files.list(Paths.get(inputFolderPath))
                .filter { Files.isRegularFile(it) && it.toString().endsWith(".csv") }
                .collect(Collectors.toList())

            if (csvFiles.isEmpty()) {
                throw FileNotFoundException("Keine CSV-Dateien im Input-Ordner $inputFolderPath gefunden.")
            }

            val csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setDelimiter(';')
                .setSkipHeaderRecord(true)
                .build()

            for (csvFilePath in csvFiles) {
                val file = csvFilePath.toFile()
                val reader: Reader = FileReader(file)
                val records: Iterable<CSVRecord> = csvFormat.parse(reader)

                for (record in records) {
                    val id = record["station/location"]?.lowercase() ?: throw IllegalArgumentException("Header fehlt: 'station/location'")
                    val date = record["date"]?.lowercase() ?: throw IllegalArgumentException("Header fehlt: 'date'")
                    val temperature = record["tre200d0"]?.lowercase() ?: throw IllegalArgumentException("Header fehlt: 'tre200d0'")

                    // Überprüfe das Dezimaltrennzeichen
                    if (temperature.contains(",")) {
                        throw IOException("Temperaturwert sollte '.' als Dezimaltrennzeichen verwenden, jedoch: $temperature")
                    }

                    stations.add(id)

                    if (!dataMap.containsKey(date)) {
                        dataMap[date] = mutableMapOf()
                    }
                    dataMap[date]!![id] = temperature
                }

                processedFiles++
                telemetryLogger.log("Verarbeitet: ${file.name}")
            }

            // Sicherstellen, dass der Output-Ordner existiert
            val outputFolder = File(outputFilePath).parentFile
            if (!outputFolder.exists()) {
                outputFolder.mkdirs()
            }

            FileWriter(outputFilePath).use { writer ->
                val outputCsvFormat = CSVFormat.Builder.create()
                    .setHeader("datum", *stations.toTypedArray())
                    .setDelimiter(';')
                    .build()

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

            // Überprüfen, ob die Ausgabedatei erstellt wurde
            if (!Files.exists(Paths.get(outputFilePath))) {
                throw IOException("Ausgabedatei wurde nicht erstellt.")
            }

            val endTime = System.currentTimeMillis()
            val durationSeconds = (endTime - startTime) / 1000.0

            telemetryLogger.log("Verarbeitung beendet nach $durationSeconds Sekunden.")
            println("Erfolgreich $processedFiles Dateien verarbeitet in $durationSeconds Sekunden. Ausgabe nach: $outputFilePath")
        } catch (e: Exception) {
            errorHandler.handleException(e)
            throw e
        }
    }
}
