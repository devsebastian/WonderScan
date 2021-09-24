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
package com.wonderscan.android.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.wonderscan.android.R

class GalleryAdapter(var context: Context?, private var data: List<String>) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder?>() {
    private val positions: MutableList<Int> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun reset() {
        positions.clear()
        notifyDataSetChanged()
    }

    fun setImagePaths(data: MutableList<String>) {
        this.data = data
    }

    fun clearSelection() {
        positions.clear()
    }

    fun getSelectedUris(): ArrayList<String> {
        val uris = ArrayList<String>()
        for (pos in positions) {
            uris.add(data[pos])
        }
        return uris
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.row_image, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = data[position]
        holder.apply {
            context?.let {
                Glide.with(it).load(uri).centerCrop().downsample(DownsampleStrategy.AT_MOST)
                    .into(imageView)
            }
            if (positions.contains(position)) {
                imageView.alpha = 0.5f
                bubble.text = (positions.indexOf(position) + 1).toString()
                bubble.visibility = View.VISIBLE
            } else {
                imageView.alpha = 1f
                bubble.visibility = View.GONE
            }
            imageView.setOnClickListener {
                if (positions.contains(adapterPosition)) {
                    positions.remove(Integer.valueOf(adapterPosition))
                    notifyDataSetChanged()
                } else {
                    positions.add(adapterPosition)
                    notifyItemChanged(adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.imageview)
        var bubble: TextView = itemView.findViewById(R.id.iv_bubble)

    }
}