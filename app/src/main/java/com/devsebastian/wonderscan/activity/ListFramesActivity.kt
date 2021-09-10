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

import android.content.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.devsebastian.wonderscan.*
import com.devsebastian.wonderscan.adapter.ProgressFramesAdapter
import com.devsebastian.wonderscan.utils.ExportPdf
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.utils.Filter
import com.devsebastian.wonderscan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

class ListFramesActivity : BaseActivity() {
    private var docId: Long = 0
    private var pdfDocument: PdfDocument? = null
    private lateinit var dbHelper: DBHelper
    private lateinit var document: Document
    private var frames: MutableList<Frame> = ArrayList()
    private lateinit var framesAdapter: ProgressFramesAdapter

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_frames, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_export_pdf -> {
                lifecycleScope.launch(Dispatchers.Default) {
                    pdfDocument = ExportPdf.exportPdf(dbHelper, frames)
                    Utils.sendCreateFileIntent(
                        this@ListFramesActivity,
                        this@ListFramesActivity.document.name!!,
                        "application/pdf",
                        SAVE_PDF_INTENT_CODE
                    )
                }
            }
            R.id.menu_delete -> {
                Utils.showConfirmDeleteDialog(this, docId)
            }
            R.id.menu_rename -> {
                Utils.showDocumentRenameDialog(this, docId, document.name)
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun processFrame(context: Context, frameId: Long) {
        val file = Utils.createPhotoFile(context)
        val mat = Imgcodecs.imread(dbHelper.getCroppedPath(frameId))
        val editedMat = Filter.magicColor(mat)
        Imgcodecs.imwrite(file.absolutePath, editedMat)
        dbHelper.updateEditedPath(frameId, file.absolutePath)
        mat.release()
        editedMat.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        setContentView(R.layout.activity_list_frames)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        }
        dbHelper = DBHelper(this)
        val intent = intent
        if (intent != null) {
            docId = intent.getLongExtra(getString(R.string.intent_document_id), -1)
            if (docId == -1L) {
                Toast.makeText(this, getString(R.string.toast_error_message), Toast.LENGTH_SHORT)
                    .show()
                finish()
                return
            }
            document = dbHelper.getDocument(docId)
            frames = dbHelper.getAllFrames(docId)
            (findViewById<View?>(R.id.toolbar_title) as TextView).text =
                dbHelper.getDocumentName(docId)
        }
        framesAdapter = ProgressFramesAdapter(this, docId, frames)
        val itemTouchHelper = ItemTouchHelper(getItemTouchHelperCallback())
        processUnprocessedFrames()
        val fab = findViewById<View?>(R.id.fab)
        val recyclerView = findViewById<RecyclerView?>(R.id.rv_frames)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = framesAdapter
        fab.setOnClickListener {
            val i = Intent(this@ListFramesActivity, ScanActivity::class.java)
            i.putExtra(getString(R.string.intent_document_id), docId)
            startActivity(i)
            finish()
        }
        itemTouchHelper.attachToRecyclerView(recyclerView)
        Toast.makeText(
            this,
            "Hint: Re-order pages by long pressing a page and dragging it to the appropriate position",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getItemTouchHelperCallback(): ItemTouchHelper.Callback {
        return object : ItemTouchHelper.Callback() {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                framesAdapter.swap(from, to)
                dbHelper.swapFrames(
                    frames[from].id,
                    from.toLong(),
                    frames[to].id,
                    to.toLong()
                )
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            //defines the enabled move directions in each state (idle, swiping, dragging).
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END
                )
            }
        }
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
        } else if (requestCode == VIEW_PAGE_ACTIVITY) {
            processUnprocessedFrames()
        }
    }

    private fun processUnprocessedFrames() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(min(9, frames.size))
        for (i in frames.indices) {
            val frameId = frames[i].id
            if (dbHelper.getEditedPath(frameId) == null) {
                executorService.submit {
                    val frame = frames[i]
                    processFrame(this, frame.id)
                    runOnUiThread { framesAdapter.notifyItemChanged(i) }
                }
            }
        }
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    companion object {
        const val VIEW_PAGE_ACTIVITY = 101
        const val SAVE_PDF_INTENT_CODE = 102
    }
}