/** 
 * Copyright (c) 2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gionee.widget;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.android.lunar.ILunarService;

import java.util.ArrayList;
import java.util.Locale;

/**
 * We use this to load the lunar cursor.
 */
public class LunarLoader extends CursorLoader {
    private static final String TAG = "LunarLoader";

    private int mYear = -1;
    private int mMonth = -1;

    private static final String[] COLS = new String[] { "monthDay", "lunar", "isSpecial" };

    private static ILunarService sLunarService = null;

    private static final int MSG_START_LOAD = 0;

    // We used this to handle MSG_START_LOAD to start loading. 
    // That is caused by we need wait for the LunarService was bound.
    private Handler mHandler = new Handler() {
        private static final int DELAY_MILLIS = 200;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_LOAD:
                    if (sLunarService != null) {
                        startLoading();
                    } else {
                        // if the service is null, we will try to start loading again.
                        sendEmptyMessageDelayed(MSG_START_LOAD, DELAY_MILLIS);
                    }
            }
            super.handleMessage(msg);
        }

    };

    public LunarLoader(Context context) {
        super(context);
    }

    public void load(Time time) {
        if (Utility.DEBUG) {
            Log.i(TAG, "Start to load cursor, and the time is:" + time.year + "-" + time.month);
        }

        reset();
        mYear = time.year;
        mMonth = time.month;
        mHandler.sendEmptyMessage(MSG_START_LOAD);
    }

    public static void setLunarService(ILunarService service) {
        sLunarService = service;
    }

    /**
     * Get if we need show the lunar info.
     *
     * @return true if the local language is "zh", and the country is "cn". Otherwise false.
     */
    public static boolean showLunar() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry().toLowerCase();
        if ("zh".equals(language) && "cn".equals(country)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * We will get the lunar info from the LunarService. And then build the {@link LunarCursor}.
     */
    @Override
    public Cursor loadInBackground() {
        if (!showLunar() || sLunarService == null) {
            if (Utility.DEBUG) {
                Log.d(TAG, "Load in background, but shouldn't show lunar info, return null");
            }
            return null;
        }

        MatrixCursor cursor = new MatrixCursor(COLS);
        try {
            // get the lunar info from the LunarService.
            String[] lunars = sLunarService.getLunarAndType(mYear, mMonth);
            String sep = sLunarService.getSeparationForType();
            String isSpecial = sLunarService.getIsSpecialFlag();

            // build the lunar cursor.
            for (int i = 1; i <= lunars.length; i++) {
                ArrayList<Object> row = new ArrayList<Object>(3);
                row.add(i);    // add the month day
                String[] lunarAndType = lunars[i - 1].split(sep);
                row.add(lunarAndType[0]);    // add the lunar info
                row.add(isSpecial.equals(lunarAndType[1]) ? 1 : 0);
                cursor.addRow(row);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Catch the RemoteException e:" + e);
            e.printStackTrace();
        }

        return new LunarCursor(cursor, this);
    }

    /**
     * Cursor data specifically for used by the {@link DateViews}.
     * For this, we could easily get the lunar info and if special.
     */
    static class LunarCursor extends CursorWrapper {
        private Cursor mCursor;
        private LunarLoader mLoader;

        public LunarCursor(Cursor cursor, LunarLoader loader) {
            super(cursor);
            mCursor = cursor;
            mLoader = loader;
        }

        /**
         * Get the year for this cursor.
         * @return the year number
         */
        public int getYear() {
            return mLoader.mYear;
        }

        /**
         * Get the month for this cursor.
         * @return the month number
         */
        public int getMonth() {
            return mLoader.mMonth;
        }

        /**
         * Get the day of month for the special position.
         * @return the day number
         */
        public int getDayOfMonth() {
            return mCursor.getInt(0);
        }

        /**
         * Get the lunar info for the special position.
         * @return the lunar info
         */
        public String getLunar() {
            return mCursor.getString(1);
        }

        /**
         * If special for the special position.
         * @return true if it is special, otherwise false.
         */
        public boolean isSpecial() {
            return mCursor.getInt(2) == 1 ? true : false;
        }
    }

}
