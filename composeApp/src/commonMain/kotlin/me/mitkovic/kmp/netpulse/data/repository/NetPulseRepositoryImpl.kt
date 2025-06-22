package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestServersRepository

class NetPulseRepositoryImpl(
    override val speedTestServersRepository: SpeedTestServersRepository,
) : NetPulseRepository
