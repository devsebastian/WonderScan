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

package com.devsebastian.wonderscan.data;


import org.opencv.core.Point;

import java.util.List;

public class BoundingRect {
    public Point TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

    public BoundingRect() {
        TOP_LEFT = new Point(0, 0);
        TOP_RIGHT = new Point(0, 0);
        BOTTOM_LEFT = new Point(0, 0);
        BOTTOM_RIGHT = new Point(0, 0);
    }

    public BoundingRect(int i) {
        TOP_LEFT = new Point(i, i);
        TOP_RIGHT = new Point(i, i);
        BOTTOM_LEFT = new Point(i, i);
        BOTTOM_RIGHT = new Point(i, i);
    }

    public void fromPoints(List<Point> points, double hRatio, double vRatio) {
        this.TOP_RIGHT = new Point(points.get(0).x * hRatio, points.get(0).y * vRatio);
        this.TOP_LEFT = new Point(points.get(1).x * hRatio, points.get(1).y * vRatio);
        this.BOTTOM_LEFT = new Point(points.get(2).x * hRatio, points.get(2).y * vRatio);
        this.BOTTOM_RIGHT = new Point(points.get(3).x * hRatio, points.get(3).y * vRatio);
    }

    public Point getTopLeft() {
        return TOP_LEFT;
    }

    public void setTopLeft(Point topLeft) {
        this.TOP_LEFT = topLeft;
    }

    public Point getTopRight() {
        return TOP_RIGHT;
    }

    public void setTopRight(Point topRight) {
        this.TOP_RIGHT = topRight;
    }

    public Point getBottomLeft() {
        return BOTTOM_LEFT;
    }

    public void setBottomLeft(Point bottomLeft) {
        this.BOTTOM_LEFT = bottomLeft;
    }

    public Point getBottomRight() {
        return BOTTOM_RIGHT;
    }

    public void setBottomRight(Point bottomRight) {
        this.BOTTOM_RIGHT = bottomRight;
    }

    public Point getTop() {
        return new Point((TOP_LEFT.x + TOP_RIGHT.x) / 2f, (TOP_LEFT.y + TOP_RIGHT.y) / 2f);
    }

    public void setTop(Point point) {
        Point top = getTop();
        double halfX = TOP_LEFT.x - top.x;
        double halfY = TOP_LEFT.y - top.y;
        this.TOP_LEFT = new Point(halfX + point.x, halfY + point.y);
        this.TOP_RIGHT = new Point(point.x - halfX, point.y - halfY);
    }

    public Point getBottom() {
        return new Point((BOTTOM_LEFT.x + BOTTOM_RIGHT.x) / 2f, (BOTTOM_LEFT.y + BOTTOM_RIGHT.y) / 2f);
    }

    public void setBottom(Point point) {
        Point bottom = getBottom();
        double halfX = BOTTOM_LEFT.x - bottom.x;
        double halfY = BOTTOM_LEFT.y - bottom.y;
        this.BOTTOM_LEFT = new Point(halfX + point.x, halfY + point.y);
        this.BOTTOM_RIGHT = new Point(point.x - halfX, point.y - halfY);
    }

    public Point getLeft() {
        return new Point((TOP_LEFT.x + BOTTOM_LEFT.x) / 2f, (TOP_LEFT.y + BOTTOM_LEFT.y) / 2f);
    }

    public void setLeft(Point point) {
        Point left = getLeft();
        double halfX = TOP_LEFT.x - left.x;
        double halfY = TOP_LEFT.y - left.y;
        this.TOP_LEFT = new Point(halfX + point.x, halfY + point.y);
        this.BOTTOM_LEFT = new Point(point.x - halfX, point.y - halfY);
    }

    public Point getRight() {
        return new Point((TOP_RIGHT.x + BOTTOM_RIGHT.x) / 2f, (TOP_RIGHT.y + BOTTOM_RIGHT.y) / 2f);
    }

    public void setRight(Point point) {
        Point right = getRight();
        double halfX = TOP_RIGHT.x - right.x;
        double halfY = TOP_RIGHT.y - right.y;
        this.TOP_RIGHT = new Point(halfX + point.x, halfY + point.y);
        this.BOTTOM_RIGHT = new Point(point.x - halfX, point.y - halfY);
    }
}
