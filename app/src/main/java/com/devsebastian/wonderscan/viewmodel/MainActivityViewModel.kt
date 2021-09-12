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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsebastian.wonderscan.dao.DocumentDao
import com.devsebastian.wonderscan.dao.FrameDao
import com.devsebastian.wonderscan.data.Document
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val documentDao: DocumentDao?,
    frameDao: FrameDao?
) : MainViewModel(frameDao) {
    var docId: Long? = null
    var documents: LiveData<MutableList<Document>>? = null

    fun getAllDocuments(): LiveData<MutableList<Document>>? {
        if (documents == null) {
            viewModelScope.launch {
                documents = documentDao?.getAllDocuments()
            }
        }
        return documents
    }
}

class MainActivityViewModelFactory(
    private val documentDao: DocumentDao,
    private val frameDao: FrameDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainActivityViewModel(documentDao, frameDao) as T
    }
}