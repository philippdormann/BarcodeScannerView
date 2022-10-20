package de.markusfisch.android.barcodescannerview.widget

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import de.markusfisch.android.cameraview.widget.CameraView
import de.markusfisch.android.zxingcpp.ZxingCpp
import de.markusfisch.android.zxingcpp.ZxingCpp.readByteArray

class BarcodeScannerView : CameraView {
    interface OnBarcodeListener {
        fun onBarcodeRead(result: ZxingCpp.Result)
    }

    val formats: HashSet<ZxingCpp.Format> = HashSet()
    private val cropRect = Rect()
    private var onBarcodeListener: OnBarcodeListener? = null
    private var decoding = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun calculateCropRect(width: Int, height: Int) {

        cropRect[0, 0, width] = height

    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    /**
     * Set listener for recognized barcodes.
     *
     * @param listener listener for barcodes
     */
    fun setOnBarcodeListener(listener: OnBarcodeListener?) {
        onBarcodeListener = listener
    }

    private fun init(context: Context) {
        formats.add(ZxingCpp.Format.QR_CODE)
        setUseOrientationListener(true)
        setOnCameraListener(object : OnCameraListener {
            override fun onConfigureParameters(parameters: Camera.Parameters) {
                val modes = parameters.supportedSceneModes
                if (modes != null) {
                    for (mode in modes) {
                        if (Camera.Parameters.SCENE_MODE_BARCODE == mode) {
                            parameters.sceneMode = mode
                            break
                        }
                    }
                }
                setAutoFocus(parameters)
            }

            override fun onCameraError() {}
            override fun onCameraReady(camera: Camera) {
                val width = frameWidth
                val height = frameHeight
                calculateCropRect(width, height);
                val orientation = frameOrientation
                camera.setPreviewCallback { data: ByteArray?, camera1: Camera? ->
                    if (!decoding) {
                        return@setPreviewCallback
                    }
                    val result = readByteArray(
                        data!!,
                        width,
                        cropRect,
                        orientation,
                        formats,
                        tryHarder = false,
                        tryRotate = true,
                        tryInvert = false,
                        tryDownscale = false
                    ) ?: return@setPreviewCallback
                    if (onBarcodeListener == null) {
                        return@setPreviewCallback
                    } else {
                        onBarcodeListener!!.onBarcodeRead(result)
                        decoding = false
                    }
                }
            }

            override fun onPreviewStarted(camera: Camera) {}
            override fun onCameraStopping(camera: Camera) {
                camera.setPreviewCallback(null)
            }
        })
    }
}