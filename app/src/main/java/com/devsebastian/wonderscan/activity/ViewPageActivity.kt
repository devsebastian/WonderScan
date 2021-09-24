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
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.WonderScanApp
import com.devsebastian.wonderscan.adapter.ViewFrameAdapter
import com.devsebastian.wonderscan.data.Frame
import com.devsebastian.wonderscan.databinding.ActivityViewFramesBinding
import com.devsebastian.wonderscan.viewmodel.ViewPageActivityViewModel
import com.devsebastian.wonderscan.viewmodel.ViewPageActivityViewModelFactory
import com.google.android.material.navigation.NavigationBarView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ViewPageActivity : BaseActivity(), NavigationBarView.OnItemSelectedListener,
    ViewPager.OnPageChangeListener {
    private var docId: String? = null
    private lateinit var viewFrameAdapter: ViewFrameAdapter
    private lateinit var viewModel: ViewPageActivityViewModel
    private lateinit var binding: ActivityViewFramesBinding

    private fun initialiseViewModel(docId: String) {
        (application as WonderScanApp).database?.let { db ->
            viewModel = ViewModelProvider(
                this,
                ViewPageActivityViewModelFactory(db.documentDao(), db.frameDao(), docId)
            ).get(ViewPageActivityViewModel::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFramesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = ""
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        docId = intent.getStringExtra(getString(R.string.intent_document_id))
        if (docId == null) {
            Toast.makeText(this, getString(R.string.toast_error_message), Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        initialiseViewModel(docId!!)

        viewFrameAdapter = ViewFrameAdapter(this, ArrayList())
        viewModel.currentIndex = intent.getIntExtra(
            getString(R.string.intent_frame_position),
            0
        )
        binding.let {
            it.bottomNavigationView.setOnItemSelectedListener(this)
            it.viewPager.adapter = viewFrameAdapter
            it.viewPager.addOnPageChangeListener(this)
            viewModel.frames.observe(this) { frames ->
                viewFrameAdapter.setFrames(frames)
                viewFrameAdapter.notifyDataSetChanged()
                it.viewPager.currentItem = viewModel.currentIndex
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_modify -> {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra(getString(R.string.intent_document_id), docId)
                intent.putExtra(
                    getString(R.string.intent_frame_id),
                    viewFrameAdapter.get(getCurrentIndex()).id
                )
                startActivity(intent)
            }
            R.id.menu_note -> {
                showNoteDialog(
                    "Note",
                    "Write something beautiful here! This note will be saved alongside the scanned copy",
                    viewFrameAdapter.get(getCurrentIndex()).note,
                    viewFrameAdapter.get(getCurrentIndex())
                )
            }
            R.id.menu_crop -> {
                val cropIntent = Intent(this, CropActivity::class.java)
                cropIntent.putExtra(
                    getString(R.string.intent_source_path),
                    viewFrameAdapter.get(getCurrentIndex()).uri
                )
                cropIntent.putExtra(
                    getString(R.string.intent_cropped_path),
                    viewFrameAdapter.get(getCurrentIndex()).croppedUri
                )
                cropIntent.putExtra(
                    getString(R.string.intent_frame_position),
                    getCurrentIndex()
                )
                cropIntent.putExtra(
                    getString(R.string.intent_angle),
                    viewFrameAdapter.get(getCurrentIndex()).angle
                )
                cropResultLauncher.launch(cropIntent)
            }
            R.id.menu_ocr -> {
                Toast.makeText(this, "Detecting Text. Please wait", Toast.LENGTH_SHORT).show()
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val bitmap =
                    BitmapFactory.decodeFile(viewFrameAdapter.get(getCurrentIndex()).editedUri)
                recognizer.process(InputImage.fromBitmap(bitmap, 0))
                    .addOnSuccessListener { text: Text ->
                        showNoteDialog(
                            "Detected Text",
                            "",
                            text.text,
                            viewFrameAdapter.get(getCurrentIndex())
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

    var cropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val frame = viewFrameAdapter.get(getCurrentIndex()).apply { editedUri = null }
                viewModel.updateFrame(frame)
            }
        }

    private fun getCurrentIndex(): Int {
        return binding.viewPager.currentItem
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
                showFrameRenameDialog(this, viewFrameAdapter.get(getCurrentIndex()))
            }
            R.id.menu_delete -> {
                showFrameDeleteDialog(this, viewFrameAdapter.get(getCurrentIndex()))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFrameRenameDialog(activity: Activity, frame: Frame) {
        val frameLayout = FrameLayout(activity)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(50, 12, 50, 12)
        }
        val editText = EditText(activity).apply {
            layoutParams = params
            hint = "Frame Name"
            setText(frame.name)
        }
        frameLayout.addView(editText)
        AlertDialog.Builder(activity).apply {
            setTitle("Rename")
            setView(frameLayout)
            setNegativeButton("Cancel", null)
            setPositiveButton("Save") { _: DialogInterface?, _: Int ->
                frame.name = editText.text.toString()
                viewModel.updateFrame(frame)
            }
            create().show()
        }
    }

    private fun showFrameDeleteDialog(activity: Activity?, frame: Frame) {
        AlertDialog.Builder(activity).apply {
            setTitle("Confirm Delete")
            setMessage("Are you sure you want to delete this frame? You won't be able to recover this frame later")
            setNegativeButton("Cancel", null)
            setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFrame(frame)
            }
            create().show()
        }
    }

    @SuppressLint("InflateParams")
    private fun showNoteDialog(name: String?, hint: String?, note: String?, frame: Frame) {
        layoutInflater.inflate(R.layout.dialog_note, null).apply {
            val alertDialog = AlertDialog.Builder(this@ViewPageActivity)
                .setView(this)
                .create().apply {
                    window?.setBackgroundDrawableResource(android.R.color.transparent)
                    show()
                }
            val etNote = findViewById<EditText>(R.id.et_note).apply {
                this.hint = hint
                setText(note)
            }
            findViewById<TextView>(R.id.title).text = name
            findViewById<TextView>(R.id.tv_save).setOnClickListener {
                frame.note = etNote.text.toString()
                viewModel.updateFrame(frame)
                alertDialog.dismiss()
            }
            findViewById<TextView>(R.id.tv_cancel).setOnClickListener { alertDialog.dismiss() }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // required
    }

    override fun onPageSelected(position: Int) {
        viewModel.currentIndex = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        // required
    }
}