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
package org.opencv.imgproc;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Size;

// C++: class CLAHE
//javadoc: CLAHE

public class CLAHE extends Algorithm {

    protected CLAHE(long addr) { super(addr); }

    // internal usage only
    public static CLAHE __fromPtr__(long addr) { return new CLAHE(addr); }

    //
    // C++:  Size cv::CLAHE::getTilesGridSize()
    //

    //javadoc: CLAHE::getTilesGridSize()
    public  Size getTilesGridSize()
    {
        
        Size retVal = new Size(getTilesGridSize_0(nativeObj));
        
        return retVal;
    }


    //
    // C++:  double cv::CLAHE::getClipLimit()
    //

    //javadoc: CLAHE::getClipLimit()
    public  double getClipLimit()
    {
        
        double retVal = getClipLimit_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::CLAHE::apply(Mat src, Mat& dst)
    //

    //javadoc: CLAHE::apply(src, dst)
    public  void apply(Mat src, Mat dst)
    {
        
        apply_0(nativeObj, src.nativeObj, dst.nativeObj);
        
        return;
    }


    //
    // C++:  void cv::CLAHE::collectGarbage()
    //

    //javadoc: CLAHE::collectGarbage()
    public  void collectGarbage()
    {
        
        collectGarbage_0(nativeObj);
        
        return;
    }


    //
    // C++:  void cv::CLAHE::setClipLimit(double clipLimit)
    //

    //javadoc: CLAHE::setClipLimit(clipLimit)
    public  void setClipLimit(double clipLimit)
    {
        
        setClipLimit_0(nativeObj, clipLimit);
        
        return;
    }


    //
    // C++:  void cv::CLAHE::setTilesGridSize(Size tileGridSize)
    //

    //javadoc: CLAHE::setTilesGridSize(tileGridSize)
    public  void setTilesGridSize(Size tileGridSize)
    {
        
        setTilesGridSize_0(nativeObj, tileGridSize.width, tileGridSize.height);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  Size cv::CLAHE::getTilesGridSize()
    private static native double[] getTilesGridSize_0(long nativeObj);

    // C++:  double cv::CLAHE::getClipLimit()
    private static native double getClipLimit_0(long nativeObj);

    // C++:  void cv::CLAHE::apply(Mat src, Mat& dst)
    private static native void apply_0(long nativeObj, long src_nativeObj, long dst_nativeObj);

    // C++:  void cv::CLAHE::collectGarbage()
    private static native void collectGarbage_0(long nativeObj);

    // C++:  void cv::CLAHE::setClipLimit(double clipLimit)
    private static native void setClipLimit_0(long nativeObj, double clipLimit);

    // C++:  void cv::CLAHE::setTilesGridSize(Size tileGridSize)
    private static native void setTilesGridSize_0(long nativeObj, double tileGridSize_width, double tileGridSize_height);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
