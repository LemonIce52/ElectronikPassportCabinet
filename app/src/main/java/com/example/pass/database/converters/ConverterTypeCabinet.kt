package com.example.pass.database.converters

import androidx.room.TypeConverter
import com.example.pass.database.cabinets.TypeCabinet

class ConverterTypeCabinet {

    @TypeConverter
    fun fromType(typeCabinet: TypeCabinet): String {
        return typeCabinet.toString()
    }

    @TypeConverter
    fun toType(typeCabinet: String): TypeCabinet {
        return TypeCabinet.valueOf(typeCabinet)
    }

}