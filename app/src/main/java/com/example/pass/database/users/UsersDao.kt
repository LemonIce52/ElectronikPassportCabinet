package com.example.pass.database.users

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {
    @Insert
    suspend fun savedUser(user: UsersEntity)

    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getUser(email: String): UsersEntity?

    @Query("SELECT COUNT(*) FROM User LIMIT 1")
    suspend fun getFirstUser(): Int

    @Query("""
    SELECT * FROM User 
    WHERE role = :role 
    AND (name || ' ' || lastName LIKE '%' || :search || '%')
""")
    fun getAllUserOnTechnical(role: Role, search: String = ""): Flow<List<UsersEntity>>

    @Query("SELECT COUNT(*) FROM User WHERE email = :email")
    suspend fun getUsersOnEmail(email: String): Int

    @Query("SELECT COUNT(*) FROM User WHERE email = :email AND userId <> :userId")
    suspend fun getUsersOnEmailEdit(email: String, userId: Long): Int

    @Query("SELECT COUNT(*) FROM User WHERE password = :password AND userId <> :userId")
    suspend fun getUsersOnPasswordEdit(password: String, userId: Long): Int

    @Query("SELECT * FROM User WHERE userId = :userId")
    suspend fun getUserOnId(userId: Long): UsersEntity?

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun getUserOnIdFlow(userId: Long): Flow<UsersEntity?>

    @Update
    suspend fun updateUserData(user: UsersEntity)

    @Delete
    suspend fun deleteUser(user: UsersEntity)
}
