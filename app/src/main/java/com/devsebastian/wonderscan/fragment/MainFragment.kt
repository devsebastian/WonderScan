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
package com.devsebastian.wonderscan.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devsebastian.wonderscan.MyApplication
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.ScanActivity
import com.devsebastian.wonderscan.adapter.DocumentsAdapter
import com.devsebastian.wonderscan.viewmodel.MainActivityViewModel
import com.devsebastian.wonderscan.viewmodel.MainActivityViewModelFactory

open class MainFragment : Fragment(), View.OnClickListener {
    lateinit var documentsAdapter: DocumentsAdapter

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main, container, false)
        val fab = v.findViewById<View?>(R.id.fab)
        fab.setOnClickListener(this)

        activity?.let { activity ->
            (activity.application as MyApplication).database?.let { db ->
                viewModel = ViewModelProvider(
                    activity,
                    MainActivityViewModelFactory(db.documentDao(), db.frameDao())
                ).get(MainActivityViewModel::class.java)
            }
        }

        val recyclerView: RecyclerView = v.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)



        recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.getAllDocuments()?.observe(viewLifecycleOwner) { documents ->
            documentsAdapter = DocumentsAdapter(requireActivity(), documents, viewModel)
            recyclerView.adapter = documentsAdapter
        }
        return v
    }

    override fun onClick(view: View) {
        if (view.id == R.id.fab) {
            startActivity(Intent(requireActivity(), ScanActivity::class.java))
        }
    }
}