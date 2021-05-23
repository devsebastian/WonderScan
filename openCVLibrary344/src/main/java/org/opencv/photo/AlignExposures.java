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
package org.opencv.photo;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.utils.Converters;

// C++: class AlignExposures
//javadoc: AlignExposures

public class AlignExposures extends Algorithm {

    protected AlignExposures(long addr) { super(addr); }

    // internal usage only
    public static AlignExposures __fromPtr__(long addr) { return new AlignExposures(addr); }

    //
    // C++:  void cv::AlignExposures::process(vector_Mat src, vector_Mat dst, Mat times, Mat response)
    //

    //javadoc: AlignExposures::process(src, dst, times, response)
    public  void process(List<Mat> src, List<Mat> dst, Mat times, Mat response)
    {
        Mat src_mat = Converters.vector_Mat_to_Mat(src);
        Mat dst_mat = Converters.vector_Mat_to_Mat(dst);
        process_0(nativeObj, src_mat.nativeObj, dst_mat.nativeObj, times.nativeObj, response.nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  void cv::AlignExposures::process(vector_Mat src, vector_Mat dst, Mat times, Mat response)
    private static native void process_0(long nativeObj, long src_mat_nativeObj, long dst_mat_nativeObj, long times_nativeObj, long response_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
