import java.io.BufferedWriter
import java.io.Closeable
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TelemetryLogger(filename: String, charset: Charset) : Closeable {
    private val writer: BufferedWriter = Files.newBufferedWriter(Paths.get(filename), charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun log(message: String) {
        val timestampedMessage = "${LocalDateTime.now().format(formatter)} - $message"
        writer.write(timestampedMessage)
        writer.newLine()
    }

    override fun close() {
        writer.close()
    }

}
