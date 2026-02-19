package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("DELETE FROM CurrentLocation")
    suspend fun clearCurrentLocation()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentLocation(entity: CurrentLocationEntity)

    @Transaction
    suspend fun replaceCurrentLocation(entity: CurrentLocationEntity) {
        clearCurrentLocation()
        insertCurrentLocation(entity)
    }

    @Query("SELECT * FROM CurrentLocation LIMIT 1")
    fun observeCurrentLocation(): Flow<CurrentLocationEntity?>

    @Insert
    suspend fun insertTestLocation(entity: TestLocationEntity): Long

    @Query("SELECT id FROM TestLocation WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    suspend fun getTestLocationIdByLatLon(lat: Double, lon: Double): Long?
}
