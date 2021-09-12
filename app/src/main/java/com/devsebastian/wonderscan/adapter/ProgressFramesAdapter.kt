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
package com.devsebastian.wonderscan.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.ListFramesActivity
import com.devsebastian.wonderscan.activity.ViewPageActivity
import com.devsebastian.wonderscan.data.Frame
import java.util.*

class ProgressFramesAdapter(
    var activity: Activity,
    private var docId: String,
    var frames: List<Frame>
) : RecyclerView.Adapter<ProgressFramesAdapter.ViewHolder?>() {

    fun swap(from: Int, to: Int) {
        Collections.swap(frames, from, to)
        notifyItemMoved(from, to)
    }

    fun get(index: Int): Frame {
        return frames[index]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.row_frame, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val frame = frames[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(activity, ViewPageActivity::class.java)
            intent.putExtra(activity.getString(R.string.intent_document_id), docId)
            intent.putExtra(activity.getString(R.string.intent_frame_position), position)
            activity.startActivityForResult(intent, ListFramesActivity.VIEW_PAGE_ACTIVITY)
        }
        if (frame.name == null || frame.name!!.isEmpty()) {
            holder.textView.text = (position + 1).toString()
        } else {
            holder.textView.text = frame.name
        }
        if (frame.editedUri == null) {
            Glide.with(activity).load(frame.uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                .into(holder.imageView)
        } else {
            Glide.with(activity).load(frame.editedUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                .into(holder.imageView)
            holder.progressBar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return frames.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.iv_frame)
        var textView: TextView = itemView.findViewById(R.id.tv_frame)
        var progressBar: View = itemView.findViewById(R.id.progress)

        init {
            itemView.setOnLongClickListener {
                val v = itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(50)
                false
            }
        }
    }

}