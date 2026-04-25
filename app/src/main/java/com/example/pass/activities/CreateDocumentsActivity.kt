package com.example.pass.activities

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.adapters.AssigningAndUnpinAdapter
import com.example.pass.adapters.BuysAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.database.documents.DocumentEntity
import com.example.pass.database.equipment.StateEquipment
import com.example.pass.dialog.CloseDialog
import com.example.pass.otherClasses.Animates
import com.example.pass.otherClasses.Buys
import com.example.pass.otherClasses.CheckedEquipment
import com.example.pass.otherClasses.DocumentsHTML
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Date

class CreateDocumentsActivity : AppCompatActivity() {

    val firstElement: String = "Выберете тип документа"

    private var selectionPeriod: Pair<Long, Long> = Pair(
        MaterialDatePicker.todayInUtcMilliseconds(),
        MaterialDatePicker.todayInUtcMilliseconds()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_documentation)

        val panelPlanShopping: LinearLayout = findViewById(R.id.planPrice)
        val panelPlanBuying: LinearLayout = findViewById(R.id.buying)
        val panelActs: LinearLayout = findViewById(R.id.actsPanel)

        val adapterActs = AssigningAndUnpinAdapter()

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
        val createDocumentButton: FrameLayout = findViewById(R.id.create_document)
        val period: EditText = findViewById(R.id.periodInput)
        val planPriceInput: EditText = findViewById(R.id.planPriceInput)

        val nameEquipmentInput: EditText = findViewById(R.id.name_equipment_input)
        val priceInput: EditText = findViewById(R.id.priceInput)
        val countInput: EditText = findViewById(R.id.countInput)
        val listToBuyEquipment: MutableList<Buys> = ArrayList()
        val addEquipmentButton: FrameLayout = findViewById(R.id.addOborud)
        val recyclerItems: RecyclerView = findViewById(R.id.oborudViewer)

        lateinit var adapter: BuysAdapter

        adapter = BuysAdapter { position ->
            listToBuyEquipment.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, listToBuyEquipment.size)
            adapter.submitList(listToBuyEquipment.toList())
        }

        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.adapter = adapter

        addEquipmentButton.setOnClickListener {
            Animates().animatesButton(it) {
                addEquipment(listToBuyEquipment, nameEquipmentInput, priceInput, countInput)
                adapter.submitList(listToBuyEquipment.toList())
            }
        }

        period.setOnClickListener {
            createDialogDate(period)
        }

        createSpinnerDropDown(
            database,
            typeDocumentationList,
            typeDocumentsSpinner,
            panelPlanBuying,
            panelPlanShopping,
            panelActs,
            adapterActs
        )

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeButton(
                    listToBuyEquipment,
                    period,
                    planPriceInput
                )
            }
        }
        createDocumentButton.setOnClickListener {
            Animates().animatesButton(it) {
                createDocument(
                    typeDocumentsSpinner,
                    typeDocumentationList,
                    planPriceInput,
                    period,
                    database,
                    userId,
                    listToBuyEquipment,
                    adapterActs
                )
            }
        }
    }

    private fun createDocument(
        typeDocumentsSpinner: Spinner,
        typeDocumentationList: List<TypeDocument>,
        planPriceInput: EditText,
        period: EditText,
        database: AppDatabase,
        userId: Long,
        listToBuyEquipment: MutableList<Buys>,
        adapterActs: AssigningAndUnpinAdapter
    ) {
        if (typeDocumentsSpinner.selectedItem.equals(firstElement)) {
            Toast.makeText(this, "Нужно выбрать тип документа", Toast.LENGTH_LONG).show()
            return
        }

        val selectedItem: Int = typeDocumentsSpinner.selectedItemPosition - 1

        when (typeDocumentationList[selectedItem]) {
            TypeDocument.FORECAST_OF_PLANNED_COSTS -> {
                if (validationPeriodAndPlanPrice(planPriceInput, period)) return

                val documentHTML: String = DocumentsHTML().generateBudgetHtml(
                    planPriceInput.text.toString(),
                    period.text.toString()
                )

                addDocumentToDatabase(
                    database,
                    typeDocumentationList[selectedItem],
                    documentHTML,
                    userId
                )
            }

            TypeDocument.SHOPPING_PLAN -> {
                if (listToBuyEquipment.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Список добавленного оборудования пуст!",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val documentHTML: String =
                    DocumentsHTML().generatePurchaseReportHtml(listToBuyEquipment)

                addDocumentToDatabase(
                    database,
                    typeDocumentationList[selectedItem],
                    documentHTML,
                    userId
                )
            }

            TypeDocument.WRITE_OF_ACT -> {
                val listSelected = adapterActs.currentList.filter { entity -> entity.isChecked }
                    .map { entity -> entity.equipment }

                if (listSelected.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Требуется выбрать оборудование для создания акта списания!",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val documentHTML: String = DocumentsHTML().generateWriteOfActHtml(listSelected)

                lifecycleScope.launch {
                    database.equipmentDao()
                        .deleteByIds(listSelected.map { entity -> entity.equipmentId })
                }

                addDocumentToDatabase(
                    database,
                    typeDocumentationList[selectedItem],
                    documentHTML,
                    userId
                )
            }

            TypeDocument.ACT_OF_ACCEPTANCE -> {
                val listSelected = adapterActs.currentList.filter { entity -> entity.isChecked }
                    .map { entity -> entity.equipment }

                if (listSelected.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Требуется выбрать оборудование для создания акта принятия!",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val documentHTML: String = DocumentsHTML().generateAcceptanceActHtml(listSelected)

                lifecycleScope.launch {
                    database.equipmentDao().updateStateForWorkerOnEquipment(
                        listSelected.map { entity -> entity.equipmentId },
                        StateEquipment.WORKING
                    )
                }

                addDocumentToDatabase(
                    database,
                    typeDocumentationList[selectedItem],
                    documentHTML,
                    userId
                )
            }
        }

        finish()
    }

    private fun createDialogDate(period: EditText) {
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

    private fun addEquipment(
        listEquipment: MutableList<Buys>,
        nameEquipmentInput: EditText,
        priceInput: EditText,
        countInput: EditText
    ) {

        val price: Int? = priceInput.text.toString().toIntOrNull()
        val count: Int? = countInput.text.toString().toIntOrNull()

        if (validationDataInputs(nameEquipmentInput, priceInput, countInput, price, count)) return

        val buys = Buys(nameEquipmentInput.text.toString(), count!!, price!!)

        priceInput.setText(null)
        countInput.setText(null)
        nameEquipmentInput.setText(null)

        listEquipment.forEach { element ->
            if (element.name == buys.name) {
                Toast.makeText(this, "Оборудование с таким именем уже есть!", Toast.LENGTH_LONG)
                    .show()
                return
            }
        }

        listEquipment.add(buys)
    }

    private fun validationDataInputs(
        nameEquipmentInput: EditText,
        priceInput: EditText,
        countInput: EditText,
        price: Int?,
        count: Int?
    ): Boolean {
        if (nameEquipmentInput.text.isEmpty()) {
            nameEquipmentInput.error = "Название не может быть пустым!"
            return true
        }

        if (priceInput.text.isEmpty()) {
            priceInput.error = "Цена не может быть пустой!"
            return true
        }

        if (countInput.text.isEmpty()) {
            countInput.error = "Количество не может быть пустым!"
            return true
        }

        if (price == null) {
            priceInput.error = "Не верный формат числа!"
            return true
        }

        if (price <= 0) {
            priceInput.error = "Цена не может быть меньше или равной 0!"
            return true
        }

        if (count == null) {
            countInput.error = "Не верный формат числа!"
            return true
        }

        if (count <= 0) {
            countInput.error = "Количество не может быть меньше или равно 0!"
            return true
        }
        return false
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

    private fun addDocumentToDatabase(
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
        database: AppDatabase,
        typeDocumentationList: List<TypeDocument>,
        typeDocumentsSpinner: Spinner,
        panelPlanBuying: LinearLayout,
        panelPlanShopping: LinearLayout,
        panelActs: LinearLayout,
        adapter: AssigningAndUnpinAdapter
    ) {
        val list = mutableListOf(firstElement)
        typeDocumentationList.forEach {
            list.add(it.nameDocument)
        }

        val typeDocumentsAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            list
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return if (position == 0) {
                    View(context).apply {
                        layoutParams = LayoutParams(0, 0)
                        visibility = View.GONE
                    }
                } else {
                    super.getDropDownView(position, null, parent)
                }
            }
        }

        typeDocumentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        typeDocumentsSpinner.adapter = typeDocumentsAdapter


        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                panelPlanShopping.visibility = View.GONE
                panelPlanBuying.visibility = View.GONE
                panelActs.visibility = View.GONE

                when (typeDocumentsSpinner.selectedItem) {
                    TypeDocument.SHOPPING_PLAN.nameDocument -> panelPlanBuying.visibility =
                        View.VISIBLE

                    TypeDocument.FORECAST_OF_PLANNED_COSTS.nameDocument -> panelPlanShopping.visibility =
                        View.VISIBLE

                    TypeDocument.ACT_OF_ACCEPTANCE.nameDocument -> {
                        panelActs.visibility = View.VISIBLE
                        createCardList(
                            database, this@CreateDocumentsActivity, adapter,
                            StateEquipment.NEW
                        )
                    }

                    TypeDocument.WRITE_OF_ACT.nameDocument -> {
                        panelActs.visibility = View.VISIBLE
                        createCardList(
                            database, this@CreateDocumentsActivity, adapter,
                            StateEquipment.WRITTEN_OFF
                        )
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        typeDocumentsSpinner.onItemSelectedListener = listener
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createCardList(
        database: AppDatabase,
        context: Context,
        adapter: AssigningAndUnpinAdapter,
        state: StateEquipment
    ) {
        val recyclerView: RecyclerView = findViewById(R.id.equipmentActViewer)
        val searchInput: EditText = findViewById(R.id.search_input_identification)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val searchQuery = MutableStateFlow("")

        searchInput.addTextChangedListener {
            searchQuery.value = it.toString()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchQuery
                    .debounce(150)
                    .flatMapLatest { text ->
                        database.equipmentDao().getEquipmentsByState(state, text)
                    }
                    .collect { list ->
                        adapter.submitList(list.map { CheckedEquipment(it) })
                    }
            }
        }
    }

    private fun closeButton(
        listEquipment: MutableList<Buys>,
        period: EditText,
        planPriceInput: EditText
    ) {

        if (!listEquipment.isEmpty() && !period.text.isEmpty() && !planPriceInput.text.isEmpty()) {
            val dialog = CloseDialog()
            dialog.show(supportFragmentManager, "CloseDialog")
        } else {
            finish()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                // Если нажатие произошло вне области текущего EditText
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}