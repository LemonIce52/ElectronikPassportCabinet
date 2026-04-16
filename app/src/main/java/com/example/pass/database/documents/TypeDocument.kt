package com.example.pass.database.documents

enum class TypeDocument(val nameDocument: String) {
    SHOPPING_PLAN("План закупок"),
    FORECAST_OF_PLANNED_COSTS("Прогноз планируемых затрат"),
    WRITE_OF_ACT("Акт принятия"),
    ACT_OF_ACCEPTANCE("Акт списания")
}
