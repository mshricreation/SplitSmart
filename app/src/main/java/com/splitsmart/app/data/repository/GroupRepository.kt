package com.splitsmart.app.data.repository

import com.splitsmart.app.data.database.GroupDao
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.GroupMember
import com.splitsmart.app.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDao: GroupDao
) {
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()

    suspend fun getGroupById(groupId: String): Group? = groupDao.getGroupById(groupId)

    suspend fun insertGroup(group: Group) = groupDao.insertGroup(group)

    suspend fun deleteGroup(group: Group) = groupDao.deleteGroup(group)

    fun getMembersOfGroup(groupId: String): Flow<List<User>> =
        groupDao.getMembersOfGroup(groupId)

    suspend fun addMember(groupId: String, userId: String) {
        groupDao.addMember(GroupMember(groupId = groupId, userId = userId))
    }

    suspend fun removeMember(groupId: String, userId: String) {
        groupDao.removeMember(GroupMember(groupId = groupId, userId = userId))
    }

    suspend fun isMember(groupId: String, userId: String): Boolean =
        groupDao.isMember(groupId, userId) > 0
}
