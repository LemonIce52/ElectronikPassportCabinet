package com.example.pass.database.equipment

enum class StateEquipment(val nameDescription: String) {
    NEW("Новое"), WORKING("Исправно"), BROKEN("Сломано"), WRITTEN_OFF("Списано")
}
