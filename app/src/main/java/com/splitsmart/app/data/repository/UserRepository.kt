package com.splitsmart.app.data.repository

import com.splitsmart.app.data.database.UserDao
import com.splitsmart.app.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun insertUsers(users: List<User>) = userDao.insertUsers(users)

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}
