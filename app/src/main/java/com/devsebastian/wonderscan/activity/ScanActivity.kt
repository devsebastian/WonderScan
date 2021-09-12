/*
 * Copyright (C) 2021 Dev Sebastian
 * This file is part of WonderScan <https://github.com/devsebastian/WonderScan>.
 *
 * WonderScan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WonderScan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WonderScan.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.devsebastian.wonderscan.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devsebastian.wonderscan.MyApplication
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.utils.DetectBox
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.view.ScanView
import com.devsebastian.wonderscan.viewmodel.ScanActivityViewModel
import com.devsebastian.wonderscan.viewmodel.ScanActivityViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ScanActivity : BaseActivity() {
    private val requestCodePermissions = 1001
    private val requiredPermissions: Array<String> =
        arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var angle = 0
    private lateinit var scanView: ScanView
    private lateinit var viewFinder: PreviewView
    private lateinit var captureProgress: ProgressBar
    private lateinit var finalImage: ImageView
    private lateinit var pageCount: TextView
    private var executor: Executor = Executors.newSingleThreadExecutor()
    private lateinit var viewModel: ScanActivityViewModel
    private var count = 0

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        (application as MyApplication).database?.let { db ->
            viewModel = ViewModelProvider(
                this,
                ScanActivityViewModelFactory(db.documentDao(), db.frameDao())
            ).get(ScanActivityViewModel::class.java)
        }
        setContentView(R.layout.activity_scan)
        captureProgress = findViewById(R.id.pb_scan)
        finalImage = findViewById(R.id.iv_recent_capture)
        val frameLayout = findViewById<FrameLayout?>(R.id.camera_frame)
        viewFinder = findViewById(R.id.viewFinder)
        scanView = findViewById(R.id.scan_view)
        pageCount = findViewById(R.id.page_count)
        val intent = intent
        if (intent != null) {
            val docId = intent.getStringExtra(getString(R.string.intent_document_id))
            if (docId != null) {
                viewModel.getPageCount(docId).observe(this) { count -> this.count = count }
            }
        }
        val width = Utils.getDeviceWidth()
        val height = (width * (4 / 3f)).toInt()
        frameLayout.layoutParams = LinearLayout.LayoutParams(width, height)
        finalImage.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
            val name = getString(R.string.app_name) + " " + simpleDateFormat.format(Date())
            val job = viewModel.capture(name, angle, count)
            job.invokeOnCompletion {
                val i = Intent(this@ScanActivity, ListFramesActivity::class.java)
                i.putExtra(getString(R.string.intent_document_id), viewModel.docId)
                startActivity(i)
                finish()
            }
        }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, requestCodePermissions)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    @ExperimentalGetImage
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermissions) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    @ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CROP_ACTIVITY && resultCode == RESULT_OK && data != null) {
            val croppedPath = data.getStringExtra(getString(R.string.intent_cropped_path))
            val sourcePath = data.getStringExtra(getString(R.string.intent_source_path))
            viewModel.addPath(sourcePath!!, croppedPath!!)
            finalImage.setImageBitmap(BitmapFactory.decodeFile(croppedPath))
            if (pageCount.visibility != View.VISIBLE) pageCount.visibility = View.VISIBLE
            pageCount.text = viewModel.pathsCount().toString()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @ExperimentalGetImage
    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val builder = ImageCapture.Builder()
        val imageCapture = builder
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .build()
        preview.setSurfaceProvider(viewFinder.surfaceProvider)
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)
        val captureImageBtn = findViewById<View?>(R.id.btn_capture)
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), { imageProxy: ImageProxy ->
            angle = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image
            if (image != null && image.format == ImageFormat.YUV_420_888) {
                lifecycleScope.launch(Dispatchers.Default) {
                    val boundingRect = DetectBox.findCorners(imageProxy, angle)
                    imageProxy.close()
                    runOnUiThread {
                        scanView.setBoundingRect(boundingRect)
                    }
                }
            }
        })

        captureImageBtn.setOnClickListener {
            captureProgress.visibility = View.VISIBLE
            val file = Utils.createPhotoFile(this)
            executor = ContextCompat.getMainExecutor(this)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            imageCapture.takePicture(
                outputFileOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val intent = Intent(this@ScanActivity, CropActivity::class.java)
                        intent.putExtra(getString(R.string.intent_source_path), file.absolutePath)
                        intent.putExtra(getString(R.string.intent_angle), angle)
                        startActivityForResult(intent, CROP_ACTIVITY)
                        captureProgress.visibility = View.GONE
                    }

                    override fun onError(error: ImageCaptureException) {
                        Log.e(TAG, Log.getStackTraceString(error))
                    }
                })
        }
    }

    companion object {
        val TAG: String = ScanActivity::class.java.simpleName
        private const val CROP_ACTIVITY = 101
    }
}