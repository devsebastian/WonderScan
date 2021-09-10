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

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Pair
import android.view.*
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devsebastian.wonderscan.*
import com.devsebastian.wonderscan.adapter.ProgressFramesAdapter
import com.devsebastian.wonderscan.utils.ExportPdf
import com.devsebastian.wonderscan.data.BoundingRect
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.utils.Filter
import com.devsebastian.wonderscan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.min

class CropAndListFramesActivity : BaseActivity() {
    private val simpleDateFormat: SimpleDateFormat =
        SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault())
    var pdfDocument: PdfDocument? = null
    private var docId: Long = 0
    private var docName: String =
        getString(R.string.app_name) + " " + simpleDateFormat.format(Date())

    private var dbHelper: DBHelper = DBHelper(this)
    private lateinit var framesAdapter: ProgressFramesAdapter
    private var sourcePaths: MutableList<String> = ArrayList()
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_frames, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_export_pdf -> {
                lifecycleScope.launch(Dispatchers.Default) {
                    pdfDocument = ExportPdf.exportPdf(dbHelper, dbHelper.getAllFrames(docId))
                    Utils.sendCreateFileIntent(
                        this@CropAndListFramesActivity,
                        docName,
                        "application/pdf",
                        SAVE_PDF_INTENT_CODE
                    )
                }

            }
            R.id.menu_delete -> {
                Utils.showConfirmDeleteDialog(this, docId)
            }
            R.id.menu_rename -> {
                Utils.showDocumentRenameDialog(this, docId, docName)
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SAVE_PDF_INTENT_CODE && resultCode == RESULT_OK && data != null) {
            data.data?.let { uri ->
                try {
                    pdfDocument?.writeTo(contentResolver.openOutputStream(uri))
                    Toast.makeText(this, "PDF document saved in " + uri.path, Toast.LENGTH_SHORT)
                        .show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        setContentView(R.layout.activity_list_frames)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        }
        val recyclerView = findViewById<RecyclerView?>(R.id.rv_frames)
        dbHelper = DBHelper(this)
        val intent = intent
        if (intent != null) {
            sourcePaths =
                intent.getStringArrayListExtra(getString(R.string.intent_uris)) ?: ArrayList()
        }
        val fab = findViewById<View?>(R.id.fab)
        fab.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)
        docId = dbHelper.insertDocument(docName)
        framesAdapter = ProgressFramesAdapter(this, docId, ArrayList())
        recyclerView.adapter = framesAdapter
        val executorService = Executors.newFixedThreadPool(min(9, sourcePaths.size))
        executorService.submit {
            val frames = getFramesFromImagePaths(sourcePaths)
            framesAdapter.setFrames(frames)
            runOnUiThread { framesAdapter.notifyDataSetChanged() }
            for (i in sourcePaths.indices) {
                executorService.submit {
                    cropAndFormat(
                        sourcePaths[i],
                        frames[i].id,
                        i
                    )
                }
            }
            if (!executorService.isShutdown) executorService.shutdown()
        }
    }

    private fun getFramesFromImagePaths(paths: MutableList<String>): MutableList<Frame> {
        val frames = ArrayList<Frame>()
        for (i in paths.indices) {
            val sourcePath = paths[i]
            val frame = Frame()
            frame.timeInMillis = System.currentTimeMillis()
            frame.index = i.toLong()
            val frameId = dbHelper.insertFrame(docId, frame)
            frame.id = frameId
            dbHelper.updateSourcePath(frameId, sourcePath)
            frames.add(frame)
        }
        return frames
    }

    private fun cropAndFormat(path: String?, frameId: Long, index: Int) {
        val originalMat = Imgcodecs.imread(path)
        val ratio = Utils.getDeviceWidth() / originalMat.width().toDouble()
        val bRect = findCorners(originalMat, ratio)
        val croppedMat: Mat
        if (bRect != null) {
            croppedMat = CropActivity.getPerspectiveTransform(originalMat, bRect, ratio)
        } else {
            croppedMat = Mat()
            originalMat.copyTo(croppedMat)
        }
        val croppedPath = Utils.createPhotoFile(this).absolutePath
        Imgcodecs.imwrite(croppedPath, croppedMat)
        dbHelper.updateCroppedPath(frameId, croppedPath)
        val editedMat = Filter.auto(croppedMat)
        val editedPath = Utils.createPhotoFile(this).absolutePath
        Imgcodecs.imwrite(editedPath, editedMat)
        dbHelper.updateEditedPath(frameId, editedPath)
        runOnUiThread { framesAdapter.notifyItemChanged(index) }
        originalMat.release()
        croppedMat.release()
        editedMat.release()
    }

    private fun findCorners(sourceMat: Mat, ratio: Double): BoundingRect? {
        val mat = sourceMat.clone()
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(mat, mat, 75.0, 200.0)
        val points: MutableList<MatOfPoint?> = ArrayList()
        Imgproc.findContours(mat, points, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        val areas: MutableList<Pair<MatOfPoint, Double>> = ArrayList()
        for (point in points) {
            areas.add(Pair(point, Imgproc.contourArea(point)))
        }
        areas.sortWith { t1: Pair<MatOfPoint, Double>, t2: Pair<MatOfPoint, Double> ->
            java.lang.Double.compare(
                t2.second,
                t1.second
            )
        }
        val maxArea = (mat.width() * (mat.height() / 8f)).toDouble()
        if (areas.size == 0 || areas[0].second < maxArea) {
            return null
        }
        for (area in areas) {
            val matOfPoint2f = MatOfPoint2f(*area.first.toArray())
            Imgproc.approxPolyDP(
                matOfPoint2f,
                matOfPoint2f,
                0.02 * Imgproc.arcLength(matOfPoint2f, true),
                true
            )
            if (matOfPoint2f.height() == 4) {
                if (area.second > maxArea) {
                    val bRect = BoundingRect()
                    bRect.fromPoints(matOfPoint2f.toList(), ratio, ratio)
                    mat.release()
                    return bRect
                }
            }
        }
        mat.release()
        return null
    }

    companion object {
        private const val SAVE_PDF_INTENT_CODE = 101
    }
}