package com.example.pass.database.equipment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Insert
    suspend fun savedEquipment(equipment: EquipmentEntity)

    @Query("""
    SELECT Equipment.*, Cabinet.name as nameCabinet 
    FROM Equipment 
    LEFT JOIN Cabinet ON Equipment.cabinetId = Cabinet.cabinetId
    WHERE Equipment.name LIKE '%' || :search || '%'
""")
    fun getAllEquipmentWithCabinetName(search: String): Flow<List<EquipmentWithNameCabinet>>

    @Query("SELECT COUNT(*) FROM Equipment WHERE identificationNumber = :number")
    suspend fun getCountOnNumber(number: String): Int

    @Query("SELECT * FROM Equipment WHERE equipmentId = :equipmentId")
    suspend fun getEquipmentById(equipmentId: Long): EquipmentEntity?

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)

}