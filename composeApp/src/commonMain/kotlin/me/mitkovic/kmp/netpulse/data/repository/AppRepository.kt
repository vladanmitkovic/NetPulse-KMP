package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository

interface AppRepository {
    val speedTestRepository: SpeedTestRepository
}
