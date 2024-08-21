import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Properties

fun main() {
    // Erstelle TelemetryLogger und ErrorHandler
    val telemetryLogger = TelemetryLogger("telemetry.log", StandardCharsets.UTF_8)
    telemetryLogger.log("Logger erfolgreich erstellt")

    val errorHandler = ErrorHandler(telemetryLogger)
    telemetryLogger.log("ErrorHandler erfolgreich erstellt")

    // Lade und validiere die Konfiguration
    val configLoader = ConfigLoader("config/config.properties")
    val properties: Properties
    try {
        properties = configLoader.loadConfig()
    } catch (e: Exception) {
        errorHandler.handleException(e)
        telemetryLogger.log("Fehler: ${e.message}")
        telemetryLogger.close()
        return
    }

    // Verarbeite die Konfiguration direkt hier
    val inputFolderPath = properties.getProperty("input.folder")
    val outputFolderPath = properties.getProperty("output.folder")
    val outputFileName = properties.getProperty("output.file")
    val outputFilePath = Paths.get(outputFolderPath, outputFileName).toString()

    // Überprüfe, ob alle benötigten Pfade vorhanden sind
    if (inputFolderPath.isNullOrBlank() || outputFolderPath.isNullOrBlank() || outputFileName.isNullOrBlank()) {
        val missingFields = listOfNotNull(
            if (inputFolderPath.isNullOrBlank()) "input.folder" else null,
            if (outputFolderPath.isNullOrBlank()) "output.folder" else null,
            if (outputFileName.isNullOrBlank()) "output.file" else null
        ).joinToString(", ")
        val errorMessage = "Fehlende oder leere Konfigurationswerte: $missingFields"
        telemetryLogger.log(errorMessage)
        errorHandler.handleException(IllegalArgumentException(errorMessage))
        telemetryLogger.close()
        return
    }

    telemetryLogger.log("Verzeichnisse gefunden")

    // Initialisiere den CSVProcessor mit den geladenen Pfaden
    val csvProcessor = CSVProcessor(inputFolderPath, outputFilePath, telemetryLogger, errorHandler)
    telemetryLogger.log("csvProcessor gestartet")

    // Starte die Verarbeitung
    telemetryLogger.log("Konfiguration erfolgreich. Verarbeitung läuft.")
    try {
        csvProcessor.process()
    } catch (e: Exception) {
        errorHandler.handleException(e)
    } finally {
        telemetryLogger.log("Abgeschlossen.")
        telemetryLogger.close()
    }
}
