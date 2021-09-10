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
import android.util.Pair
import android.view.*
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.utils.DetectBox
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.view.ScanView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ScanActivity : BaseActivity() {
    private val requestCodePermissions = 1001
    private val requiredPermissions: Array<String> =
        arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var docId: Long = 0
    private var angle = 0
    private lateinit var scanView: ScanView
    private lateinit var viewFinder: PreviewView
    private lateinit var captureProgress: ProgressBar
    private lateinit var finalImage: ImageView
    private lateinit var pageCount: TextView
    private lateinit var paths: MutableList<Pair<String, String>>
    private var executor: Executor = Executors.newSingleThreadExecutor()
    private lateinit var dbHelper: DBHelper

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_scan)
        captureProgress = findViewById(R.id.pb_scan)
        finalImage = findViewById(R.id.iv_recent_capture)
        val frameLayout = findViewById<FrameLayout?>(R.id.camera_frame)
        viewFinder = findViewById(R.id.viewFinder)
        scanView = findViewById(R.id.scan_view)
        pageCount = findViewById(R.id.page_count)
        paths = ArrayList()
        val intent = intent
        if (intent != null) {
            docId = intent.getLongExtra(getString(R.string.intent_document_id), -1)
        }
        val width = Utils.getDeviceWidth()
        val height = (width * (4 / 3f)).toInt()
        frameLayout.layoutParams = LinearLayout.LayoutParams(width, height)
        dbHelper = DBHelper(this)
        finalImage.setOnClickListener {
            var count: Long = 0
            if (docId == -1L) {
                val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
                docId = dbHelper.insertDocument(
                    getString(R.string.app_name) + " " + simpleDateFormat.format(Date())
                )
            } else {
                count = dbHelper.getPageCount(docId)
            }
            for (i in paths.indices) {
                val path = paths[i]
                val frame = Frame()
                frame.timeInMillis = System.currentTimeMillis()
                frame.index = count + i
                frame.angle = angle
                val frameId = dbHelper.insertFrame(docId, frame)
                dbHelper.updateSourcePath(frameId, path.first)
                dbHelper.updateCroppedPath(frameId, path.second)
            }
            val i = Intent(this@ScanActivity, ListFramesActivity::class.java)
            i.putExtra(getString(R.string.intent_document_id), docId)
            startActivity(i)
            finish()
        }
        if (allPermissionsGranted()) {
            startCamera() //start camera if permission has been granted by user
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
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CROP_ACTIVITY && resultCode == RESULT_OK && data != null) {
            val croppedPath = data.getStringExtra(getString(R.string.intent_cropped_path))
            val sourcePath = data.getStringExtra(getString(R.string.intent_source_path))
            paths.add(Pair(sourcePath, croppedPath))
            finalImage.setImageBitmap(BitmapFactory.decodeFile(croppedPath))
            if (pageCount.visibility != View.VISIBLE) pageCount.visibility = View.VISIBLE
            pageCount.text = paths.size.toString()
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
                    scanView.setBoundingRect(DetectBox.findCorners(imageProxy, angle))
                    imageProxy.close()
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
                        Log.d(TAG, Log.getStackTraceString(error))
                    }
                })
        }
    }

    companion object {
        val TAG: String = ScanActivity::class.java.simpleName
        private const val CROP_ACTIVITY = 101
    }
}