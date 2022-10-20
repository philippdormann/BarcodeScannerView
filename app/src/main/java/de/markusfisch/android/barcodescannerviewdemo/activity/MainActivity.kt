package de.markusfisch.android.barcodescannerviewdemo.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import de.markusfisch.android.barcodescannerview.widget.BarcodeScannerView
import de.markusfisch.android.barcodescannerview.widget.BarcodeScannerView.OnBarcodeListener
import de.markusfisch.android.barcodescannerviewdemo.R
import de.markusfisch.android.zxingcpp.ZxingCpp

class MainActivity : Activity() {
    private var scannerView: BarcodeScannerView? = null
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this, R.string.error_camera,
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        checkPermissions()
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.text)
        scannerView = findViewById(R.id.scanner)
        scannerView?.setOnBarcodeListener(object : OnBarcodeListener {
            override fun onBarcodeRead(result: ZxingCpp.Result) {
                textView.post {
                    textView.text = result.text
                    Toast.makeText(applicationContext, result.text, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    public override fun onResume() {
        super.onResume()
        scannerView!!.openAsync(
            Camera.CameraInfo.CAMERA_FACING_BACK
        )
    }

    public override fun onPause() {
        super.onPause()
        scannerView!!.close()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = Manifest.permission.CAMERA
            if (checkSelfPermission(permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(permission), REQUEST_CAMERA)
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1
    }
}