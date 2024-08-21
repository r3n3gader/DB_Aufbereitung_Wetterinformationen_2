import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.*

class ConfigLoaderTest {

    companion object {
        private val telemetryLogFile = Paths.get("build/reports/telemetry_tests.log").toAbsolutePath().toString()
        private lateinit var telemetryLogger: TelemetryLogger

        @BeforeAll
        @JvmStatic
        fun setUp() {
            telemetryLogger = TelemetryLogger(telemetryLogFile, StandardCharsets.UTF_8)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            telemetryLogger.log("Beende alle Tests für ConfigLoader.")
            telemetryLogger.close()
        }
    }

    @Test
    fun `test loading invalid config properties`() {
        telemetryLogger.log("Starte Test: lade ungültige config.properties")

        // Arrange
        val invalidConfigPath = Paths.get("src/test/resources/invalid/config.properties").toAbsolutePath().toString()
        val configLoader = ConfigLoader(invalidConfigPath)

        // Act & Assert
        try {
            assertThrows<RuntimeException> {
                configLoader.loadConfig()
            }
            telemetryLogger.log("Test erfolgreich")
        } catch (e: Exception) {
            telemetryLogger.log("Test fehlgeschlagen: - ${e.message}")
            throw e
        } finally {
            telemetryLogger.log("Beende Test")
        }
    }

    @Test
    fun `test missing required keys in config properties`() {
        telemetryLogger.log("Starte Test: lade unvollständige config.properties")

        // Arrange
        val missingKeysConfigPath = Paths.get("src/test/resources/invalid/missing_keys_config.properties").toAbsolutePath().toString()
        val configLoader = ConfigLoader(missingKeysConfigPath)

        // Act & Assert
        try {
            assertThrows<RuntimeException> {
                configLoader.loadConfig()
            }
            telemetryLogger.log("Test erfolgreich")
        } catch (e: Exception) {
            telemetryLogger.log("Test fehlgeschlagen: - ${e.message}")
            throw e
        } finally {
            telemetryLogger.log("Beende Test")
        }
    }

    @Test
    fun `test loading valid config properties`() {
        telemetryLogger.log("Starte Test: lade gültige config.properties")

        // Arrange
        val validConfigPath = Paths.get("src/test/resources/valid/config.properties").toAbsolutePath().toString()
        val configLoader = ConfigLoader(validConfigPath)

        // Act & Assert
        try {
            val properties: Properties = configLoader.loadConfig()
            assertNotNull(properties.getProperty("input.folder"))
            assertNotNull(properties.getProperty("output.folder"))
            assertNotNull(properties.getProperty("output.file"))
            telemetryLogger.log("Test erfolgreich")
        } catch (e: Exception) {
            telemetryLogger.log("Test fehlgeschlagen: - ${e.message}")
            throw e
        } finally {
            telemetryLogger.log("Beende Test")
        }
    }
}
