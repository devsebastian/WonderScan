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
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.adapter.DocumentsAdapter
import com.devsebastian.wonderscan.data.Document
import java.util.*

class SearchActivity : BaseActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var documentsAdapter: DocumentsAdapter
    private lateinit var documents: ArrayList<Document>
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val searchEt = findViewById<EditText?>(R.id.et_search)
        dbHelper = DBHelper(this)
        documents = dbHelper.getAllDocuments()
        documentsAdapter = DocumentsAdapter(this, documents)
        val recyclerView = findViewById<RecyclerView?>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = documentsAdapter
        searchEt.requestFocus()
        searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                documents = dbHelper.searchDocuments(charSequence.toString())
                documentsAdapter.updateDocuments(documents)
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }
}