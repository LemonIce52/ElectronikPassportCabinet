package com.example.pass.database.equipment

import androidx.room.Embedded

data class EquipmentWithNameCabinet(
    @Embedded val equipment: EquipmentEntity,
    val nameCabinet: String?
)
