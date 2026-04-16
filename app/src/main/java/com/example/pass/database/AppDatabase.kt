package com.example.pass.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pass.database.cabinets.CabinetDao
import com.example.pass.database.cabinets.CabinetEntity
import com.example.pass.database.converters.ConverterTypeDocument
import com.example.pass.database.documents.DocumentDao
import com.example.pass.database.documents.DocumentEntity
import com.example.pass.database.converters.ConverterDate
import com.example.pass.database.converters.ConverterEquipmentType
import com.example.pass.database.converters.ConverterStateEquipment
import com.example.pass.database.converters.ConverterTypeCabinet
import com.example.pass.database.converters.ConvertersUserRole
import com.example.pass.database.equipment.EquipmentDao
import com.example.pass.database.equipment.EquipmentEntity
import com.example.pass.database.users.UsersDao
import com.example.pass.database.users.UsersEntity

@Database(
    entities = [
        UsersEntity::class,
        DocumentEntity::class,
        CabinetEntity::class,
        EquipmentEntity::class
               ],
    version = 7
)
@TypeConverters(value = [
    ConvertersUserRole::class,
    ConverterDate::class,
    ConverterTypeDocument::class,
    ConverterStateEquipment::class,
    ConverterTypeCabinet::class,
    ConverterEquipmentType::class
])
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao

    abstract fun documentDao(): DocumentDao
    abstract fun cabinetDao(): CabinetDao
    abstract fun equipmentDao(): EquipmentDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}