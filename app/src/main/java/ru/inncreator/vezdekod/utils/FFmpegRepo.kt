package ru.inncreator.vezdekod.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.arthenica.ffmpegkit.FFmpegKit
import org.koin.core.KoinComponent
import timber.log.Timber
import java.io.File


class FFmpegRepo : KoinComponent {


    private val commands = arrayListOf<String>()

    private lateinit var currentVideo: Uri

    private var startTrim: Float? = null
    private var endTrim: Float? = null

    fun setTrim(startTrim: Float, endTrim: Float) {
        this.startTrim = startTrim
        this.endTrim = endTrim
    }

    var isLoop = false
    var isBoomerang = false

    fun initialize() {

    }


    private fun completeAllCommand() {
        FFmpegKit.execute("-i file1.mp4 -c:v mpeg4 file2.mp4")

        FFmpegKit.executeAsync("-i file1.mp4 -c:v mpeg4 file2.mp4",
            { session ->
                val state = session.state
                val returnCode = session.returnCode

                // CALLED WHEN SESSION IS EXECUTED
                Timber.d(
                    "FFmpeg process exited with state %s and rc %s.%s",
                    state,
                    returnCode,
                    session.failStackTrace
                )
            }, {
                // CALLED WHEN SESSION PRINTS LOGS
            }) {
            // CALLED WHEN SESSION GENERATES STATISTICS
        }
    }

    fun selectVideo(context: Context, uri: Uri) {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(uri, proj, null, null, null)
            if (cursor == null)
                return
            val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()

            currentVideo = cursor.getString(columnIndex).toUri()
        } finally {
            cursor?.close()
        }

    }


    fun saveVideo() {
        val uri = getPathSaveUri()
//        val outputVideoPath = FFmpegKitConfig.getSafParameterForWrite(context, file.toUri())
        FFmpegKit.execute(" -i $currentVideo -c:v copy $uri")
    }

    fun getTrimCommand(): String {
        val localStartTrim = startTrim ?: return ""  // Seconds in start trim video
        val localEndTrim = endTrim ?: return ""

        Timber.d("Start trim = $localStartTrim; End trim = $localEndTrim")

        val startHH: Int = if (localStartTrim > 3600f) (localStartTrim / 3600).toInt() else 0
        val endHH: Int = if (localEndTrim > 3600f) (localEndTrim / 3600).toInt() else 0

        val startMM: Int = if (localStartTrim > 60f) (localStartTrim % 3600).toInt() / 60 else 0
        val endMM: Int = if (localEndTrim > 60f) (localEndTrim % 3600).toInt() / 60 else 0

        val startSS: Int = if (localStartTrim > 60f) (localStartTrim % 60).toInt() else localStartTrim.toInt()
        val endSS: Int = if (localEndTrim > 60f) (localEndTrim % 60).toInt() else localEndTrim.toInt()

//        val startFinalTime = String.format("%02d:%02:%02d", startHH, startMM, startSS)
//        val endFinalTime = String.format("%02d:%02:%02d",   endHH, endMM, endSS)


        val trimCommand = String.format("-ss %02d:%02d:%02d -to %02d:%02d:%02d",
            startHH,
            startMM,
            startSS,
            endHH,
            endMM,
            endSS
        )

        Timber.i("Trim command = $trimCommand")
        return  trimCommand
    }

    fun saveAndUpdateVideo() {
        val uri = getPathSaveUri()
        val trimCommand = getTrimCommand()
        val loopCommand = getLoopCommnad()

//        val outputVideoPath = FFmpegKitConfig.getSafParameterForWrite(context, file.toUri())
        FFmpegKit.execute(" $loopCommand -i $currentVideo $trimCommand -c:v mpeg4 $uri")
        currentVideo = uri
    }

    private fun getLoopCommnad(): String {
       return if (isLoop) "-stream_loop 4" else ""
    }

    private fun getPathSaveUri(): Uri {

        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .toString(), "vezdekod"
        )
        if (!folder.exists())
            folder.mkdirs()
        val file = File(folder, System.nanoTime().toString() + ".mp4")
        Timber.i("Uri = ${file.toUri()}")
        return file.toUri()
    }


    fun getCurrentVideo() = currentVideo
}