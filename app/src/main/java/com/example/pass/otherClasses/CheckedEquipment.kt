package com.example.pass.otherClasses

import com.example.pass.database.equipment.EquipmentEntity

data class CheckedEquipment(
    val equipment: EquipmentEntity,
    var isChecked: Boolean = false
)
