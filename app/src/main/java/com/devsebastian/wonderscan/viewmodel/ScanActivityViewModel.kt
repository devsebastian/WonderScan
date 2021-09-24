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

package com.devsebastian.wonderscan.viewmodel

import android.app.Activity
import android.content.Intent
import android.util.Pair
import androidx.lifecycle.*
import com.devsebastian.wonderscan.R
import com.devsebastian.wonderscan.activity.ListFramesActivity
import com.devsebastian.wonderscan.dao.DocumentDao
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.Document
import com.devsebastian.wonderscan.data.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanActivityViewModel(
    private val documentDao: DocumentDao?,
    private val frameDao: FrameDao?
) : ViewModel() {
    private var newDocument = true
    var docId: String? = null
    var count: LiveData<Int> = MutableLiveData(0)

    private val paths: MutableList<Pair<String, String>> = ArrayList()

    fun addPath(sourceUri: String, croppedUri: String) {
        paths.add(Pair.create(sourceUri, croppedUri))
    }

    fun pathsCount(): Int {
        return paths.size
    }

    fun getPageCount(docId: String): LiveData<Int> {
        viewModelScope.launch {
            count = frameDao?.getFrameCount(docId) ?: MutableLiveData(0)
        }
        newDocument = false
        this.docId = docId
        return count
    }

    fun capture(name: String, angle: Int, count: Int, activity: Activity) {
        viewModelScope.launch(Dispatchers.Default) {
            if (newDocument) {
                val doc = Document()
                docId = doc.id
                doc.name = name
                doc.dateTime = System.currentTimeMillis()
                documentDao?.insert(doc)
            }
            for (i in paths.indices) {
                val path = paths[i]
                val frame = Frame(
                    timeInMillis = System.currentTimeMillis(),
                    index = count + i,
                    angle = angle,
                    docId = docId!!,
                    uri = path.first,
                    croppedUri = path.second
                )
                frameDao?.insert(frame)
            }
            if (!activity.isDestroyed) {
                val i = Intent(activity, ListFramesActivity::class.java)
                i.putExtra(activity.getString(R.string.intent_document_id), docId)
                activity.startActivity(i)
                activity.finish()
            }
        }
    }
}

class ScanActivityViewModelFactory(
    private val documentDao: DocumentDao?,
    private val frameDao: FrameDao?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScanActivityViewModel(documentDao, frameDao) as T
    }
}