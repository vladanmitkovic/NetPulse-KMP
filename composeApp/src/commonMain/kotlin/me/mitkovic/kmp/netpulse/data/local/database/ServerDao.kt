package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("DELETE FROM Server")
    suspend fun clearServers()

    @Upsert
    suspend fun upsertServers(servers: List<ServerEntity>)

    @Transaction
    suspend fun replaceServers(servers: List<ServerEntity>) {
        clearServers()
        upsertServers(servers)
    }

    @Query("SELECT * FROM Server")
    fun observeServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM Server WHERE id = :serverId")
    fun observeServerById(serverId: String): Flow<List<ServerEntity>>
}
