package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.adapters.EquipmentAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class EquipmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment)

        val database: AppDatabase = AppDatabase.getDatabase(this)
        val returnButton: ImageView = findViewById(R.id.returnButton)
        val addEquipmentButton: FrameLayout = findViewById(R.id.addEquipmentButton)

        createList(database)

        returnButton.setOnClickListener { Animates().animatesButton(it) { finish() } }
        addEquipmentButton.setOnClickListener { Animates().animatesButton(it) { addEquipment() } }
    }

    private fun addEquipment() {
        val intent = Intent(this, AddEquipmentActivity::class.java)
        startActivity(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun createList(database: AppDatabase) {
        val recyclerView: RecyclerView = findViewById(R.id.equipmentViewer)
        val searchInput: EditText = findViewById(R.id.searchEquipmentWithName)

        val adapter = EquipmentAdapter { equipmentId ->
            val intent = Intent(this, EditEquipmentActivity::class.java)
            intent.putExtra("equipmentId", equipmentId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this@EquipmentActivity)
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
                        database.equipmentDao().getAllEquipmentWithCabinetName(text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}