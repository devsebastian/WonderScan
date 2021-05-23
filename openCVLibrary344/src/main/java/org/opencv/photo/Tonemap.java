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

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;

// C++: class Tonemap
//javadoc: Tonemap

public class Tonemap extends Algorithm {

    protected Tonemap(long addr) { super(addr); }

    // internal usage only
    public static Tonemap __fromPtr__(long addr) { return new Tonemap(addr); }

    //
    // C++:  float cv::Tonemap::getGamma()
    //

    //javadoc: Tonemap::getGamma()
    public  float getGamma()
    {
        
        float retVal = getGamma_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::Tonemap::process(Mat src, Mat& dst)
    //

    //javadoc: Tonemap::process(src, dst)
    public  void process(Mat src, Mat dst)
    {
        
        process_0(nativeObj, src.nativeObj, dst.nativeObj);
        
        return;
    }


    //
    // C++:  void cv::Tonemap::setGamma(float gamma)
    //

    //javadoc: Tonemap::setGamma(gamma)
    public  void setGamma(float gamma)
    {
        
        setGamma_0(nativeObj, gamma);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  float cv::Tonemap::getGamma()
    private static native float getGamma_0(long nativeObj);

    // C++:  void cv::Tonemap::process(Mat src, Mat& dst)
    private static native void process_0(long nativeObj, long src_nativeObj, long dst_nativeObj);

    // C++:  void cv::Tonemap::setGamma(float gamma)
    private static native void setGamma_0(long nativeObj, float gamma);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
