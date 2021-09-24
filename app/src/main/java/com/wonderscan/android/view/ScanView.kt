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
package com.wonderscan.android.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.wonderscan.android.R
import com.wonderscan.android.data.BoundingRect
import org.opencv.core.Point

class ScanView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {
    private val line: Paint = Paint()
    private val circleStroke: Paint = Paint()
    private val fill: Paint = Paint()
    private var dynamicBoundingRect: BoundingRect
    private var delayedHandler: Handler? = null
    private var delayedRunnable: Runnable? = null
    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
    }

    fun setBoundingRect(boundingRect: BoundingRect?) {
        if (boundingRect != null) {
            if (delayedHandler != null) {
                removeDelayedHandler()
            }
            dynamicBoundingRect = boundingRect
            invalidate()
        } else {
            if (delayedHandler == null) {
                startDelayedHandler()
            }
        }
    }

    private fun startDelayedHandler() {
        delayedHandler = Handler(Looper.getMainLooper())
        if (delayedRunnable == null) delayedRunnable = Runnable {
            dynamicBoundingRect = BoundingRect(-100.0)
            invalidate()
            removeDelayedHandler()
        }
        delayedHandler?.postDelayed(delayedRunnable!!, 100)
    }

    private fun removeDelayedHandler() {
        delayedHandler?.removeCallbacks(delayedRunnable!!)
        delayedHandler = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val tl = dynamicBoundingRect.topLeft
        val tr = dynamicBoundingRect.topRight
        val bl = dynamicBoundingRect.bottomLeft
        val br = dynamicBoundingRect.bottomRight
        canvas.drawLine(tl.x.toFloat(), tl.y.toFloat(), tr.x.toFloat(), tr.y.toFloat(), line)
        canvas.drawLine(tr.x.toFloat(), tr.y.toFloat(), br.x.toFloat(), br.y.toFloat(), line)
        canvas.drawLine(br.x.toFloat(), br.y.toFloat(), bl.x.toFloat(), bl.y.toFloat(), line)
        canvas.drawLine(bl.x.toFloat(), bl.y.toFloat(), tl.x.toFloat(), tl.y.toFloat(), line)
        drawPin(canvas, dynamicBoundingRect.topLeft)
        drawPin(canvas, dynamicBoundingRect.topRight)
        drawPin(canvas, dynamicBoundingRect.bottomLeft)
        drawPin(canvas, dynamicBoundingRect.bottomRight)
        drawPin(canvas, dynamicBoundingRect.getTop())
        drawPin(canvas, dynamicBoundingRect.getBottom())
        drawPin(canvas, dynamicBoundingRect.getLeft())
        drawPin(canvas, dynamicBoundingRect.getRight())
    }

    private fun drawPin(canvas: Canvas, point: Point) {
        val inner = 12
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), (inner * 2).toFloat(), fill)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), (inner * 2).toFloat(), circleStroke)
    }

    init {
        val color = context.getColor(R.color.colorAccent)
        dynamicBoundingRect = BoundingRect()
        val strokeWidth = 8
        line.strokeWidth = strokeWidth.toFloat()
        line.isAntiAlias = true
        line.style = Paint.Style.STROKE
        line.color = color
        val circleStrokeWidth = 6
        circleStroke.strokeWidth = circleStrokeWidth.toFloat()
        circleStroke.isAntiAlias = true
        circleStroke.style = Paint.Style.STROKE
        circleStroke.color = Color.WHITE
        fill.color = context.getColor(R.color.colorAccent)
    }
}