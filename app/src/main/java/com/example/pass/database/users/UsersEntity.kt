package com.example.pass.database.users

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "User")
data class UsersEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val name: String,
    val lastName: String,
    val birthday: Date,
    val email: String,
    val password: String,
    val role: Role
)
