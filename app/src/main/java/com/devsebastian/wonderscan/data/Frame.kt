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
package com.devsebastian.wonderscan.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    foreignKeys = [ForeignKey(
        entity = Document::class,
        childColumns = ["docId"],
        parentColumns = ["id"],
        onDelete = CASCADE
    )]
)
class Frame(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(index = true)
    var docId: String,
    var index: Int,
    var timeInMillis: Long,
    var angle: Int,
    var name: String? = null,
    var note: String? = null,
    var ocr: String? = null,
    var uri: String,
    var editedUri: String? = null,
    var croppedUri: String? = null
) : Serializable