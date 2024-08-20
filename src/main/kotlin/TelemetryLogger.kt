import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TelemetryLogger(private val logFilePath: String, private val charset: Charset = StandardCharsets.UTF_8) {

    private val writer: BufferedWriter = Files.newBufferedWriter(
        Path.of(logFilePath),
        charset,
        StandardOpenOption.CREATE,  // Erstellt die Datei, wenn sie nicht existiert
        StandardOpenOption.APPEND   // Fügt neue Einträge an das Ende der Datei an
    )
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun log(message: String) {
        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        writer.write("[$timestamp] $message")
        writer.newLine()
        writer.flush()
    }

    fun close() {
        writer.close()
    }
}
