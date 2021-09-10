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

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.adapter.ViewFrameAdapter
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.view.CustomViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ViewPageActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var docId: Long = 0
    private lateinit var frames: MutableList<Frame>
    private lateinit var viewPager: CustomViewPager
    private lateinit var viewFrameAdapter: ViewFrameAdapter
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        setContentView(R.layout.activity_view_frames)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            frames = dbHelper.getAllFrames(docId)
            viewPager = findViewById(R.id.view_pager)
            viewFrameAdapter = ViewFrameAdapter(this, frames)
            viewPager.adapter = viewFrameAdapter
            viewPager.currentItem = intent.getIntExtra(
                getString(R.string.intent_frame_position),
                0
            )
        }
        val bottomNavigationView = findViewById<BottomNavigationView?>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val frame = frames[viewPager.currentItem]
        when (item.itemId) {
            R.id.menu_modify -> {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra(getString(R.string.intent_document_id), docId)
                intent.putExtra(
                    getString(R.string.intent_frame_id),
                    frames[viewPager.currentItem].id
                )
                startActivityForResult(intent, MODIFY_FRAME)
            }
            R.id.menu_note -> {
                showNoteDialog(
                    "Note",
                    "Write something beautiful here! This note will be saved alongside the scanned copy",
                    null
                )
            }
            R.id.menu_crop -> {
                val cropIntent = Intent(this, CropActivity::class.java)
                cropIntent.putExtra(
                    getString(R.string.intent_source_path),
                    dbHelper.getSourcePath(frame.id)
                )
                cropIntent.putExtra(
                    getString(R.string.intent_cropped_path),
                    dbHelper.getCroppedPath(frame.id)
                )
                cropIntent.putExtra(
                    getString(R.string.intent_frame_position),
                    viewPager.currentItem
                )
                cropIntent.putExtra(getString(R.string.intent_angle), frame.angle)
                startActivityForResult(cropIntent, CROP_ACTIVITY)
            }
            R.id.menu_ocr -> {
                Toast.makeText(this, "Detecting Text. Please wait", Toast.LENGTH_SHORT).show()
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val bitmap = BitmapFactory.decodeFile(dbHelper.getEditedPath(frame.id))
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { text: Text ->
                        showNoteDialog(
                            "Detected Text",
                            "",
                            text.text
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "ERROR: Could not detect text",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view_frames, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_rename -> {
                showFrameRenameDialog(this, docId, viewPager.currentItem)
            }
            R.id.menu_delete -> {
                showFrameDeleteDialog(this, docId, viewPager.currentItem.toLong())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFrameRenameDialog(activity: Activity, docId: Long, framePos: Int) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Rename")
        val frame = frames[framePos]
        val frameLayout = FrameLayout(activity)
        val editText = EditText(activity)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(50, 12, 50, 12)
        editText.layoutParams = layoutParams
        editText.setText(frame.name)
        editText.hint = "Frame Name"
        frameLayout.addView(editText)
        builder.setView(frameLayout)
        builder.setNegativeButton("Cancel", null)
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int ->
            dbHelper.renameFrame(
                docId,
                framePos.toLong(),
                editText.text.toString()
            )
        }
        builder.create().show()
    }

    private fun showFrameDeleteDialog(activity: Activity?, docId: Long, frameIndex: Long) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this frame? You won't be able to recover this frame later")
        builder.setNegativeButton("Cancel", null)
        builder.setPositiveButton("Delete") { _, _ ->
            dbHelper.deleteFrame(
                docId,
                frameIndex
            )
        }
        builder.create().show()
    }

    private fun showNoteDialog(name: String?, hint: String?, note: String?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_note, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
        val framePos = viewPager.currentItem
        val etNote: EditText = view.findViewById(R.id.et_note)
        etNote.hint = hint
        val cancelBtn: TextView = view.findViewById(R.id.tv_cancel)
        val saveBtn: TextView = view.findViewById(R.id.tv_save)
        val title: TextView = view.findViewById(R.id.title)
        title.text = name
        etNote.setText(note ?: frames[framePos].note)
        saveBtn.setOnClickListener {
            val dbHelper = DBHelper(this@ViewPageActivity)
            dbHelper.addNote(docId, framePos.toLong(), etNote.text.toString())
            alertDialog.dismiss()
        }
        cancelBtn.setOnClickListener { alertDialog.dismiss() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            var index = 0
            if (data != null) index = data.getIntExtra(getString(R.string.intent_frame_position), 0)
            if (requestCode == CROP_ACTIVITY) {
                dbHelper.updateEditedPath(docId, index.toLong(), null)
            }
            viewPager.adapter = viewFrameAdapter
            viewPager.currentItem = index
        }
    }

    companion object {
        const val MODIFY_FRAME = 101
        const val CROP_ACTIVITY = 102
    }
}