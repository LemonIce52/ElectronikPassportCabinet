package com.example.pass.database.equipment

enum class StateEquipment(val nameDescription: String) {
    NEW("Новое"), WORKING("Работает"), BROKEN("Сломано"), WRITTEN_OFF("Списано")
}
