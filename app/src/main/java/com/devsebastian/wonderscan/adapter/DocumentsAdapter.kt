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
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.devsebastian.wonderscan.utils.DBHelper
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.utils.Utils
import com.devsebastian.wonderscan.activity.ListFramesActivity
import com.devsebastian.wonderscan.data.Document
import java.text.SimpleDateFormat
import java.util.*

class DocumentsAdapter(activity: Activity, private var data: ArrayList<Document>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var simpleDateFormat: SimpleDateFormat =
        SimpleDateFormat("dd MMM, yyyy hh:mm", Locale.getDefault())
    var context = activity
    private var maxWidth: Int
    var dbHelper: DBHelper = DBHelper(context)

    @SuppressLint("NotifyDataSetChanged")
    fun updateDocuments(documents: ArrayList<Document>) {
        data = documents
        data.add(Document())
        notifyDataSetChanged()
    }

    fun insertDocument(document: Document) {
        data.add(0, document)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View
        return if (viewType == TYPE_NORMAL) {
            listItem = layoutInflater.inflate(R.layout.row_document, parent, false)
            NormalViewHolder(listItem)
        } else {
            listItem = layoutInflater.inflate(R.layout.row_documents_footer, parent, false)
            FooterViewHolder(listItem)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < data.size) {
            val document = data[position]
            val h = holder as NormalViewHolder
            h.title.text = document.name
            h.subtitle.text = simpleDateFormat.format(Date(document.dateTime))
            h.sheetNumber.text = String.format(
                Locale.getDefault(),
                "%d pages",
                dbHelper.getPageCount(document.id)
            )
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ListFramesActivity::class.java)
                intent.putExtra(context.getString(R.string.intent_document_id), document.id)
                context.startActivity(intent)
            }
            Glide.with(context).load(dbHelper.getFirstFrameImagePath(document.id))
                .downsample(DownsampleStrategy.AT_MOST).into(h.imageView)
        } else {
            holder.itemView.setOnClickListener { shareAppLink(context) }
        }
    }

    private fun shareAppLink(context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message))
        context.startActivity(intent)
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

    // RecyclerView recyclerView;
    init {
        val deviceWidth = Utils.getDeviceWidth()
        maxWidth = deviceWidth / 4
    }
}