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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devsebastian.wonderscan.dao.FrameDao
import kotlinx.coroutines.launch

open class MainViewModel(
    private val frameDao: FrameDao?
) : ViewModel() {

    fun getPageCount(docId: String): LiveData<Int> {
        lateinit var count: LiveData<Int>
        viewModelScope.launch {
            count = frameDao?.getFrameCount(docId) ?: MutableLiveData(0)
        }
        return count
    }

    fun getFirstFrameImagePath(docId: String): LiveData<String>? {
        return frameDao?.getFrameUri(docId)
    }
}