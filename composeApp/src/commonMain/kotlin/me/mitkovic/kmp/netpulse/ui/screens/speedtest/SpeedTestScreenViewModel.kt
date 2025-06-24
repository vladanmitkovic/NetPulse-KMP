package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SpeedTestScreenViewModel(
    netPulseRepository: NetPulseRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
    }
}
