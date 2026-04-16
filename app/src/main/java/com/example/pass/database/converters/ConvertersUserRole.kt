package com.example.pass.database.converters

import androidx.room.TypeConverter
import com.example.pass.database.users.Role

class ConvertersUserRole {
    @TypeConverter
    fun fromRole(role: Role): String {
        return role.toString()
    }

    @TypeConverter
    fun toRole(role: String): Role {
        return Role.valueOf(role)
    }
}