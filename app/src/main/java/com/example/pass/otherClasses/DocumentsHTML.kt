package com.example.pass.otherClasses

import com.example.pass.database.equipment.EquipmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentsHTML {

    fun generateBudgetHtml(
        amount: String,
        period: String
    ): String {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: sans-serif; color: #333; line-height: 1.6; padding: 20px; }
            .header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; }
            .title { font-size: 18pt; font-weight: bold; text-transform: uppercase; }
            .info-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            .info-table td { padding: 10px; border: 1px solid #ccc; }
            .label { font-weight: bold; background-color: #f2f2f2; width: 30%; }
            .content { margin-top: 30px; text-align: justify; }
            .footer { margin-top: 50px; font-size: 10pt; }
            .signature { margin-top: 40px; border-top: 1px solid #000; width: 200px; text-align: center; }
        </style>
    </head>
    <body>
        <div class="header">
            <div class="title">Отчет о планируемых затратах</div>
            <div>Дата формирования: $currentDate</div>
        </div>

        <div class="content">
            <p>Настоящим документом подтверждается план целевого расходования денежных средств в рамках установленного регламента организации. Ниже приведены детализированные данные по планируемой финансовой операции:</p>
        </div>

        <table class="info-table">
            <tr>
                <td class="label">Планируемая сумма</td>
                <td><strong>$amount</strong></td>
            </tr>
            <tr>
                <td class="label">Период действия</td>
                <td>$period</td>
            </tr>
        </table>

        <div class="content">
            <p><strong>Пояснительная записка:</strong><br>
            Указанные затраты обоснованы производственной необходимостью и соответствуют утвержденному бюджетному лимиту на текущий отчетный период. Ответственное лицо обязуется предоставить отчетную документацию (чеки, квитанции, акты) в течение трех рабочих дней после завершения указанного периода.</p>
        </div>

        <div class="footer">
            <p>Документ сформирован автоматически в системе учета затрат.</p>
            <div class="signature">Подпись ответственного лица</div>
        </div>
    </body>
    </html>
    """.trimIndent()
    }

    fun generatePurchaseReportHtml(
        items: List<Buys>,
    ): String {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val totalSum = items.sumOf { it.count * it.price }

        val tableRows = items.joinToString("") { item ->
            """
        <tr>
            <td>${item.name}</td>
            <td style="text-align: center;">${item.count}</td>
            <td style="text-align: right;">${item.price}</td>
            <td style="text-align: right;">${item.count * item.price}</td>
        </tr>
        """.trimIndent()
        }

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: "Times New Roman", serif; color: #000; padding: 40px; line-height: 1.5; }
            .org-header { text-align: left; margin-bottom: 40px; font-size: 10pt; text-transform: uppercase; border-bottom: 1px solid #000; padding-bottom: 10px; }
            .header { text-align: center; margin-bottom: 30px; }
            .title { font-size: 16pt; font-weight: bold; margin-bottom: 5px; }
            
            .intro { margin-bottom: 20px; text-align: justify; text-indent: 30px; }
            
            .items-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
            .items-table th { background-color: #f5f5f5; font-weight: bold; text-align: center; }
            .items-table td, .items-table th { padding: 10px; border: 1px solid #000; font-size: 11pt; }
            
            .total-row { font-weight: bold; }
            .summary-text { margin-top: 20px; font-weight: bold; }
            
            .footer { margin-top: 60px; width: 100%; }
            .sig-table { width: 100%; margin-top: 40px; border: none; }
            .sig-table td { border: none; padding: 10px 0; }
            .sig-line { border-bottom: 1px solid #000; width: 200px; display: inline-block; }
        </style>
    </head>
    <body>
        <div class="org-header">
            Наименование организации: ЧУ ВО «Московская академия предпринимательства»<br>
            Адрес: Планетная улица, 36, Москва, 125319
        </div>

        <div class="header">
            <div class="title">ВНУТРЕННИЙ ОТЧЕТ</div>
            <div style="font-size: 14pt;">о планируемом целевом расходовании денежных средств</div>
            <div>от $currentDate г.</div>
        </div>

        <div class="intro">
            Настоящим документом подтверждается необходимость осуществления закупок материальных ценностей и оборудования для обеспечения бесперебойного функционирования рабочих процессов. Указанный перечень составлен на основании текущих производственных потребностей и соответствует утвержденной бюджетной политике организации.
        </div>

        <table class="items-table">
            <thead>
                <tr>
                    <th>Наименование позиции</th>
                    <th>Кол-во</th>
                    <th>Цена за ед.</th>
                    <th>Итоговая стоимость</th>
                </tr>
            </thead>
            <tbody>
                $tableRows
                <tr class="total-row">
                    <td colspan="3" style="text-align: right;">ОБЩАЯ СТОИМОСТЬ:</td>
                    <td style="text-align: right;">$totalSum</td>
                </tr>
            </tbody>
        </table>

        <div class="summary-text">
            Всего к закупке на сумму: $totalSum
        </div>

        <div class="intro" style="margin-top: 20px; font-style: italic; font-size: 10pt;">
            Ответственное лицо гарантирует использование приобретенных ТМЦ строго по назначению и обязуется предоставить закрывающие документы в установленный срок.
        </div>

        <table class="sig-table">
            <tr>
                <td>Менеджер по закупкам:</td>
                <td>________________ / ____________ /</td>
            </tr>
            <tr>
                <td style="font-size: 8pt;">(должность)</td>
                <td style="font-size: 8pt;">(подпись / расшифровка)</td>
            </tr>
            <tr>
                <td style="padding-top: 20px;">Руководитель подразделения:</td>
                <td style="padding-top: 20px;">________________ / ____________ /</td>
            </tr>
        </table>
    </body>
    </html>
    """.trimIndent()
    }

    fun generateWriteOfActHtml(
        items: List<EquipmentEntity>,
    ): String {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val tableRows = items.joinToString("") { item ->
            """
        <tr>
            <td>${item.name}</td>
            <td style="text-align: center;">${item.identificationNumber}</td>
        </tr>
        """.trimIndent()
        }

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: "Times New Roman", serif; color: #000; padding: 40px; line-height: 1.5; }
            .org-header { text-align: left; margin-bottom: 40px; font-size: 10pt; text-transform: uppercase; border-bottom: 1px solid #000; padding-bottom: 10px; }
            .header { text-align: center; margin-bottom: 30px; }
            .title { font-size: 16pt; font-weight: bold; margin-bottom: 5px; }
            
            .intro { margin-bottom: 20px; text-align: justify; text-indent: 30px; }
            
            .items-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
            .items-table th { background-color: #f5f5f5; font-weight: bold; text-align: center; }
            .items-table td, .items-table th { padding: 10px; border: 1px solid #000; font-size: 11pt; }
            
            .sig-table { width: 100%; margin-top: 50px; border: none; }
            .sig-table td { border: none; padding: 15px 0; }
        </style>
    </head>
    <body>
        <div class="org-header">
            Наименование организации: ЧУ ВО «Московская академия предпринимательства»<br>
            Адрес: Планетная улица, 36, Москва, 125319
        </div>

        <div class="header">
            <div class="title">ЗАКЛЮЧЕНИЕ О ТЕХНИЧЕСКОМ СОСТОЯНИИ</div>
            <div style="font-size: 14pt;">(АКТ СПИСАНИЯ)</div>
            <div>от $currentDate г.</div>
        </div>

        <div class="intro">
            Настоящий документ составлен техническим специалистом по результатам проверки состояния оборудования. В ходе осмотра установлено, что перечисленные ниже объекты <b>перестали работать и являются полностью неремонтопригодными</b>. Восстановление работоспособности признано невозможным или экономически нецелесообразным.
        </div>

        <table class="items-table">
            <thead>
                <tr>
                    <th>Наименование оборудования</th>
                    <th style="width: 30%;">Инвентарный номер</th>
                </tr>
            </thead>
            <tbody>
                $tableRows
            </tbody>
        </table>

        <div class="intro">
            Оборудование подлежит списанию с баланса организации и последующей утилизации.
        </div>

        <table class="sig-table">
            <tr>
                <td style="width: 40%;">Технический специалист:</td>
                <td>________________ / ____________ /</td>
            </tr>
            <tr>
                <td style="font-size: 8pt;">(должность)</td>
                <td style="font-size: 8pt;">(подпись / расшифровка)</td>
            </tr>
        </table>
    </body>
    </html>
    """.trimIndent()
    }

    fun generateAcceptanceActHtml(
        items: List<EquipmentEntity>,
    ): String {
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val tableRows = items.joinToString("") { item ->
            """
        <tr>
            <td>${item.name}</td>
            <td style="text-align: center;">${item.identificationNumber}</td>
        </tr>
        """.trimIndent()
        }

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <style>
            body { font-family: "Times New Roman", serif; color: #000; padding: 40px; line-height: 1.5; }
            .org-header { text-align: left; margin-bottom: 40px; font-size: 10pt; text-transform: uppercase; border-bottom: 1px solid #000; padding-bottom: 10px; }
            .header { text-align: center; margin-bottom: 30px; }
            .title { font-size: 16pt; font-weight: bold; margin-bottom: 5px; }
            
            .intro { margin-bottom: 20px; text-align: justify; text-indent: 30px; }
            
            .items-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
            .items-table th { background-color: #f5f5f5; font-weight: bold; text-align: center; }
            .items-table td, .items-table th { padding: 10px; border: 1px solid #000; font-size: 11pt; }
            
            .sig-table { width: 100%; margin-top: 50px; border: none; }
            .sig-table td { border: none; padding: 15px 0; }
        </style>
    </head>
    <body>
        <div class="org-header">
            Наименование организации: ЧУ ВО «Московская академия предпринимательства»<br>
            Адрес: Планетная улица, 36, Москва, 125319
        </div>

        <div class="header">
            <div class="title">АКТ ПРИЕМА-ПЕРЕДАЧИ ОБОРУДОВАНИЯ</div>
            <div style="font-size: 14pt;">(ВВОД В ЭКСПЛУАТАЦИЮ)</div>
            <div>от $currentDate г.</div>
        </div>

        <div class="intro">
            Настоящий акт подтверждает факт приема и постановки на учет перечисленного ниже оборудования. В ходе осмотра и проверки работоспособности установлено, что оборудование находится в исправном состоянии, дефектов не имеет и готово к эксплуатации. <b>В таблице ниже указан присвоенный каждому объекту уникальный инвентарный номер</b> для последующего учета в организации.
        </div>

        <table class="items-table">
            <thead>
                <tr>
                    <th>Наименование оборудования</th>
                    <th style="width: 35%;">Присвоенный инвентарный номер</th>
                </tr>
            </thead>
            <tbody>
                $tableRows
            </tbody>
        </table>

        <div class="intro">
            С момента подписания данного акта оборудование считается введенным в эксплуатацию и закрепляется за ответственным подразделением.
        </div>

        <table class="sig-table">
            <tr>
                <td style="width: 40%;">Технический специалист:</td>
                <td>________________ / ____________ /</td>
            </tr>
            <tr>
                <td style="font-size: 8pt;">(должность)</td>
                <td style="font-size: 8pt;">(подпись / расшифровка)</td>
            </tr>
        </table>
    </body>
    </html>
    """.trimIndent()
    }
}