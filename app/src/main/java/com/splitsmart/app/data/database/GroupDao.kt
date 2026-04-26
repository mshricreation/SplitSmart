package com.splitsmart.app.data.database

import androidx.room.*
import com.splitsmart.app.data.model.Group
import com.splitsmart.app.data.model.GroupMember
import com.splitsmart.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    // ── Group CRUD ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): Group?

    @Delete
    suspend fun deleteGroup(group: Group)

    // ── Member management ───────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMember(member: GroupMember)

    @Delete
    suspend fun removeMember(member: GroupMember)

    /** Returns all users who are members of the given group. */
    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN group_members gm ON u.id = gm.userId
        WHERE gm.groupId = :groupId
        ORDER BY u.name ASC
        """
    )
    fun getMembersOfGroup(groupId: String): Flow<List<User>>

    /** Checks membership — used to prevent duplicate addition. */
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun isMember(groupId: String, userId: String): Int
}
