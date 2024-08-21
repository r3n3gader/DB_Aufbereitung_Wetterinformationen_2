import java.io.FileReader
import java.io.IOException
import java.util.*

class ConfigLoader(private val configFile: String) {

    fun loadConfig(): Properties {
        val properties = Properties()
        try {
            FileReader(configFile).use { reader ->
                properties.load(reader)
            }
            validateProperties(properties)
        } catch (e: IOException) {
            throw RuntimeException("Fehler beim Laden der Konfigurationsdatei: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Fehler in der Konfigurationsdatei: ${e.message}", e)
        }
        return properties
    }

    private fun validateProperties(properties: Properties) {
        val requiredKeys = listOf("input.folder", "output.folder", "output.file")
        for (key in requiredKeys) {
            if (properties.getProperty(key).isNullOrBlank()) {
                throw IllegalArgumentException("Fehlender oder leerer Wert für Konfigurationseinstellung: $key")
            }
        }
    }
}
