package com.wifianalyze.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

data class PingResult(
    val host: String,
    val minMs: Float,
    val avgMs: Float,
    val maxMs: Float,
    val packetLoss: Int   // 0–100 %
)

sealed class LatencyResult {
    object Idle : LatencyResult()
    object Running : LatencyResult()
    data class Success(val gateway: PingResult?, val internet: PingResult?) : LatencyResult()
    data class Error(val message: String) : LatencyResult()
}

@Singleton
class LatencyTestRunner @Inject constructor() {

    /**
     * Measures round-trip latency to [host] using 4 probes.
     *
     * Strategy:
     *  - For local hosts (192.168.x.x, 10.x.x.x, 172.16-31.x.x): InetAddress.isReachable (ICMP/TCP-7)
     *  - For internet hosts: TCP connect to port 53 (DNS), fast and firewall-friendly
     *
     * Returns null only if every probe fails.
     */
    suspend fun ping(host: String): PingResult? = withContext(Dispatchers.IO) {
        if (host.isBlank()) return@withContext null

        val count = 4
        val samples = mutableListOf<Float>()

        repeat(count) { i ->
            if (i > 0) delay(300)
            val ms = if (isLocalAddress(host)) probeReachable(host) else probeTcp(host, 53)
            if (ms != null) samples.add(ms)
        }

        if (samples.isEmpty()) null
        else PingResult(
            host = host,
            minMs = samples.min(),
            avgMs = samples.average().toFloat(),
            maxMs = samples.max(),
            packetLoss = ((count - samples.size) * 100) / count
        )
    }

    // ── probes ────────────────────────────────────────────────────────────────

    private fun probeReachable(host: String): Float? = try {
        val addr = InetAddress.getByName(host)
        val start = System.nanoTime()
        val ok = addr.isReachable(2000)
        if (ok) (System.nanoTime() - start) / 1_000_000f else null
    } catch (_: Exception) { null }

    private fun probeTcp(host: String, port: Int): Float? = try {
        val start = System.nanoTime()
        Socket().use { s ->
            s.connect(InetSocketAddress(host, port), 2000)
        }
        (System.nanoTime() - start) / 1_000_000f
    } catch (_: Exception) { null }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun isLocalAddress(host: String): Boolean {
        val parts = host.split(".").mapNotNull { it.toIntOrNull() }
        if (parts.size != 4) return false
        return parts[0] == 10 ||
                parts[0] == 192 && parts[1] == 168 ||
                parts[0] == 172 && parts[1] in 16..31
    }
}
