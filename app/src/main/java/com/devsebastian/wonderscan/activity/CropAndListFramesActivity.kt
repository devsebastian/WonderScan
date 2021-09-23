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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.devsebastian.wonderscan.WonderScanApp
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.adapter.ProgressFramesAdapter
import com.devsebastian.wonderscan.databinding.ActivityListFramesBinding
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.viewmodel.CropAndListFramesActivityViewModel
import com.devsebastian.wonderscan.viewmodel.CropAndListFramesActivityViewModelFactory

class CropAndListFramesActivity : BaseActivity() {
    private var sourcePaths: MutableList<String> = ArrayList()

    private lateinit var framesAdapter: ProgressFramesAdapter
    private lateinit var viewModel: CropAndListFramesActivityViewModel

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_frames, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_export_pdf -> {
                Utils.sendCreateFileIntent(
                    viewModel.document.name!!,
                    "application/pdf",
                    resultLauncher
                )
            }
            R.id.menu_delete -> {
                viewModel.showConfirmDeleteDialog(this)
            }
            R.id.menu_rename -> {
                viewModel.showRenameDialog()
            }
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.exportPdf(uri)
                }
            }
        }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityListFramesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = ""
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
        }
        sourcePaths = intent.getStringArrayListExtra(getString(R.string.intent_uris)) ?: ArrayList()
        binding.fab.visibility = View.GONE

        framesAdapter = ProgressFramesAdapter(this, viewModel.document.id, ArrayList())
        binding.rvFrames.let {
            it.layoutManager = GridLayoutManager(this, 2)
            it.setHasFixedSize(true)
            it.adapter = framesAdapter
        }

        initialiseViewModel()
        viewModel.let {
            it.setup(sourcePaths)
            it.frames.observe(this) { frames ->
                framesAdapter.frames = frames
                framesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initialiseViewModel() {
        (application as WonderScanApp).database?.let { db ->
            viewModel = ViewModelProvider(
                this,
                CropAndListFramesActivityViewModelFactory(
                    application as WonderScanApp,
                    db.documentDao(),
                    db.frameDao()
                )
            ).get(CropAndListFramesActivityViewModel::class.java)
        }
    }
}