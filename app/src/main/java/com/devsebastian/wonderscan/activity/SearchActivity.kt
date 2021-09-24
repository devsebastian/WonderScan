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

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.devsebastian.wonderscan.WonderScanApp
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.adapter.DocumentsAdapter
import com.devsebastian.wonderscan.databinding.ActivitySearchBinding
import com.devsebastian.wonderscan.viewmodel.SearchActivityViewModel
import com.devsebastian.wonderscan.viewmodel.SearchActivityViewModelFactory

class SearchActivity : BaseActivity() {
    private lateinit var documentsAdapter: DocumentsAdapter
    lateinit var viewModel: SearchActivityViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initialiseViewModel() {
        (application as WonderScanApp).database?.let { db ->
            viewModel = ViewModelProvider(
                this,
                SearchActivityViewModelFactory(db.documentDao(), db.frameDao())
            ).get(
                SearchActivityViewModel::class.java
            )
        }
    }

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        initialiseViewModel()
        documentsAdapter = DocumentsAdapter(this, ArrayList(), viewModel)

        binding.recyclerView.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = documentsAdapter
        }

        viewModel.documents.observe(this) { documents ->
            documentsAdapter.updateDocuments(documents)
        }

        binding.etSearch.let {
            it.requestFocus()
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                    viewModel.search(charSequence.toString())
                }

                override fun afterTextChanged(editable: Editable?) {}
            })
        }
    }
}
