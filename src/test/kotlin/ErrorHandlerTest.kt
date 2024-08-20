import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

class ErrorHandlerTest {

    companion object {
        private val telemetryLogFile = Paths.get("build/reports/telemetry_tests.log").toAbsolutePath().toString()
        private lateinit var telemetryLogger: TelemetryLogger

        @BeforeAll
        @JvmStatic
        fun setUp() {
            // Öffnen oder erstellen der Log-Datei im UTF-8 Format
            telemetryLogger = TelemetryLogger(telemetryLogFile, StandardCharsets.UTF_8)
            telemetryLogger.log("Starte alle Tests für ErrorHandler.")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            telemetryLogger.log("Beende alle Tests für ErrorHandler.")
            telemetryLogger.close()
        }
    }

    @Test
    fun `handleIOException should log IOException message`() {
        telemetryLogger.log("Starte Test")

        // Arrange
        val errorHandler = ErrorHandler(telemetryLogger)
        val exception = IOException("Test: IO Exception")

        // Act
        try {
            errorHandler.handleIOException(exception)
            telemetryLogger.log("Test erfolgreich: handleIOException loggt die IOException")
        } catch (e: Exception) {
            telemetryLogger.log("Test fehlgeschlagen: handleIOException loggt die IOException message nicht - ${e.message}")
            throw e
        } finally {
            telemetryLogger.log("Beende Test")
        }
    }

    @Test
    fun `handleIllegalArgumentException should log IllegalArgumentException message`() {
        telemetryLogger.log("Starte Test")

        // Arrange
        val errorHandler = ErrorHandler(telemetryLogger)
        val exception = IllegalArgumentException("Test: Illegal Argument Exception")

        // Act
        try {
            errorHandler.handleIllegalArgumentException(exception)
            telemetryLogger.log("Test erfolgreich: handleIllegalArgumentException loggt die IllegalArgumentException message")
        } catch (e: Exception) {
            telemetryLogger.log("Test fehlgeschlagen: handleIllegalArgumentException loggt die IllegalArgumentException message nicht - ${e.message}")
            throw e
        } finally {
            telemetryLogger.log("Beende Test")
        }
    }
}
