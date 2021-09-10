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
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.devsebastian.wonderscan.*
import com.devsebastian.wonderscan.utils.BrightnessAndContrastController
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.utils.Filter
import com.devsebastian.wonderscan.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jsibbold.zoomage.ZoomageView
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EditActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener, OnSeekBarChangeListener {
    private var processImageHandler: Handler? = null
    private var processImageRunnable: Runnable? = null
    private lateinit var croppedMat: Mat
    private lateinit var editedMat: Mat
    private lateinit var mainImageView: ZoomageView
    private lateinit var modifyToolsLayout: LinearLayout
    private lateinit var tvBrightness: TextView
    private lateinit var tvContrast: TextView
    private var modifyToolsIsVisible = false
    private var currentActiveId = R.id.iv_original_image
    private var editedPath: String? = null
    private var croppedPath: String? = null
    private var executorService: ExecutorService = Executors.newFixedThreadPool(5)
    private lateinit var brightnessAndContrastController: BrightnessAndContrastController

    private fun setupPreview() {
        croppedMat = Utils.readMat(croppedPath)
        if (editedPath != null) {
            editedMat = Utils.readMat(editedPath)
        } else {
            editedMat = Mat()
            croppedMat.copyTo(editedMat)
        }
        previewMat(editedMat)
        findViewById<View?>(R.id.pb_edit).visibility = View.GONE
    }

    private fun filterImageButton(resourceId: Int, processImage: ProcessImage) {
        executorService.submit {
            var mat = Mat()
            val height: Double = croppedMat.height().toDouble()
            val width: Double = croppedMat.width().toDouble()
            Imgproc.resize(croppedMat, mat, Size(width, height))
            mat = processImage.process(mat)
            val bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
            org.opencv.android.Utils.matToBitmap(mat, bmp)
            runOnUiThread { (findViewById<View?>(resourceId) as ImageView).setImageBitmap(bmp) }
        }
    }

    private fun filterImage(processImage: ProcessImage) {
        findViewById<View?>(R.id.pb_edit).visibility = View.VISIBLE
        if (processImageHandler != null && processImageRunnable != null) {
            processImageHandler!!.removeCallbacks(processImageRunnable!!)
            processImageHandler = null
        }
        processImageHandler = Handler()
        processImageRunnable = Runnable {
            editedMat = processImage.process(croppedMat)
            previewMat(editedMat)
            runOnUiThread { findViewById<View?>(R.id.pb_edit).visibility = View.GONE }
        }
        processImageHandler!!.post(processImageRunnable!!)
    }

    private fun setupFilterButtons() {
        filterImageButton(R.id.iv_original_image, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return croppedMat
            }
        })
        filterImageButton(
            R.id.iv_black_and_white, object : ProcessImage {
                override fun process(mat: Mat): Mat {
                    return Filter.thresholdOTSU(mat)
                }
            })
        filterImageButton(R.id.iv_auto, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.auto(mat)
            }
        })
        filterImageButton(R.id.iv_grayscale, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.grayscale(mat)
            }
        })
        filterImageButton(R.id.iv_magic, object : ProcessImage {
            override fun process(mat: Mat): Mat {
                return Filter.magicColor(mat)
            }
        })
    }

    private fun setActive(activeId: Int) {
        findViewById<View?>(currentActiveId).alpha = 0.6f
        findViewById<View?>(currentActiveId).setPadding(12, 12, 12, 12)
        currentActiveId = activeId
        findViewById<View?>(currentActiveId).alpha = 1f
        findViewById<View?>(currentActiveId).setPadding(0, 0, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        val dbHelper = DBHelper(this)
        setActive(R.id.iv_auto)
        val frameId = intent.getLongExtra(getString(R.string.intent_frame_id), -1)
        if (frameId == -1L) {
            Toast.makeText(this, getString(R.string.toast_error_message), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        editedPath = dbHelper.getEditedPath(frameId)
        croppedPath = dbHelper.getCroppedPath(frameId)
        mainImageView = findViewById(R.id.iv_edit)
        modifyToolsLayout = findViewById(R.id.ll_modify_tools)
        tvBrightness = findViewById(R.id.tv_brightness)
        tvContrast = findViewById(R.id.tv_contrast)
        val bottomNavigationView = findViewById<BottomNavigationView?>(R.id.bottom_navigation_view)
        modifyToolsLayout.visibility = View.GONE
        setupPreview()
        setupFilterButtons()
        setupBrightnessAndContrast()
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        findViewById<View?>(R.id.iv_black_and_white).setOnClickListener(this)
        findViewById<View?>(R.id.iv_auto).setOnClickListener(this)
        findViewById<View?>(R.id.iv_grayscale).setOnClickListener(this)
        findViewById<View?>(R.id.iv_magic).setOnClickListener(this)
        findViewById<View?>(R.id.iv_original_image).setOnClickListener(this)
    }

    private fun setupBrightnessAndContrast() {
        brightnessAndContrastController = BrightnessAndContrastController(0.0, 1.0)
        val sbContrast = findViewById<SeekBar?>(R.id.sb_contrast)
        val sbBrightness = findViewById<SeekBar?>(R.id.sb_brightness)
        sbBrightness.max = 200
        sbBrightness.progress = 100
        sbBrightness.setOnSeekBarChangeListener(this)
        sbContrast.max = 200
        sbContrast.progress = 100
        sbContrast.setOnSeekBarChangeListener(this)
    }

    private fun previewMat(mat: Mat) {
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(mat, bitmap)
        runOnUiThread { mainImageView.setImageBitmap(bitmap) }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                findViewById<View?>(R.id.pb_edit).visibility = View.VISIBLE
                executorService.submit {
                    Utils.saveMat(editedMat, editedPath)
                    Utils.saveMat(croppedMat, croppedPath)
                    val resultIntent = Intent()
                    resultIntent.putExtra(
                        getString(R.string.intent_frame_position),
                        intent.getIntExtra(getString(R.string.intent_frame_position), 0)
                    )
                    setResult(RESULT_OK, resultIntent)
                    editedMat.release()
                    croppedMat.release()
                    runOnUiThread { finish() }
                }
            }
            R.id.menu_rotate_left -> {
                rotateLeft()
            }
            R.id.menu_retake -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            R.id.menu_modify -> {
                modifyToolsIsVisible = !modifyToolsIsVisible
                modifyToolsLayout.visibility = if (modifyToolsIsVisible) View.VISIBLE else View.GONE
            }
        }
        return false
    }

    private fun rotateLeft() {
        Core.rotate(croppedMat, croppedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
        Core.rotate(editedMat, editedMat, Core.ROTATE_90_COUNTERCLOCKWISE)
        previewMat(editedMat)
    }

    override fun onClick(view: View) {
        setActive(view.id)
        when (view.id) {
            R.id.iv_black_and_white -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.thresholdOTSU(mat)
                    }
                })
            }
            R.id.iv_auto -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.auto(mat)
                    }
                })
            }
            R.id.iv_grayscale -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.grayscale(mat)
                    }
                })
            }
            R.id.iv_magic -> {
                filterImage(object : ProcessImage {
                    override fun process(mat: Mat): Mat {
                        return Filter.magicColor(mat)
                    }
                })
            }
            R.id.iv_original_image -> {
                editedMat = croppedMat.clone()
                previewMat(editedMat)
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        val mat: Mat = when (seekBar.id) {
            R.id.sb_contrast -> {
                tvContrast.text = String.format(
                    Locale.getDefault(),
                    "Contrast • %d%%",
                    i - 100
                )
                brightnessAndContrastController.setContrast(editedMat.clone(), i / 100.0)
            }
            R.id.sb_brightness -> {
                tvBrightness.text = String.format(
                    Locale.getDefault(),
                    "Brightness • %d%%",
                    i - 100
                )
                brightnessAndContrastController.setBrightness(
                    editedMat.clone(),
                    (i - 100).toDouble()
                )
            }
            else -> {
                Mat()
            }
        }
        previewMat(mat)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    internal interface ProcessImage {
        fun process(mat: Mat): Mat
    }
}