/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fingerprint;

import android.os.SystemProperties;

import com.gionee.fingerprint.IGnEnrolCallback;
import com.gionee.fingerprint.IGnIdentifyCallback;
import com.mediatek.settings.sim.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

public class AuroraFingerprintUtils {
    private static final String TAG = "Fingerprint_Settings";
    public static final int MSG_REIDENTIFY = 2;
    public static final int MSG_REFRESH_SCREEN = 3;
    public static final int MSG_REFRESH_HEADER = 4;
    public static final int MSG_DEFAULT_HEADER = 5;

    private static final String CLASS_GNFPMANAGER = "com.gionee.fingerprint.GnFingerPrintManager";
    private Class<?> mGnFPmgrClass;
    private Object mFingerPrintManager;


    public static final int REASON_NOMATCH = 0;
    public static final int REASON_TIMEOUT = 1;
    public static final int REASON_CANCEL = 2;

    public AuroraFingerprintUtils() {
        try {
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            mFingerPrintManager = GnFingerPrintManager.newInstance();
            mGnFPmgrClass = GnFingerPrintManager;
            if (mFingerPrintManager == null || mGnFPmgrClass == null) {
                Log.e(TAG, "AuroraFingerprintUtils IS NULL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * all fingerPrint function methods
     */
    public static boolean isFingerPrintSupported() {
        return !SystemProperties.get("ro.gn.fingerprint.support", "no").equals("no");
    }

    /**
     * all fingerPrint function methods
     */
    public void startEnrol(IGnEnrolCallback cb, int fingerId, String name) {
        try {
            Log.d(TAG, "startEnrol() start fingerId = " + fingerId + " name = " + name);

            Method startEnrol = mGnFPmgrClass.getMethod("startEnrol", IGnEnrolCallback.class, int.class,
                    String.class);
            startEnrol.invoke(mFingerPrintManager, cb, fingerId, name);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startEnrol() e=" + e);
        }
    }

    public void startIdentify(IGnIdentifyCallback cb, int[] ids) {
        try {
            Method startIdentify = mGnFPmgrClass.getMethod("startIdentify", IGnIdentifyCallback.class,
                    int[].class);
            startIdentify.invoke(mFingerPrintManager, cb, ids);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startIdentify() e=" + e);
        }
    }
    
    /*public void startCaptureImage(IGnCaptureImageCallback cb) {
        try {
            Log.d(TAG, "startCaptureImage() start");
            Class<?> GnFingerPrintManager = (Class<?>) Class.forName(CLASS_GNFPMANAGER);
            Object obj = GnFingerPrintManager.newInstance();
            mGnFPmgrClass = GnFingerPrintManager;
            mFingerPrintManager = obj;

            Method open = GnFingerPrintManager.getMethod("open");
            open.invoke(obj);
            Log.d(TAG, "startCaptureImage()-->open() aleady");

            Method startIdentify = GnFingerPrintManager.getMethod("startCaptureImage", IGnCaptureImageCallback.class);
            startIdentify.invoke(obj, cb);
            Log.d(TAG, "startCaptureImage() aleady");

        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(1);
        }
    }*/

    public void cancel() {
        Log.d(TAG, "cancel()-start--");
        try {
            Method cancel = mGnFPmgrClass.getMethod("cancel");
            cancel.invoke(mFingerPrintManager);
        } catch (Exception e) {
            e.toString();
        }
    }


    public int[] getIds() {
        Log.d(TAG, "getIds() start");
        try {
            Method getIds = mGnFPmgrClass.getMethod("getIds");
            int[] ids = (int[]) getIds.invoke(mFingerPrintManager);

            Log.d(TAG, "getIds() end ids=" + Arrays.toString(ids));
            return ids;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getIds() e=" + e);
        }
        return null;
        // TODO: handle exception
    }

    public void removeData(int fingerId) {
        Log.d(TAG, "removeData() start");
        try {
            Method removeData = mGnFPmgrClass.getMethod("removeData", int.class);
            removeData.invoke(mFingerPrintManager, fingerId);
            Log.d(TAG, "removeData() end");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "removeData() e=" + e);
        }
    }

    public String getNameById(int fingerId) {
        Log.d(TAG, "getNameById() start");
        try {

            Method getNameById = mGnFPmgrClass.getMethod("getNameById", int.class);
            String name = (String) getNameById.invoke(mFingerPrintManager, fingerId);

            Log.d(TAG, "getNameById() end name=" + name);
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getNameById() e=" + e);
        }
        return null;
        // TODO: handle exception
    }

    public String[] getNames() {
        Log.d(TAG, "getNames() start");
        try {

            Method getNames = mGnFPmgrClass.getMethod("getNames");
            String[] names = (String[]) getNames.invoke(mFingerPrintManager);

            Log.d(TAG, "getNames() end nameList=" + Arrays.toString(names));
            return names;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getNames() e=" + e);
        }
        return null;
        // TODO: handle exception

    }

    public void renameById(int fingerId, String name) {
        Log.d(TAG, "renameById() start");
        try {

            Method renameById = mGnFPmgrClass.getMethod("renameById", int.class, String.class);
            renameById.invoke(mFingerPrintManager, fingerId, name);

            Log.d(TAG, "renameById() end");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "renameById() e=" + e);
        }
    }
    // identify callback
    /*private IGnIdentifyCallback identifyCb = new IGnIdentifyCallback() {

        public void onWaitingForInput() {

            Log.d(TAG, "onWaitingForInput()---");
            //String details = DemoReflectActivity.this.getString(R.string.waitinput, ++mWaitCount);
            //displayView.setText(details);
        }

        public void onInput() {
            Log.d(TAG, "onInput()---");
            //displayView.setText(R.string.input);
        }

        public void onCaptureCompleted() {
            Log.d(TAG, "onCaptureCompleted()---");
            //displayView.setText(R.string.capturecompleted);
        }

        public void onCaptureFailed(int reason) {
            Log.d(TAG, "onCaptureFailed()---");
            //displayView.setText(R.string.capturefailed);
        }

        public void onIdentified(int fingerId, boolean updated) {
            Log.d(TAG, "onIdentified()---");
            //displayView.setText(R.string.identify_completed);
            mHandler.sendEmptyMessage(1);
            //mWaitCount = 0;
        }

        public void onNoMatch() {
            Log.d(TAG, "onNoMatch()---");
            //displayView.setText(R.string.identify_fail);
            mHandler.sendEmptyMessage(1);
            //mWaitCount = 0;
        }

        public void onExtIdentifyMsg(Message msg, String description) {
            Log.d(TAG, "onExtIdentifyMsg()---");
            //displayView.setText(R.string.identify_ext);
        }
    };*/

}
