package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.adapters.AssigningAndUnpinAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.otherClasses.Animates
import com.example.pass.otherClasses.CheckedEquipment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class AssigningEquipmentToCabinetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assigning_equipment_to_cabinet)

        val cabinetId: Long = intent.getLongExtra("cabinetId", -1)
        val database: AppDatabase = AppDatabase.getDatabase(this)
        val adapter = AssigningAndUnpinAdapter()
        val closeButton: ImageView = findViewById(R.id.closeButton)
        val assigningButton: FrameLayout = findViewById(R.id.assigningEquipmentButton)

        if (cabinetId == -1L) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            finish()
        }

        createCardList(database, this, adapter)

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeActivity()
            }
        }

        assigningButton.setOnClickListener {
            Animates().animatesButton(it) {
                assigning(database, adapter, cabinetId)
            }
        }
    }

    private fun assigning(database: AppDatabase, adapter: AssigningAndUnpinAdapter, cabinetId: Long) {
        val selectionList = adapter.currentList.filter { it.isChecked }.map{ it.equipment.equipmentId }

        if (selectionList.isEmpty()) {
            Toast.makeText(this, "Требуется выбрать оборудование которое вы хотите закрепить за кабинетом!", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            database.equipmentDao().updateCabinetIdOnEquipment(selectionList, cabinetId)
        }

        finish()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createCardList(database: AppDatabase, context: Context, adapter: AssigningAndUnpinAdapter){
        val recyclerView: RecyclerView = findViewById(R.id.equipmentAssigningViewer)
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
                        database.equipmentDao().getEquipmentByNullCabinetId(text)
                    }
                    .collect { list ->
                        adapter.submitList(list.map { CheckedEquipment(it) })
                    }
            }
        }
    }

    private fun closeActivity() {
        finish()
    }

}