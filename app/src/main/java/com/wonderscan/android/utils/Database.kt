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

package com.wonderscan.android.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wonderscan.android.dao.DocumentDao
import com.wonderscan.android.dao.FrameDao
import com.wonderscan.android.data.Document
import com.wonderscan.android.data.Frame


@Database(entities = [Document::class, Frame::class], version = 5, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun frameDao(): FrameDao
    abstract fun documentDao(): DocumentDao

    companion object {
        private var INSTANCE: MyDatabase? = null

        fun geDatabase(context: Context): MyDatabase? {
            synchronized(Database::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MyDatabase::class.java,
                        "database"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE
        }
    }
}