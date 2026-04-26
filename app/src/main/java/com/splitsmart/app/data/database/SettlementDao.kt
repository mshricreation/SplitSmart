package com.splitsmart.app.data.database

import androidx.room.*
import com.splitsmart.app.data.model.Settlement
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: Settlement)

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY settledAt DESC")
    fun getSettlementsForGroup(groupId: String): Flow<List<Settlement>>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId")
    suspend fun getSettlementsForGroupSnapshot(groupId: String): List<Settlement>

    @Delete
    suspend fun deleteSettlement(settlement: Settlement)
}
