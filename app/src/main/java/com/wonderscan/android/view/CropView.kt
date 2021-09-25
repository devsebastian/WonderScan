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

import android.annotation.SuppressLint
import android.content.*
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.wonderscan.android.R
import com.wonderscan.android.data.BoundingRect
import org.opencv.core.Point

class CropView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {
    private val line: Paint = Paint()
    private val circleStroke: Paint = Paint()
    private val fill: Paint = Paint()
    private var boundingRect: BoundingRect
    private var move = 0
    private var w = 0
    private var h = 0

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
        super.setLayoutParams(params)
        w = params.width
        h = params.height
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
    }

    fun getBoundingRect(): BoundingRect {
        return boundingRect
    }

    fun setBoundingRect(boundingRect: BoundingRect?) {
        if (boundingRect != null) {
            this.boundingRect = boundingRect
            invalidate()
        } else {
            setDefaultPositions()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val tl = boundingRect.topLeft
        val tr = boundingRect.topRight
        val bl = boundingRect.bottomLeft
        val br = boundingRect.bottomRight
        canvas.drawLine(tl.x.toFloat(), tl.y.toFloat(), tr.x.toFloat(), tr.y.toFloat(), line)
        canvas.drawLine(tr.x.toFloat(), tr.y.toFloat(), br.x.toFloat(), br.y.toFloat(), line)
        canvas.drawLine(br.x.toFloat(), br.y.toFloat(), bl.x.toFloat(), bl.y.toFloat(), line)
        canvas.drawLine(bl.x.toFloat(), bl.y.toFloat(), tl.x.toFloat(), tl.y.toFloat(), line)
        drawPin(canvas, boundingRect.topLeft)
        drawPin(canvas, boundingRect.topRight)
        drawPin(canvas, boundingRect.bottomLeft)
        drawPin(canvas, boundingRect.bottomRight)
        drawPin(canvas, boundingRect.getTop())
        drawPin(canvas, boundingRect.getBottom())
        drawPin(canvas, boundingRect.getLeft())
        drawPin(canvas, boundingRect.getRight())
    }

    private fun drawPin(canvas: Canvas, point: Point) {
        val inner = 15
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), (inner * 2).toFloat(), fill)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), (inner * 2).toFloat(), circleStroke)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> move = getSelectPinNumber(event.x, event.y)
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                if (move < 8 && isInside(x, y)) {
                    setPosition(x, y)
                }
            }
        }
        return true
    }

    private fun isInside(x: Float, y: Float): Boolean {
        return x > 0 && x < w && y > 0 && y < h
    }

    private fun getSelectPinNumber(x: Float, y: Float): Int {
        return when {
            collides(x, y, boundingRect.topLeft) -> 0
            collides(x, y, boundingRect.topRight) -> 1
            collides(x, y, boundingRect.bottomRight) -> 2
            collides(x, y, boundingRect.bottomLeft) -> 3
            collides(x, y, boundingRect.getLeft()) -> 4
            collides(x, y, boundingRect.getTop()) -> 5
            collides(x, y, boundingRect.getRight()) -> 6
            collides(x, y, boundingRect.getBottom()) -> 7
            else -> 8
        }
    }

    private fun setDefaultPositions() {
        val inner = 0.0
        boundingRect.topLeft = Point(inner, inner)
        boundingRect.topRight = Point(w - inner, inner)
        boundingRect.bottomLeft = Point(inner, h - inner)
        boundingRect.bottomRight = Point(w - inner, h - inner)
        invalidate()
    }

    private fun setPosition(x: Float, y: Float) {
        when (move) {
            0 -> {
                boundingRect.topLeft = Point(x.toDouble(), y.toDouble())
                invalidate()
            }
            1 -> {
                boundingRect.topRight = Point(x.toDouble(), y.toDouble())
                invalidate()
            }
            2 -> {
                boundingRect.bottomRight = Point(x.toDouble(), y.toDouble())
                invalidate()
            }
            3 -> {
                boundingRect.bottomLeft = Point(x.toDouble(), y.toDouble())
                invalidate()
            }
            4 -> {
                boundingRect.setLeft(Point(x.toDouble(), y.toDouble()))
                invalidate()
            }
            5 -> {
                boundingRect.setTop(Point(x.toDouble(), y.toDouble()))
                invalidate()
            }
            6 -> {
                boundingRect.setRight(Point(x.toDouble(), y.toDouble()))
                invalidate()
            }
            7 -> {
                boundingRect.setBottom(Point(x.toDouble(), y.toDouble()))
                invalidate()
            }
        }
    }

    private fun collides(x: Float, y: Float, point: Point): Boolean {
        val outer = 50
        return x < point.x + outer && x > point.x - outer && y < point.y + outer && y > point.y - outer
    }

    init {
        val color = context.getColor(R.color.colorAccent)
        val strokeWidth = 8
        val circleStrokeWidth = 6
        boundingRect = BoundingRect()
        line.strokeWidth = strokeWidth.toFloat()
        line.isAntiAlias = true
        line.style = Paint.Style.STROKE
        line.color = color
        circleStroke.strokeWidth = circleStrokeWidth.toFloat()
        circleStroke.isAntiAlias = true
        circleStroke.style = Paint.Style.STROKE
        circleStroke.color = color
        fill.color = Color.parseColor("#66FFFFFF")
    }
}