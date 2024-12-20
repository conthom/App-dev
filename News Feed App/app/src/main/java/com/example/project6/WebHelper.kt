package com.example.project6

import android.content.Context
import android.util.Log
import org.chromium.base.ThreadUtils.runOnUiThread
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class WebHelper {
    private var cronetEngine: CronetEngine? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun initialize(context: Context) {
        cronetEngine = CronetEngine.Builder(context).build()
    }

    fun fetchArticleTitle(url: String, callback: (ByteArray?) -> Unit) {
        val requestBuilder = cronetEngine?.newUrlRequestBuilder(
            url,
            object : UrlRequest.Callback() {
                private val byteArrayOutputStream = ByteArrayOutputStream()

                override fun onRedirectReceived(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    newLocationUrl: String
                ) {
                    request.followRedirect()
                }

                override fun onResponseStarted(
                    request: UrlRequest,
                    info: UrlResponseInfo
                ) {
                    request.read(ByteBuffer.allocateDirect(1024))
                }

                override fun onReadCompleted(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    byteBuffer: ByteBuffer
                ) {
                    byteBuffer.flip()
                    val bytes = ByteArray(byteBuffer.remaining())
                    byteBuffer.get(bytes)
                    byteArrayOutputStream.write(bytes)
                    byteBuffer.clear()
                    request.read(byteBuffer)
                }

                override fun onSucceeded(
                    request: UrlRequest,
                    info: UrlResponseInfo
                ) {
                    try {
                        // Convert the response to a string
                        val jsonResponse = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name())

                        // Pass the entire JSON response back to the callback
                        runOnUiThread {
                            callback(jsonResponse.toByteArray(StandardCharsets.UTF_8)) // Pass as byte array
                        }
                    } catch (e: Exception) {
                        Log.e("WebHelper", "Failed to process response", e)
                        runOnUiThread {
                            callback(null) // Handle parsing failure
                        }
                    }
                }

                override fun onFailed(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    error: CronetException
                ) {
                    Log.e("WebHelper", "Request failed: ${error.message}")
                    runOnUiThread {
                        callback(null) // Handle request failure
                    }
                }
            },
            executor
        )
        requestBuilder?.build()?.start()
    }
}