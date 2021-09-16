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

package com.devsebastian.wonderscan.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.devsebastian.wonderscan.data.Document

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(document: Document)

    @Update
    fun update(document: Document)

    @Delete
    fun delete(document: Document)

    @Query("DELETE FROM Document where id=:docId")
    fun delete(docId: String)

    @Query("SELECT * FROM Document WHERE id=:docId")
    fun getDocument(docId: String): LiveData<Document>

    @Query("SELECT * FROM Document WHERE id=:docId")
    suspend fun getDocumentSync(docId: String): Document

    @Query("SELECT name FROM Document WHERE id=:docId")
    suspend fun getDocumentName(docId: String): String

    @Query("SELECT * FROM Document ORDER BY dateTime DESC")
    fun getAllDocuments(): LiveData<MutableList<Document>>

    @Query("SELECT * FROM Document WHERE name LIKE :query ORDER BY dateTime DESC")
    suspend fun search(query: String): MutableList<Document>
}