package com.example.pass.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.example.pass.database.cabinets.CabinetEntity
import com.example.pass.otherClasses.Animates
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CabinetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cabinet)

        val cabinetId: Long = intent.getLongExtra("cabinetId", -1)
        val database: AppDatabase = AppDatabase.getDatabase(this)

        val nameActivity: TextView = findViewById(R.id.nameActivityCabinet)
        val typeText: TextView = findViewById(R.id.type)
        val widthText: TextView = findViewById(R.id.width)
        val heightText: TextView = findViewById(R.id.height)
        val lengthText: TextView = findViewById(R.id.length)
        val areaText: TextView = findViewById(R.id.area)

        val returnButton: ImageView = findViewById(R.id.returnButton)
        val editButton: ImageView = findViewById(R.id.editButton)
        val getQrButton: FrameLayout = findViewById(R.id.getQrButton)
        val assigningStartActivityButton: FrameLayout = findViewById(R.id.assigningStartActivity)
        val unpinStartActivityButton: FrameLayout = findViewById(R.id.unpinEquipmentButton)

        if (cabinetId == -1L) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            finish()
        }

        setDataOnCabinet(
            database,
            cabinetId,
            nameActivity,
            typeText,
            widthText,
            heightText,
            lengthText,
            areaText
        )

        createList(database, cabinetId)

        returnButton.setOnClickListener {
            Animates().animatesButton(it) {
                returnActivity()
            }
        }

        editButton.setOnClickListener {
            Animates().animatesButton(it) {
                editActivityStart(cabinetId)
            }
        }

        getQrButton.setOnClickListener {
            Animates().animatesButton(it) {
                generateQrAndDownload(database, cabinetId)
            }
        }

        assigningStartActivityButton.setOnClickListener {
            Animates().animatesButton(it) {
                assigningStart(cabinetId)
            }
        }

        unpinStartActivityButton.setOnClickListener {
            Animates().animatesButton(it) {
                unpinStart(cabinetId)
            }
        }
    }

    private fun setDataOnCabinet(
        database: AppDatabase,
        cabinetId: Long,
        nameActivity: TextView,
        typeText: TextView,
        widthText: TextView,
        heightText: TextView,
        lengthText: TextView,
        areaText: TextView
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.cabinetDao().getCabinetByIdFlow(cabinetId).collect { cabinet ->

                    if (cabinet != null) {
                        nameActivity.text = cabinet.name
                        typeText.text = this@CabinetActivity.getString(
                            R.string.equipment_type_text,
                            cabinet.typeCabinet.nameDescription
                        )
                        widthText.text = this@CabinetActivity.getString(
                            R.string.equipment_width_text,
                            cabinet.width
                        )
                        heightText.text = this@CabinetActivity.getString(
                            R.string.equipment_height_text,
                            cabinet.height
                        )
                        lengthText.text = this@CabinetActivity.getString(
                            R.string.equipment_length_text,
                            cabinet.length
                        )
                        areaText.text = this@CabinetActivity.getString(
                            R.string.equipment_area_text,
                            cabinet.width.toLong() * cabinet.length.toLong()
                        )
                    } else {
                        Toast.makeText(this@CabinetActivity, "Произошла ошибка!", Toast.LENGTH_LONG)
                            .show()
                        finish()
                    }
                }
            }
        }
    }

    private fun unpinStart(cabinetId: Long) {
        val intent = Intent(this, UnpinEquipmentToCabinetActivity::class.java)
        intent.putExtra("cabinetId", cabinetId)
        startActivity(intent)
    }

    private fun assigningStart(cabinetId: Long) {
        val intent = Intent(this, AssigningEquipmentToCabinetActivity::class.java)
        intent.putExtra("cabinetId", cabinetId)
        startActivity(intent)
    }

    private fun generateQrAndDownload(database: AppDatabase, cabinetId: Long) {
        lifecycleScope.launch {
            val cabinet = database.cabinetDao().getCabinetById(cabinetId)

            if (cabinet != null) {
                val qr: Bitmap? = generateQr("cabinet:/:${cabinet.name}")

                if (qr != null) {
                    downloadQr(this@CabinetActivity, qr, cabinet.name)
                } else {
                    Toast.makeText(
                        this@CabinetActivity, "Произошла ошибка, не получилось достать qr код!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@CabinetActivity, "Произошла ошибка, возможно оборудование удалено!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun downloadQr(context: Context, qr: Bitmap, name: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${name}_qr.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/${context.getString(R.string.app_name)}"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            try {
                resolver.openOutputStream(it).use { stream ->
                    if (stream != null) {
                        qr.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, values, null, null)
                Toast.makeText(context, "Сохранено в галерею", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                resolver.delete(it, null, null)
                Toast.makeText(context, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateQr(text: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun editActivityStart(cabinetId: Long) {
        val intent = Intent(this, EditCabinetActivity::class.java)
        intent.putExtra("cabinetId", cabinetId)
        startActivity(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun createList(database: AppDatabase, cabinetId: Long) {
        val recyclerView: RecyclerView = findViewById(R.id.equipmentViewer)
        val searchInput: EditText = findViewById(R.id.search_input_identification)

        val adapter = EquipmentAdapter(true) {}

        recyclerView.layoutManager = LinearLayoutManager(this@CabinetActivity)
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
                        database.cabinetDao().getAllEquipmentOnCabinet(cabinetId, text)
                    }
                    .collect { list ->
                        adapter.submitList(list)
                    }
            }
        }

    }

    private fun returnActivity() {
        finish()
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