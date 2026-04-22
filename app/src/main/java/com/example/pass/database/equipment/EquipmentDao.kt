package com.example.pass.database.equipment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

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

    @Query("SELECT COUNT(*) FROM Equipment WHERE identificationNumber = :number AND equipmentId <> :equipmentId")
    suspend fun getCountOnNumberEdit(number: String, equipmentId: Long): Int

    @Query("SELECT * FROM Equipment WHERE equipmentId = :equipmentId")
    suspend fun getEquipmentById(equipmentId: Long): EquipmentEntity?

    @Query("SELECT * FROM Equipment WHERE cabinetId = :cabinetId AND (identificationNumber LIKE '%' || :search || '%')")
    fun getEquipmentByCabinetIdFlow(cabinetId: Long, search: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM Equipment WHERE cabinetId = :cabinetId")
    fun getEquipmentByCabinetId(cabinetId: Long): List<EquipmentEntity>

    @Query("SELECT * FROM Equipment WHERE cabinetId IS NULL AND (identificationNumber LIKE '%' || :search || '%')")
    fun getEquipmentByNullCabinetId(search: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM Equipment WHERE stateEquipment = :state AND (identificationNumber LIKE '%' || :search || '%')")
    fun getEquipmentsByState(state: StateEquipment, search: String): Flow<List<EquipmentEntity>>

    @Query("SELECT equipmentId FROM Equipment WHERE identificationNumber = :number")
    suspend fun getEquipmentByIdentificationNumber(number: String): Long?

    @Query("UPDATE Equipment SET cabinetId = :cabinetId WHERE equipmentId in (:idx)")
    suspend fun updateCabinetIdOnEquipment(idx: List<Long>, cabinetId: Long)

    @Query("UPDATE Equipment SET cabinetId = NULL WHERE equipmentId in (:idx)")
    suspend fun updateCabinetIdForNullOnEquipment(idx: List<Long>)

    @Query("UPDATE Equipment SET stateEquipment = :state WHERE equipmentId in (:idx)")
    suspend fun updateStateForWorkerOnEquipment(idx: List<Long>, state: StateEquipment)

    @Query("UPDATE Equipment SET stateEquipment = :state, lastDateCheck = :date WHERE equipmentId = :equipmentId")
    suspend fun updateStateAndLastDateCheckById(equipmentId: Long, state: StateEquipment, date: Date)

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)

    @Query("DELETE FROM Equipment WHERE equipmentId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

}