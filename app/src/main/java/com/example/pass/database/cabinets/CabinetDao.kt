package com.example.pass.database.cabinets

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pass.database.equipment.EquipmentWithNameCabinet
import kotlinx.coroutines.flow.Flow

@Dao
interface CabinetDao {

    @Insert
    suspend fun saveCabinet(cabinetEntity: CabinetEntity)

    @Query("SELECT * FROM Cabinet WHERE name LIKE '%' || :search || '%'")
    fun getAllCabinet(search: String): Flow<List<CabinetEntity>>

    @Query("""SELECT Equipment.*, Cabinet.name as nameCabinet
            FROM Equipment 
            LEFT JOIN Cabinet ON Equipment.cabinetId = Cabinet.cabinetId 
            WHERE Equipment.cabinetId = :cabinetId AND (identificationNumber LIKE '%' || :search || '%')""")
    fun getAllEquipmentOnCabinet(cabinetId: Long, search: String): Flow<List<EquipmentWithNameCabinet>>

    @Query("SELECT COUNT(*) FROM Cabinet WHERE name = :name AND cabinetId <> :cabinetId")
    suspend fun getCountCabinetForNameEdit(name: String, cabinetId: Long): Int

    @Query("SELECT COUNT(*) FROM Cabinet WHERE name = :name")
    suspend fun getCountCabinetForNameCreate(name: String): Int

    @Query("SELECT cabinetId FROM Cabinet WHERE name = :name")
    suspend fun getCabinetByName(name: String): Long?

    @Query("SELECT * FROM Cabinet WHERE cabinetId = :cabinetId")
    fun getCabinetByIdFlow(cabinetId: Long): Flow<CabinetEntity?>

    @Query("SELECT * FROM Cabinet WHERE cabinetId = :cabinetId")
    suspend fun getCabinetById(cabinetId: Long): CabinetEntity?

    @Update
    suspend fun updateCabinet(entityCabinet: CabinetEntity)

    @Delete
    suspend fun deleteCabinet(entityCabinet: CabinetEntity)
}