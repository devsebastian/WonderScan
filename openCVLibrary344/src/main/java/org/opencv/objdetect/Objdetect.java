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

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.objdetect;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.utils.Converters;

// C++: class Objdetect
//javadoc: Objdetect

public class Objdetect {

    public static final int
            CASCADE_DO_CANNY_PRUNING = 1,
            CASCADE_SCALE_IMAGE = 2,
            CASCADE_FIND_BIGGEST_OBJECT = 4,
            CASCADE_DO_ROUGH_SEARCH = 8;


    //
    // C++:  void cv::groupRectangles(vector_Rect& rectList, vector_int& weights, int groupThreshold, double eps = 0.2)
    //

    //javadoc: groupRectangles(rectList, weights, groupThreshold, eps)
    public static void groupRectangles(MatOfRect rectList, MatOfInt weights, int groupThreshold, double eps)
    {
        Mat rectList_mat = rectList;
        Mat weights_mat = weights;
        groupRectangles_0(rectList_mat.nativeObj, weights_mat.nativeObj, groupThreshold, eps);
        
        return;
    }

    //javadoc: groupRectangles(rectList, weights, groupThreshold)
    public static void groupRectangles(MatOfRect rectList, MatOfInt weights, int groupThreshold)
    {
        Mat rectList_mat = rectList;
        Mat weights_mat = weights;
        groupRectangles_1(rectList_mat.nativeObj, weights_mat.nativeObj, groupThreshold);
        
        return;
    }




    // C++:  void cv::groupRectangles(vector_Rect& rectList, vector_int& weights, int groupThreshold, double eps = 0.2)
    private static native void groupRectangles_0(long rectList_mat_nativeObj, long weights_mat_nativeObj, int groupThreshold, double eps);
    private static native void groupRectangles_1(long rectList_mat_nativeObj, long weights_mat_nativeObj, int groupThreshold);

}
