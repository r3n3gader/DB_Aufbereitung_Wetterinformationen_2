import java.io.FileNotFoundException
import java.io.IOException

class ErrorHandler(private val telemetryLogger: TelemetryLogger) {

    fun handleException(e: Exception) {
        when (e) {
            is FileNotFoundException -> handleFileNotFoundException(e)
            is IOException -> handleIOException(e)
            is IllegalArgumentException -> handleIllegalArgumentException(e)
            else -> handleGenericException(e)
        }
    }

    private fun handleFileNotFoundException(e: FileNotFoundException) {
        e.printStackTrace()
        telemetryLogger.log("FileNotFoundException: ${e.message}")
    }

    fun handleIOException(e: IOException) {
        e.printStackTrace()
        telemetryLogger.log("IOException: ${e.message}")
    }

    fun handleIllegalArgumentException(e: IllegalArgumentException) {
        e.printStackTrace()
        telemetryLogger.log("IllegalArgumentException: ${e.message}")
    }

    private fun handleGenericException(e: Exception) {
        e.printStackTrace()
        telemetryLogger.log("Exception: ${e.message}")
    }
}
