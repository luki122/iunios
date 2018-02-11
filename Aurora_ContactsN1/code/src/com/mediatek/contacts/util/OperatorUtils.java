package com.mediatek.contacts.util;

import android.os.SystemProperties;

public class OperatorUtils {

    public static String mOptr = null;
    public static String mSpec = null;
    public static String mSeg = null;

    public static String getOptrProperties() {
        if (null == mOptr) {
            mOptr = SystemProperties.get("ro.operator.optr");

            // GIONEE licheng Jul 24, 2012 modify for CR00655268 start
            /*
            if (null == mOptr) {
             */
            String isForce = SystemProperties.get("ro.gn.force.open.optr");
            if (null == mOptr || isForce.equals("yes") && mOptr.equals("OP02")) {
            // GIONEE licheng Jul 24, 2012 modify for CR00655268 end

                mOptr = "";
            }
            //Gionee <huangzy> <2013-05-07> add for CR00809155 begin
            else if (null != mOptr && mOptr.equals("OP01")) {
            	mOptr = "OP02";
            }
            //Gionee <huangzy> <2013-05-07> add for CR00809155 end
        }
        return mOptr;   
    }

    public static String getSpecProperties() {
        if (null == mSpec) {
            mSpec = SystemProperties.get("ro.operator.spec");
            if (null == mSpec) {
                mSpec = "";
            }
        }
        return mSpec;
    }

    public static String getSegProperties() {
        if (null == mSeg) {
            mSeg = SystemProperties.get("ro.operator.seg");
            if (null == mSeg) {
                mSeg = "";
            }
        }
        return mSeg;
    }

    // Gionee <xuhz> <2013-08-16> add for CR00858149 begin
    public static String mActualOptr = null;
    public static String getActualOptrProperties() {
        if (null == mActualOptr) {
        	mActualOptr = SystemProperties.get("ro.operator.optr");

            String isForce = SystemProperties.get("ro.gn.force.open.optr");
            if (null == mActualOptr || isForce.equals("yes") && mActualOptr.equals("OP02")) {
            	mActualOptr = "";
            }
        }
        return mActualOptr;   
    }
    // Gionee <xuhz> <2013-08-16> add for CR00858149 end
}
