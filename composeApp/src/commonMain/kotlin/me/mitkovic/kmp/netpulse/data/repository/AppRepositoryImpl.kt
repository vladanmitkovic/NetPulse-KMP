package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository

class AppRepositoryImpl(
    override val speedTestRepository: SpeedTestRepository,
) : AppRepository
