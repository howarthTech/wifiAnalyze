package com.wifianalyze.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class SpeedTestState {
    object Idle : SpeedTestState()
    data class Running(val phase: Phase, val currentMbps: Float) : SpeedTestState()
    data class Result(val downloadMbps: Float, val uploadMbps: Float) : SpeedTestState()
    data class Error(val message: String) : SpeedTestState()

    enum class Phase { Download, Upload }
}

@Singleton
class SpeedTestRunner @Inject constructor() {

    private val downloadUrl = "https://speed.cloudflare.com/__down?bytes=25000000"
    private val uploadUrl   = "https://speed.cloudflare.com/__up"
    private val uploadBytes = 10_000_000 // 10 MB

    /**
     * Runs a download then upload speed test.
     * [onProgress] is called with the current phase + live Mbps estimate.
     */
    suspend fun runTest(onProgress: (SpeedTestState.Running) -> Unit): SpeedTestState =
        withContext(Dispatchers.IO) {
            try {
                // ── Download ──────────────────────────────────────────────────────
                val downloadMbps = measureDownload { mbps ->
                    onProgress(SpeedTestState.Running(SpeedTestState.Phase.Download, mbps))
                }

                // ── Upload ────────────────────────────────────────────────────────
                val uploadMbps = measureUpload { mbps ->
                    onProgress(SpeedTestState.Running(SpeedTestState.Phase.Upload, mbps))
                }

                SpeedTestState.Result(downloadMbps, uploadMbps)
            } catch (e: Exception) {
                SpeedTestState.Error(e.message ?: "Speed test failed")
            }
        }

    // ── internals ─────────────────────────────────────────────────────────────

    private fun measureDownload(onProgress: (Float) -> Unit): Float {
        val conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout    = 30_000
        }
        conn.connect()

        val buf = ByteArray(65_536)
        var totalBytes = 0L
        val start = System.nanoTime()
        var lastReport = start

        conn.inputStream.use { stream ->
            var read: Int
            while (stream.read(buf).also { read = it } != -1) {
                totalBytes += read
                val now = System.nanoTime()
                if (now - lastReport >= 200_000_000L) { // every 200 ms
                    val elapsed = (now - start) / 1e9
                    onProgress(bytesToMbps(totalBytes, elapsed))
                    lastReport = now
                }
            }
        }

        val elapsed = (System.nanoTime() - start) / 1e9
        return bytesToMbps(totalBytes, elapsed)
    }

    private fun measureUpload(onProgress: (Float) -> Unit): Float {
        val payload = ByteArray(uploadBytes) { 0x41 } // 'A' × 10 MB

        val conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            requestMethod     = "POST"
            doOutput          = true
            connectTimeout    = 10_000
            readTimeout       = 30_000
            setRequestProperty("Content-Type", "application/octet-stream")
            setFixedLengthStreamingMode(uploadBytes.toLong())
        }
        conn.connect()

        val start = System.nanoTime()
        var sentBytes = 0L
        var lastReport = start
        val chunkSize = 65_536

        conn.outputStream.use { out: OutputStream ->
            var offset = 0
            while (offset < payload.size) {
                val len = minOf(chunkSize, payload.size - offset)
                out.write(payload, offset, len)
                offset += len
                sentBytes += len

                val now = System.nanoTime()
                if (now - lastReport >= 200_000_000L) {
                    val elapsed = (now - start) / 1e9
                    onProgress(bytesToMbps(sentBytes, elapsed))
                    lastReport = now
                }
            }
        }

        // Drain response (required to complete the HTTP exchange)
        runCatching { conn.inputStream.use { it.readBytes() } }

        val elapsed = (System.nanoTime() - start) / 1e9
        return bytesToMbps(sentBytes, elapsed)
    }

    private fun bytesToMbps(bytes: Long, elapsedSeconds: Double): Float =
        if (elapsedSeconds <= 0) 0f
        else ((bytes * 8.0) / (elapsedSeconds * 1_000_000.0)).toFloat()
}
