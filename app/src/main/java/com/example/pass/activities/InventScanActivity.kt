package com.example.pass.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.dialog.CardEquipmentDialog
import com.example.pass.otherClasses.Animates
import com.journeyapps.barcodescanner.BarcodeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventScanActivity : AppCompatActivity() {

    private lateinit var barcodeView: BarcodeView
    private lateinit var spinner: Spinner
    private val nameAndIdentificationNumbers: MutableList<Pair<String, String>> = mutableListOf()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            barcodeView.resume()
        } else {
            Toast.makeText(this, "Нужен доступ к камере для сканирования", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invent_scan)

        val cabinetId = intent.getLongExtra("cabinetId", -1)
        val database = AppDatabase.getDatabase(this)

        if (cabinetId == -1L) {
            Toast.makeText(this, "Кабинет не найдет, возможно он удален!", Toast.LENGTH_LONG).show()
            finish()
        }

        lifecycleScope.launch {
            val cabinet = withContext(Dispatchers.IO) {
                database.cabinetDao().getCabinetById(cabinetId)
            }

            findViewById<TextView>(R.id.nameActivityDocumentation).text = getString(
                R.string.cabinet_name_activity_invent,
                cabinet?.name ?: ""
            )
        }

        getListEquipmentIdentificationAndName(database, cabinetId)

        val returnButton: FrameLayout = findViewById(R.id.endInvent)
        spinner = findViewById(R.id.equipments)
        barcodeView = findViewById(R.id.barcodeView)

        startScanning()

        checkCameraPermission()

        returnButton.setOnClickListener { Animates().animatesButton(it) { finish() } }
    }

    private fun getListEquipmentIdentificationAndName(database: AppDatabase, cabinetId: Long) {
        lifecycleScope.launch {
            val equipments = withContext(Dispatchers.IO) {
                database.equipmentDao().getEquipmentByCabinetId(cabinetId)
            }

            equipments.forEach {
                nameAndIdentificationNumbers.add(Pair(it.identificationNumber, it.name))
            }

            chekSize()
        }
    }

    private fun createSpinnerDropDown(spinner: Spinner) {
        val listString =
            mutableListOf("Осталось ${nameAndIdentificationNumbers.size} оборудований нажмите чтобы посмотреть")

        nameAndIdentificationNumbers.forEach { listString.add("${it.second}(${it.first})") }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            listString
        ) {
            override fun isEnabled(position: Int): Boolean {
                return false
            }

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

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

    }

    private fun startScanning() {
        barcodeView.decodeContinuous { result ->
            barcodeView.pause()
            handleQrResult(result.text)
        }
    }

    private fun handleQrResult(text: String) {
        val splitQr = text.split(":/:")
        val db = AppDatabase.getDatabase(this@InventScanActivity)

        if (splitQr.size == 2 && splitQr[0] == "equipment") {
            var isContains = false
            var pair: Pair<String, String>? = null

            nameAndIdentificationNumbers.forEach {
                if (it.first == splitQr[1]) {
                    isContains = true
                    pair = it
                }
            }

            if (!isContains) {
                Toast.makeText(this, "Данное оборудование не закреплено за данным кабинетом!",
                    Toast.LENGTH_LONG).show()
                startScanningCamera()
            } else {
                nameAndIdentificationNumbers.remove(pair)

                chekSize()

                lifecycleScope.launch {
                    val equipment =
                        db.equipmentDao().getEquipmentByIdentificationNumber(splitQr[1])

                    if (equipment != null) {
                        val dialog = CardEquipmentDialog.newInstance(equipment, true)
                        dialog.isCancelable = false

                        supportFragmentManager.setFragmentResultListener(
                            "dialogClosed",
                            this@InventScanActivity
                        ) { _, _ ->
                            startScanningCamera()
                        }

                        dialog.show(supportFragmentManager, "CabinetDialog")
                    } else {
                        startScanningCamera()
                    }
                }
            }

        } else {
            startScanningCamera()
        }
    }

    private fun chekSize() {
        val findAllEquipmentText: TextView = findViewById(R.id.allEquipmentFindText)
        val dropDownFragment: FrameLayout = findViewById(R.id.listDropDown)

        if (!nameAndIdentificationNumbers.isEmpty()) {
            findAllEquipmentText.visibility = View.GONE
            dropDownFragment.visibility = View.VISIBLE
            createSpinnerDropDown(spinner)
        } else {
            findAllEquipmentText.visibility = View.VISIBLE
            dropDownFragment.visibility = View.GONE
        }
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            barcodeView.resume()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            barcodeView.pause()
            startScanningCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private fun startScanningCamera() {
        barcodeView.resume()
        startScanning()
    }

}