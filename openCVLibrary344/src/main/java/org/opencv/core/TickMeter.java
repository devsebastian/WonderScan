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



// C++: class TickMeter
//javadoc: TickMeter

public class TickMeter {

    protected final long nativeObj;
    protected TickMeter(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static TickMeter __fromPtr__(long addr) { return new TickMeter(addr); }

    //
    // C++:   cv::TickMeter::TickMeter()
    //

    //javadoc: TickMeter::TickMeter()
    public   TickMeter()
    {
        
        nativeObj = TickMeter_0();
        
        return;
    }


    //
    // C++:  double cv::TickMeter::getTimeMicro()
    //

    //javadoc: TickMeter::getTimeMicro()
    public  double getTimeMicro()
    {
        
        double retVal = getTimeMicro_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  double cv::TickMeter::getTimeMilli()
    //

    //javadoc: TickMeter::getTimeMilli()
    public  double getTimeMilli()
    {
        
        double retVal = getTimeMilli_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  double cv::TickMeter::getTimeSec()
    //

    //javadoc: TickMeter::getTimeSec()
    public  double getTimeSec()
    {
        
        double retVal = getTimeSec_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int64 cv::TickMeter::getCounter()
    //

    //javadoc: TickMeter::getCounter()
    public  long getCounter()
    {
        
        long retVal = getCounter_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  int64 cv::TickMeter::getTimeTicks()
    //

    //javadoc: TickMeter::getTimeTicks()
    public  long getTimeTicks()
    {
        
        long retVal = getTimeTicks_0(nativeObj);
        
        return retVal;
    }


    //
    // C++:  void cv::TickMeter::reset()
    //

    //javadoc: TickMeter::reset()
    public  void reset()
    {
        
        reset_0(nativeObj);
        
        return;
    }


    //
    // C++:  void cv::TickMeter::start()
    //

    //javadoc: TickMeter::start()
    public  void start()
    {
        
        start_0(nativeObj);
        
        return;
    }


    //
    // C++:  void cv::TickMeter::stop()
    //

    //javadoc: TickMeter::stop()
    public  void stop()
    {
        
        stop_0(nativeObj);
        
        return;
    }


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:   cv::TickMeter::TickMeter()
    private static native long TickMeter_0();

    // C++:  double cv::TickMeter::getTimeMicro()
    private static native double getTimeMicro_0(long nativeObj);

    // C++:  double cv::TickMeter::getTimeMilli()
    private static native double getTimeMilli_0(long nativeObj);

    // C++:  double cv::TickMeter::getTimeSec()
    private static native double getTimeSec_0(long nativeObj);

    // C++:  int64 cv::TickMeter::getCounter()
    private static native long getCounter_0(long nativeObj);

    // C++:  int64 cv::TickMeter::getTimeTicks()
    private static native long getTimeTicks_0(long nativeObj);

    // C++:  void cv::TickMeter::reset()
    private static native void reset_0(long nativeObj);

    // C++:  void cv::TickMeter::start()
    private static native void start_0(long nativeObj);

    // C++:  void cv::TickMeter::stop()
    private static native void stop_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
