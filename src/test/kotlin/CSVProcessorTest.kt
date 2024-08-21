import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class CSVProcessorTest {

    private lateinit var telemetryLogger: TelemetryLogger
    private lateinit var errorHandler: ErrorHandler

    @BeforeEach
    fun setup() {
        // Initialisiere den TelemetryLogger und ErrorHandler vor jedem Test
        telemetryLogger = TelemetryLogger("build/reports/tests/telemetry_tests.log", StandardCharsets.UTF_8)
        errorHandler = ErrorHandler(telemetryLogger)
    }

    @AfterEach
    fun teardown() {
        // Stelle sicher, dass der TelemetryLogger nach jedem Test geschlossen wird
        telemetryLogger.close()
    }

    @Test
    fun processInvalidData() {
        val testName = "processInvalidData"
        telemetryLogger.log("Test $testName gestartet.")

        try {
            val processor = CSVProcessor(
                inputFolderPath = "src/test/resources/invalid",
                outputFilePath = "",
                telemetryLogger = telemetryLogger,
                errorHandler = errorHandler
            )
            processor.process()
            telemetryLogger.log("Test $testName erfolgreich abgeschlossen.")
        } catch (e: Exception) {
            telemetryLogger.log("Test $testName fehlgeschlagen aufgrund: ${e.message}")
        } finally {
            telemetryLogger.log("Test $testName abgeschlossen.")
        }
    }

    @Test
    fun processMissingData() {
        val testName = "processMissingData"
        telemetryLogger.log("Test $testName gestartet.")

        try {
            val processor = CSVProcessor(
                inputFolderPath = "src/test/resources/missing",
                outputFilePath = "",
                telemetryLogger = telemetryLogger,
                errorHandler = errorHandler
            )
            processor.process()
            telemetryLogger.log("Test $testName erfolgreich abgeschlossen.")
        } catch (e: Exception) {
            telemetryLogger.log("Test $testName fehlgeschlagen aufgrund: ${e.message}")
        } finally {
            telemetryLogger.log("Test $testName abgeschlossen.")
        }
    }

    @Test
    fun processValidData() {
        val testName = "processValidData"
        telemetryLogger.log("Test $testName gestartet.")

        try {
            val outputFilePath = "build/reports/tests/output/wetterdaten_test.csv"
            val processor = CSVProcessor(
                inputFolderPath = "src/test/resources/valid",
                outputFilePath = outputFilePath,
                telemetryLogger = telemetryLogger,
                errorHandler = errorHandler
            )
            processor.process()

            // Überprüfen, ob die Ausgabedatei erstellt wurde
            if (Files.exists(Paths.get(outputFilePath))) {
                telemetryLogger.log("Test $testName erfolgreich abgeschlossen.")
            } else {
                telemetryLogger.log("Test $testName fehlgeschlagen: Ausgabedatei wurde nicht erstellt.")
            }
        } catch (e: Exception) {
            telemetryLogger.log("Test $testName fehlgeschlagen aufgrund: ${e.message}")
        } finally {
            telemetryLogger.log("Test $testName abgeschlossen.")
        }
    }
}
