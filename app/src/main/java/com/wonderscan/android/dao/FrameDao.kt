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

package com.wonderscan.android.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.wonderscan.android.data.Frame

@Dao
interface FrameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(frame: Frame): Long

    @Update
    fun update(frame: Frame)

    @Delete
    fun delete(frame: Frame)

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    fun getFrames(docId: String): LiveData<MutableList<Frame>>

    @Query("SELECT * FROM Frame WHERE id=:id")
    fun getFrame(id: Long): LiveData<Frame>

    @Query("SELECT * FROM Frame WHERE docId=:docId ORDER BY `index`")
    suspend fun getFramesSync(docId: String): MutableList<Frame>

    @Query("SELECT COUNT(id) FROM Frame WHERE docId=:docId")
    fun getFrameCount(docId: String): LiveData<Int>

    @Query("SELECT uri FROM Frame WHERE docId=:docId AND `index`=0")
    fun getFrameUri(docId: String): LiveData<String>
}