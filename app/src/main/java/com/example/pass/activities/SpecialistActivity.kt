package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.adapters.CabinetAdapter
import com.example.pass.database.AppDatabase
import com.example.pass.database.documents.TypeDocument
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SpecialistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialist)

        val userid: Long = intent.getLongExtra("currUserId", -1)
        val database: AppDatabase = AppDatabase.getDatabase(this)

        val addCabinetButton: FrameLayout = findViewById(R.id.addCabinetButton)
        val equipmentStartButton: FrameLayout = findViewById(R.id.equipmentStartButton)
        val documentationButton: FrameLayout = findViewById(R.id.documentsButton)
        val scanQRButton: FrameLayout = findViewById(R.id.scanQR)

        createList(database)

        addCabinetButton.setOnClickListener { Animates().animatesButton(it) { addCabinet() } }
        equipmentStartButton.setOnClickListener { Animates().animatesButton(it) { equipmentStart() } }
        documentationButton.setOnClickListener {
            Animates().animatesButton(it) {
                getDocumentsActivity(
                    this,
                    userid
                )
            }
        }
        scanQRButton.setOnClickListener { Animates().animatesButton(it) { scanQr() } }

    }

    private fun scanQr() {
        val intent = Intent(this, ScanQRActivity::class.java)
        startActivity(intent)
    }

    private fun equipmentStart() {
        val intent = Intent(this, EquipmentActivity::class.java)
        startActivity(intent)
    }

    private fun addCabinet() {
        val intent = Intent(this, AddCabinetActivity::class.java)
        startActivity(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun createList(database: AppDatabase) {
        val recyclerView: RecyclerView = findViewById(R.id.cabinetViewer)
        val searchInput: EditText = findViewById(R.id.searchCabinetWithName)

        val adapter = CabinetAdapter { cabinetId ->
            val intent = Intent(this, CabinetActivity::class.java)
            intent.putExtra("cabinetId", cabinetId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
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
                        database.cabinetDao().getAllCabinet(text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }

    }

    private fun getDocumentsActivity(context: Context, userId: Long) {
        val intent = Intent(context, DocumentsActivity::class.java)

        intent.putExtra(
            "nameTypeDocumentsList",
            arrayListOf(TypeDocument.ACT_OF_ACCEPTANCE.name, TypeDocument.WRITE_OF_ACT.name)
        )
        intent.putExtra("currUserId", userId)
        intent.putExtra("nameActivity", "Акты")

        startActivity(intent)
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