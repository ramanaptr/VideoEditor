package com.video.trimmer.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.video.trimmer.interfaces.OnCompressVideoListener
import com.video.trimmer.interfaces.OnCropVideoListener
import com.video.trimmer.interfaces.OnTrimVideoListener


class VideoOptionsV2(private var ctx: Context) {
    companion object {
        const val TAG = "VideoOptionsV2"
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun trimVideo(
            startPosition: String,
            endPosition: String,
            inputPath: String,
            outputPath: String,
            outputFileUri: Uri,
            listener: OnTrimVideoListener?
    ) {
        val command = arrayOf(
                "-y",
                "-i",
                inputPath,
                "-ss",
                startPosition,
                "-to",
                endPosition,
                "-c",
                "copy",
                outputPath
        )
        listener?.onTrimStarted()
        FFmpeg.executeAsync(command) { executionId, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    listener?.getResult(outputFileUri)
                    Log.i(Config.TAG, "Command execution completed successfully.")
                }
                RETURN_CODE_CANCEL -> {
                    listener?.cancelAction()
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                }
                else -> {
                    listener?.onError("Command execution cancelled by user.")
                    Log.i(
                            Config.TAG,
                            String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    returnCode
                            )
                    )
                    Config.printLastCommandOutput(Log.INFO)
                }
            }
        }
        Config.enableLogCallback {
            println(">>>>>>>>>>> ${it.text}")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun cropVideo(
            width: Int,
            height: Int,
            x: Int,
            y: Int,
            inputPath: String,
            outputPath: String,
            outputFileUri: Uri,
            listener: OnCropVideoListener?,
            frameCount: Int
    ) {
        val command = arrayOf(
                "-i",
                inputPath,
                "-filter:v",
                "crop=$width:$height:$x:$y",
                "-threads",
                "5",
                "-preset",
                "ultrafast",
                "-strict",
                "-2",
                "-c:a",
                "copy",
                outputPath
        )
        listener?.onCropStarted()
        FFmpeg.executeAsync(command) { executionId, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    listener?.getResult(outputFileUri)
                    Log.i(Config.TAG, "Command execution completed successfully.")
                }
                RETURN_CODE_CANCEL -> {
                    listener?.cancelAction()
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                }
                else -> {
                    listener?.onError("Command execution cancelled by user.")
                    Log.i(
                            Config.TAG,
                            String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    returnCode
                            )
                    )
                    Config.printLastCommandOutput(Log.INFO)
                }
            }
        }
        Config.enableLogCallback { message ->
            if (message != null && message.text != null) {
                val messageArray = message.text.split("frame=")
                if (messageArray.size >= 2) {
                    val secondArray = messageArray[1].trim().split(" ")
                    if (secondArray.isNotEmpty()) {
                        val framesString = secondArray[0].trim()
                        try {
                            val frames = framesString.toInt()
                            val progress = (frames.toFloat() / frameCount.toFloat()) * 100f
                            listener?.onProgress(progress)
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun compressVideo(
            inputPath: String,
            outputPath: String,
            outputFileUri: Uri,
            width: String,
            height: String,
            listener: OnCompressVideoListener?
    ) {
        // TODO: compress command video
    }
}