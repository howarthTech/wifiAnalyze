package com.wifianalyze.ui.advanced

import androidx.lifecycle.ViewModel
import com.wifianalyze.data.local.LatencyHistoryDao
import com.wifianalyze.data.local.SignalHistoryDao
import com.wifianalyze.data.local.SpeedTestHistoryDao
import com.wifianalyze.data.local.entity.LatencyHistoryEntity
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import com.wifianalyze.data.local.entity.SpeedTestHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

enum class HistoryRange(val label: String, val millis: Long) {
    DAY("24 Hours", 24 * 3600_000L),
    WEEK("7 Days", 7 * 24 * 3600_000L)
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val signalHistoryDao: SignalHistoryDao,
    private val speedTestHistoryDao: SpeedTestHistoryDao,
    private val latencyHistoryDao: LatencyHistoryDao
) : ViewModel() {

    private val _range = MutableStateFlow(HistoryRange.DAY)
    val range: StateFlow<HistoryRange> = _range.asStateFlow()

    val history: Flow<List<SignalHistoryEntity>> = _range.flatMapLatest { r ->
        val since = System.currentTimeMillis() - r.millis
        signalHistoryDao.getHistory(since)
    }

    val speedTestHistory: Flow<List<SpeedTestHistoryEntity>> = _range.flatMapLatest { r ->
        val since = System.currentTimeMillis() - r.millis
        speedTestHistoryDao.getHistory(since)
    }

    val latencyHistory: Flow<List<LatencyHistoryEntity>> = _range.flatMapLatest { r ->
        val since = System.currentTimeMillis() - r.millis
        latencyHistoryDao.getHistory(since)
    }

    fun setRange(r: HistoryRange) { _range.value = r }
}
