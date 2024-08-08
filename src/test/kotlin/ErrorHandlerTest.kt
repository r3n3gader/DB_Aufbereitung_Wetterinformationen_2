import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.IOException

class ErrorHandlerTest {

    @Test
    fun `handleIOException should log IOException message`() {
        // Arrange
        val telemetryLogger = mock(TelemetryLogger::class.java)
        val errorHandler = ErrorHandler(telemetryLogger)
        val exception = IOException("Test IO Exception")

        // Act
        errorHandler.handleIOException(exception)

        // Assert
        verify(telemetryLogger).log("IOException: Test IO Exception")
    }

    @Test
    fun `handleIllegalArgumentException should log IllegalArgumentException message`() {
        // Arrange
        val telemetryLogger = mock(TelemetryLogger::class.java)
        val errorHandler = ErrorHandler(telemetryLogger)
        val exception = IllegalArgumentException("Test Illegal Argument Exception")

        // Act
        errorHandler.handleIllegalArgumentException(exception)

        // Assert
        verify(telemetryLogger).log("IllegalArgumentException: Test Illegal Argument Exception")
    }
}
