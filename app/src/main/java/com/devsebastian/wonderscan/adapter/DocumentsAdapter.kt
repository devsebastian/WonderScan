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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.BaseActivity
import com.devsebastian.wonderscan.activity.ListFramesActivity
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class DocumentsAdapter(
    private val activity: Activity,
    private var data: MutableList<Document>,
    val viewModel: MainViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var simpleDateFormat: SimpleDateFormat =
        SimpleDateFormat("dd MMM, yyyy hh:mm", Locale.getDefault())
    private var maxWidth: Int

    @SuppressLint("NotifyDataSetChanged")
    fun updateDocuments(documents: MutableList<Document>) {
        data = documents.apply { add(Document()) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_NORMAL) {
            NormalViewHolder(layoutInflater.inflate(R.layout.row_document, parent, false))
        } else {
            FooterViewHolder(layoutInflater.inflate(R.layout.row_documents_footer, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < data.size) {
            val document = data[position]
            (holder as NormalViewHolder).apply {
                title.text = document.name
                subtitle.text = simpleDateFormat.format(Date(document.dateTime))
                viewModel.getPageCount(document.id).observe(activity as BaseActivity) { count ->
                    sheetNumber.text = String.format(
                        Locale.getDefault(),
                        "%d pages", count
                    )
                }
                itemView.setOnClickListener {
                    val intent = Intent(activity, ListFramesActivity::class.java)
                    intent.putExtra(activity.getString(R.string.intent_document_id), document.id)
                    activity.startActivity(intent)
                }
                viewModel.getFirstFrameImagePath(document.id)?.observe(activity) { uri ->
                    Glide.with(activity).load(uri).downsample(DownsampleStrategy.AT_MOST)
                        .into(imageView)
                }
            }
        } else {
            holder.itemView.setOnClickListener { Utils.shareAppLink(activity) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.size) {
            TYPE_FOOTER
        } else {
            TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return data.size + 1
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.iv_frame)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var subtitle: TextView = itemView.findViewById(R.id.tv_sub_title)
        var sheetNumber: TextView = itemView.findViewById(R.id.tv_number)
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_FOOTER = 1
    }

    init {
        val deviceWidth = Utils.getDeviceWidth()
        maxWidth = deviceWidth / 4
    }
}