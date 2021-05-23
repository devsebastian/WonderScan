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

package org.opencv.core;

//javadoc:Size_
public class Size {

    public double width, height;

    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Size() {
        this(0, 0);
    }

    public Size(Point p) {
        width = p.x;
        height = p.y;
    }

    public Size(double[] vals) {
        set(vals);
    }

    public void set(double[] vals) {
        if (vals != null) {
            width = vals.length > 0 ? vals[0] : 0;
            height = vals.length > 1 ? vals[1] : 0;
        } else {
            width = 0;
            height = 0;
        }
    }

    public double area() {
        return width * height;
    }

    public boolean empty() {
        return width <= 0 || height <= 0;
    }

    public Size clone() {
        return new Size(width, height);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Size)) return false;
        Size it = (Size) obj;
        return width == it.width && height == it.height;
    }

    @Override
    public String toString() {
        return (int)width + "x" + (int)height;
    }

}
