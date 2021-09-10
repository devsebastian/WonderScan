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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.CropAndListFramesActivity
import com.devsebastian.wonderscan.adapter.GalleryAdapter
import java.util.*

class GalleryFragment : Fragment() {
    private lateinit var adapter: GalleryAdapter

    override fun onResume() {
        super.onResume()
        adapter.reset()
    }

    // get all images from external storage
    private fun getAllImages(): MutableList<String> {
        val uris: MutableList<String> = ArrayList()
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        activity?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )?.let { cursor ->
            while (cursor.moveToNext()) {
                val absolutePathOfImage =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                uris.add(absolutePathOfImage)
            }
            cursor.close()
        }
        return uris
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_gallery, container, false)
        val recyclerView: RecyclerView = v.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(activity, 5)
        adapter = GalleryAdapter(activity, ArrayList())
        recyclerView.adapter = adapter
        Thread {
            val uris = getAllImages()
            adapter.setImagePaths(uris)
            activity?.runOnUiThread { adapter.notifyDataSetChanged() }
        }.start()
        val fab = v.findViewById<View?>(R.id.fab)
        fab.setOnClickListener {
            v.findViewById<View?>(R.id.gallery_progress).visibility = View.VISIBLE
            Thread {
                val uris = adapter.getSelectedUris()
                val intent = Intent(activity, CropAndListFramesActivity::class.java)
                intent.putExtra(getString(R.string.intent_uris), uris)
                startActivity(intent)
                adapter.clearSelection()
                activity?.runOnUiThread {
                    v.findViewById<View?>(R.id.gallery_progress).visibility = View.GONE
                }
            }.start()
        }
        if (activity?.getPreferences(Context.MODE_PRIVATE)
                ?.getBoolean("closed_message", false) == true
        ) {
            v.findViewById<View?>(R.id.gallery_message).visibility = View.GONE
            v.findViewById<View?>(R.id.close).visibility = View.GONE
        }
        v.findViewById<View?>(R.id.close).setOnClickListener { view: View ->
            view.visibility = View.GONE
            v.findViewById<View?>(R.id.gallery_message).visibility = View.GONE
            activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
                ?.putBoolean("closed_message", true)?.apply()
        }
        return v
    }
}