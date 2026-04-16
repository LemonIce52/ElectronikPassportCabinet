package com.example.pass.database.converters

import androidx.room.TypeConverter
import java.util.Date

class ConverterDate {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}