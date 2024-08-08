import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TelemetryLoggerTest {

    @Test
    fun `log should write message to file`() {
        // Arrange
        val filename = "test_telemetry.log"
        val telemetryLogger = TelemetryLogger(filename)

        // Act
        telemetryLogger.log("Test message")

        // Assert
        val file = java.io.File(filename)
        Assertions.assertTrue(file.exists())
        val content = file.readText()
        Assertions.assertTrue(content.contains("Test message"))

        // Cleanup
        file.delete()
    }

    @Test
    fun `close should close the file writer`() {
        // Arrange
        val filename = "test_telemetry_close.log"
        val telemetryLogger = TelemetryLogger(filename)

        // Act
        telemetryLogger.close()

        // Assert
        // Test if close() does not throw an exception
        assertDoesNotThrow { telemetryLogger.close() }

        // Cleanup
        val file = java.io.File(filename)
        file.delete()
    }
}
