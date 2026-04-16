package com.example.pass.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pass.R
import com.example.pass.database.documents.TypeDocument
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.adapters.BuysAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.database.documents.DocumentEntity
import com.example.pass.dialog.CloseDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import kotlinx.coroutines.launch
import java.util.Date

class CreateDocumentsActivity : AppCompatActivity() {

    private var selectionPeriod: Pair<Long, Long> = Pair(
        MaterialDatePicker.todayInUtcMilliseconds(),
        MaterialDatePicker.todayInUtcMilliseconds()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_documentation)

        val panelPlanShopping: LinearLayout = findViewById(R.id.planPrice)
        val panelPlanBuying: LinearLayout = findViewById(R.id.buying)

        val nameTypeDocumentationList: List<String>? =
            intent.getStringArrayListExtra("nameTypeDocumentsList")
        val typeDocumentationList: List<TypeDocument> =
            nameTypeDocumentationList?.map { TypeDocument.valueOf(it) } ?: listOf()
        val userId: Long = intent.getLongExtra("currUserId", -1)

        if (userId == -1L) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            finish()
        }

        val database: AppDatabase = AppDatabase.getDatabase(this)

        val typeDocumentsSpinner: Spinner = findViewById(R.id.typeDocument)
        val closeButton: ImageView = findViewById(R.id.closeButton)
        val createDocument: FrameLayout = findViewById(R.id.create_document)
        val period: EditText = findViewById(R.id.periodInput)
        val planPriceInput: EditText = findViewById(R.id.planPriceInput)

        val nameOborudInput: EditText = findViewById(R.id.name_equipment_input)
        val priceInput: EditText = findViewById(R.id.priceInput)
        val countInput: EditText = findViewById(R.id.countInput)
        val listOborud: MutableList<Buys> = ArrayList()
        val addOborudButton: FrameLayout = findViewById(R.id.addOborud)
        val recyclerItems: RecyclerView = findViewById(R.id.oborudViewer)

        lateinit var adapter: BuysAdapter

        adapter = BuysAdapter { position ->
            listOborud.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, listOborud.size)
            adapter.submitList(listOborud.toList())
        }

        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.adapter = adapter

        addOborudButton.setOnClickListener {
            Animates().animatesButton(it) {
                addOborud(listOborud, nameOborudInput, priceInput, countInput)
                adapter.submitList(listOborud.toList())
            }
        }

        period.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.MyDatePickerStyle)
                .setTitleText("Выберите период")
                .setSelection(selectionPeriod)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            dateRangePicker.show(supportFragmentManager, "range_picker")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second

                selectionPeriod = Pair(
                    startDate,
                    endDate
                )

                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val result = "${formatter.format(startDate)} - ${formatter.format(endDate)}"
                period.setText(result)
            }
        }

        createSpinnerDropDown(
            typeDocumentationList,
            typeDocumentsSpinner,
            panelPlanBuying,
            panelPlanShopping
        )

        closeButton.setOnClickListener { Animates().animatesButton(it) { closeButton(listOborud, period, planPriceInput) } }
        createDocument.setOnClickListener {
            Animates().animatesButton(it) {

                val selectedItem: Int = typeDocumentsSpinner.selectedItemPosition

                when (typeDocumentationList[selectedItem]) {
                    TypeDocument.FORECAST_OF_PLANNED_COSTS -> {
                        if (validationPeriodAndPlanPrice(planPriceInput, period)) return@animatesButton

                        val documentHTML: String = DocumentsHTML().generateBudgetHtml(planPriceInput.text.toString(), period.text.toString())

                        createDocument(
                            database,
                            typeDocumentationList[selectedItem],
                            documentHTML,
                            userId
                        )
                    }

                    TypeDocument.SHOPPING_PLAN -> {
                        if (listOborud.isEmpty()) {
                            Toast.makeText(this, "Список добавленного оборудования пуст!", Toast.LENGTH_LONG).show()
                            return@animatesButton
                        }

                        val documentHTML: String = DocumentsHTML().generatePurchaseReportHtml(listOborud)

                        createDocument(
                            database,
                            typeDocumentationList[selectedItem],
                            documentHTML,
                            userId
                        )
                    }
                    else -> {}
                }

                finish()
            }
        }
    }

    private fun addOborud(
        listOborud: MutableList<Buys>,
        nameOborudInput: EditText,
        priceInput: EditText,
        countInput: EditText
    ) {

        if (nameOborudInput.text.isEmpty()) {
            nameOborudInput.error = "Название не может быть пустым!"
            return
        }

        if (priceInput.text.isEmpty()) {
            priceInput.error = "Цена не может быть пустой!"
            return
        }

        if (countInput.text.isEmpty()) {
            countInput.error = "Количество не может быть пустым!"
            return
        }

        val price: Int? = priceInput.text.toString().toIntOrNull()
        val count: Int? = countInput.text.toString().toIntOrNull()

        if (price == null) {
            priceInput.error = "Не верный формат числа!"
            return
        }

        if (price <= 0) {
            priceInput.error = "Цена не может быть меньше или равной 0!"
            return
        }

        if (count == null) {
            countInput.error = "Не верный формат числа!"
            return
        }

        if (count <= 0) {
            countInput.error = "Количество не может быть меньше или равно 0!"
            return
        }

        val buys = Buys(nameOborudInput.text.toString(), count, price)

        listOborud.forEach { element ->
            if (element.name == buys.name) {
                Toast.makeText(this, "Оборудование с таким именем уже есть!", Toast.LENGTH_LONG).show()
                return
            }
        }

        listOborud.add(buys)
    }

    private fun validationPeriodAndPlanPrice(
        planPriceInput: EditText,
        period: EditText
    ): Boolean {
        if (planPriceInput.text.toString().isEmpty()) {
            planPriceInput.error = "Данное поле не может быть пустым!"
            return true
        }

        val planPrice: Int? = planPriceInput.text.toString().toIntOrNull()

        if (planPrice == null) {
            planPriceInput.error = "Не верный формат числа!"
            return true
        }

        if (planPrice <= 0) {
            planPriceInput.error = "Число не может быть отрицательным или равным 0!"
            return true
        }

        if (period.text.toString().isEmpty()) {
            Toast.makeText(this, "Период не может быть пустым!", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    private fun createDocument(
        database: AppDatabase,
        typeDocument: TypeDocument,
        documentDescriptor: String,
        userId: Long
    ) {
        lifecycleScope.launch {
            val documentEntity = DocumentEntity(
                typeDocument = typeDocument,
                nameDocument = typeDocument.nameDocument,
                description = documentDescriptor,
                dateCreated = Date(),
                creatorId = userId
            )

            database.documentDao().saveDocument(documentEntity)
        }
    }

    private fun createSpinnerDropDown(
        typeDocumentationList: List<TypeDocument>,
        typeDocumentsSpinner: Spinner,
        panelPlanBuying: LinearLayout,
        panelPlanShopping: LinearLayout
    ) {
        val typeDocumentsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            typeDocumentationList.map { it.nameDocument }
        )

        typeDocumentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        typeDocumentsSpinner.adapter = typeDocumentsAdapter


        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (typeDocumentsSpinner.selectedItem.equals(TypeDocument.SHOPPING_PLAN.nameDocument)) {
                    panelPlanBuying.visibility = View.VISIBLE
                    panelPlanShopping.visibility = View.GONE
                } else {
                    panelPlanBuying.visibility = View.GONE
                    panelPlanShopping.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        typeDocumentsSpinner.onItemSelectedListener = listener
    }

    private fun closeButton(
        listOborud: MutableList<Buys>,
        period: EditText,
        planPriceInput: EditText
    ) {

        if (!listOborud.isEmpty() && !period.text.isEmpty() && !planPriceInput.text.isEmpty()) {
            val dialog = CloseDialog()
            dialog.show(supportFragmentManager, "CloseDialog")
        } else {
            finish()
        }
    }
}