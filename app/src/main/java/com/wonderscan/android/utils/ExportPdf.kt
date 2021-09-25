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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.wonderscan.android.data.Frame

class ExportPdf {

    companion object {

        private fun createPage(pdfDocument: PdfDocument, bitmap: Bitmap, width: Int, height: Int) {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, 0).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.WHITE
            canvas.drawPaint(paint)
            canvas.drawBitmap(
                bitmap,
                (width - bitmap.width) / 2f,
                (height - bitmap.height) / 2f,
                null
            )
            pdfDocument.finishPage(page)
        }

        fun exportPdf(frames: List<Frame>): PdfDocument {
            var bitmap: Bitmap?
            val pdf = PdfDocument()
            var width = 0
            var height = 0
            for (frame in frames) {
                bitmap = BitmapFactory.decodeFile(frame.editedUri)
                if (bitmap.width > width) width = bitmap.width
                if (bitmap.height > height) height = bitmap.height
                createPage(pdf, bitmap, width, height)
            }
            return pdf
        }
    }
}