package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestServersRepository

interface NetPulseRepository {
    val speedTestServersRepository: SpeedTestServersRepository
}
