package com.example.pass.database.converters

import androidx.room.TypeConverter
import com.example.pass.database.documents.TypeDocument

class ConverterTypeDocument {

    @TypeConverter
    fun fromTypeDocument(typeDocument: TypeDocument): String {
        return typeDocument.toString()
    }

    @TypeConverter
    fun toTypeDocument(stringTypeDocument: String): TypeDocument {
        return TypeDocument.valueOf(stringTypeDocument)
    }
}