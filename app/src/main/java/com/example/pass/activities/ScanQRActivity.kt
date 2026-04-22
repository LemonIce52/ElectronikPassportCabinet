package com.example.pass.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.dialog.CardEquipmentDialog
import com.example.pass.dialog.CartCabinetScanningDialog
import com.example.pass.otherClasses.Animates
import com.journeyapps.barcodescanner.BarcodeView
import kotlinx.coroutines.launch

class ScanQRActivity : AppCompatActivity() {

    private lateinit var barcodeView: BarcodeView

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
        setContentView(R.layout.activity_scan_qr)

        val returnButton: ImageView = findViewById(R.id.returnButton)

        barcodeView = findViewById(R.id.barcodeView)

        startScanning()

        checkCameraPermission()

        returnButton.setOnClickListener{ Animates().animatesButton(it) { finish() } }


    }

    private fun startScanning() {
        barcodeView.decodeContinuous { result ->
            barcodeView.pause()
            handleQrResult(result.text)
        }
    }

    private fun handleQrResult(text: String) {
        val splitQr = text.split(":/:")
        val db = AppDatabase.getDatabase(this@ScanQRActivity)

        if (splitQr.size == 2) {
            when (splitQr[0]) {
                "cabinet" -> {
                    lifecycleScope.launch {
                        val cabinet = db.cabinetDao().getCabinetByName(splitQr[1])

                        if (cabinet != null) {
                            val dialog = CartCabinetScanningDialog.newInstance(cabinet)

                            supportFragmentManager.setFragmentResultListener("dialogClosed", this@ScanQRActivity) { _, _ ->
                                startScanningCamera()
                            }

                            dialog.show(supportFragmentManager, "CabinetDialog")
                        } else {
                            startScanningCamera()
                        }

                    }
                }
                "equipment" -> {
                    lifecycleScope.launch {
                        val equipment = db.equipmentDao().getEquipmentByIdentificationNumber(splitQr[1])

                        if (equipment != null) {
                            val dialog = CardEquipmentDialog.newInstance(equipment, false)

                            supportFragmentManager.setFragmentResultListener("dialogClosed", this@ScanQRActivity) { _, _ ->
                                startScanningCamera()
                            }

                            dialog.show(supportFragmentManager, "CabinetDialog")
                        } else {
                            startScanningCamera()
                        }
                    }
                }

                else -> {startScanningCamera()}
            }

        } else {
            startScanningCamera()
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
            == PackageManager.PERMISSION_GRANTED) {
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