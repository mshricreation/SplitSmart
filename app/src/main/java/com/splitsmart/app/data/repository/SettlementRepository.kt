package com.splitsmart.app.data.repository

import com.splitsmart.app.data.database.SettlementDao
import com.splitsmart.app.data.model.Settlement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettlementRepository @Inject constructor(
    private val settlementDao: SettlementDao
) {
    fun getSettlementsForGroup(groupId: String): Flow<List<Settlement>> =
        settlementDao.getSettlementsForGroup(groupId)

    suspend fun getSettlementsSnapshot(groupId: String): List<Settlement> =
        settlementDao.getSettlementsForGroupSnapshot(groupId)

    suspend fun insertSettlement(settlement: Settlement) =
        settlementDao.insertSettlement(settlement)

    suspend fun deleteSettlement(settlement: Settlement) =
        settlementDao.deleteSettlement(settlement)
}
