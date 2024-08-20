import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.fail
import org.mockito.Mockito.*
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.Comparator

class CSVProcessorTest {

    companion object {
        private val invalidDir = Paths.get("src/test/resources/invalid")
        private val validDir = Paths.get("src/test/resources/valid")
        private val missingDir = Paths.get("src/test/resources/missing")
        private val outputDir = Paths.get("build/reports/tests/output")
        private val telemetryLogFile = Paths.get("build/reports/telemetry_tests.log").toAbsolutePath().toString()

        private lateinit var telemetryLogger: TelemetryLogger
        private lateinit var errorHandler: ErrorHandler

        @BeforeAll
        @JvmStatic
        fun setUp() {
            telemetryLogger = TelemetryLogger(telemetryLogFile, StandardCharsets.UTF_8)
            errorHandler = mock(ErrorHandler::class.java)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            telemetryLogger.log("Beende alle Tests und schließe das Logging.")
            telemetryLogger.close()
            // Cleanup: Entferne die erstellten Dateien und Ordner nach allen Tests
            Files.walk(outputDir)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }

    @Test
    fun defekteDatenVerarbeiten() {
        telemetryLogger.log("Starte Test: defekteDatenVerarbeiten")

        val inputFolder = invalidDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        try {
            csvProcessor.process()
            fail("IOException wurde nicht geworfen!")
        } catch (e: IOException) {
            // Assert
            verify(errorHandler).handleException(e)
            telemetryLogger.log("Test defekteDatenVerarbeiten: IOException wurde korrekt gefangen.")
        }

        telemetryLogger.log("Beende Test: defekteDatenVerarbeiten")
    }

    @Test
    fun fehlendeDatenVerarbeiten() {
        telemetryLogger.log("Starte Test: fehlendeDatenVerarbeiten")

        val inputFolder = missingDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        try {
            csvProcessor.process()
            fail("FileNotFoundException erwartet, wurde aber nicht geworfen!")
        } catch (e: FileNotFoundException) {
            // Assert
            verify(errorHandler).handleException(e)
            telemetryLogger.log("Test fehlendeDatenVerarbeiten: FileNotFoundException wurde korrekt gefangen.")
        }

        telemetryLogger.log("Beende Test: fehlendeDatenVerarbeiten")
    }

    @Test
    fun gueltigeDatenVerarbeiten() {
        telemetryLogger.log("Starte Test: gueltigeDatenVerarbeiten")

        val inputFolder = validDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        csvProcessor.process()

        val outputFile = outputDir.resolve("wetterdaten.csv")
        assert(Files.exists(outputFile)) { "Ausgabedatei wurde nicht erstellt" }

        Files.newBufferedReader(outputFile).use { reader ->
            val firstLine = reader.readLine()
            assert(firstLine?.startsWith("datum") == true) { "Die Header-Zeile im Output stimmt nicht" }
        }

        telemetryLogger.log("Test gueltigeDatenVerarbeiten: Datei erfolgreich verarbeitet und gespeichert.")

        telemetryLogger.log("Beende Test: gueltigeDatenVerarbeiten")
    }
}
