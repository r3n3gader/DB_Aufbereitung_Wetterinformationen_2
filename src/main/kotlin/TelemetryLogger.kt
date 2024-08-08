import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TelemetryLogger(filename: String) : AutoCloseable {
    private val writer: FileWriter = FileWriter(filename, true)

    fun log(message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        writer.write("[$timestamp] $message\n")
    }

    override fun close() {
        try {
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
