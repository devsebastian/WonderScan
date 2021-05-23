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
package org.opencv.core;

import java.lang.String;

// C++: class Algorithm
//javadoc: Algorithm

public class Algorithm {

    protected final long nativeObj;
    protected Algorithm(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static Algorithm __fromPtr__(long addr) { return new Algorithm(addr); }

    //
    // C++:  String cv::Algorithm::getDefaultName()
    //

    //javadoc: Algorithm::getDefaultName()
    public  String getDefaultName()
    {
        
        String retVal = getDefaultName_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  bool cv::Algorithm::empty()
    //

    //javadoc: Algorithm::empty()
    public  boolean empty()
    {
        
        boolean retVal = empty_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::Algorithm::clear()
    //

    //javadoc: Algorithm::clear()
    public  void clear()
    {
        
        clear_0(nativeObj);
        
        return;
    }


    //
    // C++:  void cv::Algorithm::read(FileNode fn)
    //

    // Unknown type 'FileNode' (I), skipping the function


    //
    // C++:  void cv::Algorithm::save(String filename)
    //

    //javadoc: Algorithm::save(filename)
    public  void save(String filename)
    {
        
        save_0(nativeObj, filename);
        
        return;
    }


    //
    // C++:  void cv::Algorithm::write(Ptr_FileStorage fs, String name = String())
    //

    // Unknown type 'Ptr_FileStorage' (I), skipping the function


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:  String cv::Algorithm::getDefaultName()
    private static native String getDefaultName_0(long nativeObj);

    // C++:  bool cv::Algorithm::empty()
    private static native boolean empty_0(long nativeObj);

    // C++:  void cv::Algorithm::clear()
    private static native void clear_0(long nativeObj);

    // C++:  void cv::Algorithm::save(String filename)
    private static native void save_0(long nativeObj, String filename);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
