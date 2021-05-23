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

//C++: class DMatch

/**
 * Structure for matching: query descriptor index, train descriptor index, train
 * image index and distance between descriptors.
 */
public class DMatch {

    /**
     * Query descriptor index.
     */
    public int queryIdx;
    /**
     * Train descriptor index.
     */
    public int trainIdx;
    /**
     * Train image index.
     */
    public int imgIdx;

    // javadoc: DMatch::distance
    public float distance;

    // javadoc: DMatch::DMatch()
    public DMatch() {
        this(-1, -1, Float.MAX_VALUE);
    }

    // javadoc: DMatch::DMatch(_queryIdx, _trainIdx, _distance)
    public DMatch(int _queryIdx, int _trainIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = -1;
        distance = _distance;
    }

    // javadoc: DMatch::DMatch(_queryIdx, _trainIdx, _imgIdx, _distance)
    public DMatch(int _queryIdx, int _trainIdx, int _imgIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = _imgIdx;
        distance = _distance;
    }

    public boolean lessThan(DMatch it) {
        return distance < it.distance;
    }

    @Override
    public String toString() {
        return "DMatch [queryIdx=" + queryIdx + ", trainIdx=" + trainIdx
                + ", imgIdx=" + imgIdx + ", distance=" + distance + "]";
    }

}
