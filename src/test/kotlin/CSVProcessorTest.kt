import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.*
import java.util.Comparator

class CSVProcessorTest {

    private val invalidDir = Paths.get("src/test/resources/invalid")
    private val validDir = Paths.get("src/test/resources/valid")
    private val missingDir = Paths.get("src/test/resources/missing")
    private val outputDir = Paths.get("src/test/resources/output")

    @Test
    fun defekteDatenVerarbeiten() {
        // Arrange
        val inputFolder = invalidDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val telemetryLogger = mock(TelemetryLogger::class.java)
        val errorHandler = mock(ErrorHandler::class.java)

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        // Act
        try {
            csvProcessor.process() // Wenn jetzt keine Exception geworfen wird, dann muss der Test fehlschlagen:
            fail("IOException wurde nicht geworfen!")
        } catch (e: IOException) {
            // Assert
            verify(errorHandler).handleException(e)
        }

        // Optional: Überprüfe, ob die Telemetrie-Logs korrekt aufgerufen wurden
        verify(telemetryLogger).log("Program start")
        verify(telemetryLogger).log(contains("Processing completed"))

        // Cleanup
        Files.walk(outputDir)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun fehlendeDatenVerarbeiten() {
        // Arrange
        val inputFolder = missingDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val telemetryLogger = mock(TelemetryLogger::class.java)
        val errorHandler = mock(ErrorHandler::class.java)

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        // Act
        try {
            csvProcessor.process()
            fail("Exception aufgrund nicht gefundener Rohdaten erwartet")
        } catch (e: FileNotFoundException) {
            // Assert
            verify(errorHandler).handleException(e)
        }
    }

    @Test
    fun gueltigeDatenVerarbeiten() {
        // Arrange
        val inputFolder = validDir.toAbsolutePath().toString()
        val outputFolder = outputDir.toAbsolutePath().toString()

        val telemetryLogger = mock(TelemetryLogger::class.java)
        val errorHandler = mock(ErrorHandler::class.java)

        // Bereinige vorhandene Dateien
        Files.walk(outputDir)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }

        val csvProcessor = CSVProcessor(inputFolder, outputFolder, telemetryLogger, errorHandler)

        // Act
        csvProcessor.process()

        // Assert
        val outputFile = outputDir.resolve("merged_output_test.csv")
        assert(Files.exists(outputFile)) { "Output file wurde nicht erstellt" }

        // Optional: Überprüfe den Inhalt der Ausgabedatei
        Files.newBufferedReader(outputFile).use { reader ->
            val firstLine = reader.readLine()
            assert(firstLine?.startsWith("date") == true) { "Die Header-Zeile im Output stimmt nicht" }
        }

        verify(telemetryLogger).log("Program start")
        verify(telemetryLogger).log(contains("Processing completed"))

        // Es sollten keine Interaktionen mit dem ErrorHandler vorhanden sein
        verifyNoInteractions(errorHandler)
    }
}
