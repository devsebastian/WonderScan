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

package com.wonderscan.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wonderscan.android.dao.DocumentDao
import com.wonderscan.android.dao.FrameDao
import com.wonderscan.android.data.Document
import com.wonderscan.android.data.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewPageActivityViewModel(
    documentDao: DocumentDao,
    private val frameDao: FrameDao,
    docId: String
) : ViewModel() {

    val document: LiveData<Document> = documentDao.getDocument(docId)
    val frames: LiveData<MutableList<Frame>> = frameDao.getFrames(docId)
    var currentIndex = 0

    fun updateFrame(frame: Frame) {
        viewModelScope.launch(Dispatchers.IO) {
            frameDao.update(frame)
        }
    }

    fun deleteFrame(frame: Frame) {
        viewModelScope.launch(Dispatchers.IO) {
            frameDao.delete(frame)
        }
    }
}

class ViewPageActivityViewModelFactory(
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao,
    private val docId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ViewPageActivityViewModel(documentDao, frameDao, docId) as T
    }
}