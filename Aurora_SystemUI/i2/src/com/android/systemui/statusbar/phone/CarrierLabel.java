/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.Xlog;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class CarrierLabel extends TextView {
    private static final String TAG = "CarrierLabel";
    private boolean mAttached;
    private String mNetworkNameSeparator;
    
    
    // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
    private static final int ORIGIN_CARRIER_NAME_ID
                                 = R.array.origin_carrier_names;
    private static final int LOCALE_CARRIER_NAME_ID
                                 = R.array.locale_carrier_names;
    private String getLocaleString(String c) {
        return getLocalString(
                   c, ORIGIN_CARRIER_NAME_ID, LOCALE_CARRIER_NAME_ID);
    }
    private final String getLocalString(String originalString, int originNamesId, int localNamesId) {
        return getLocalString(originalString, "com.android.systemui", originNamesId, localNamesId);
    }
    private final String getLocalString(String originalString, String defPackage,
            int originNamesId, int localNamesId) {
        String[] origNames = getContext().getResources().getStringArray(originNamesId);
        String[] localNames = getContext().getResources().getStringArray(localNamesId);
        for (int i = 0; i < origNames.length; i++) {
            if (origNames[i].equalsIgnoreCase(originalString)) {
                return getContext().getString(getContext().getResources().getIdentifier(localNames[i], "string", defPackage));
            }
        }
        return originalString;
    }
 // Aurora <tongyh> <2014-04-09> add CarrierLabel end

    public CarrierLabel(Context context) {
        this(context, null);
    }

    public CarrierLabel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarrierLabel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateNetworkName(false, null, false, null);
        mNetworkNameSeparator = getContext().getString(R.string.status_bar_network_name_separator);
    }

    void updateNetworkName(boolean showSpn, String spn, boolean showPlmn, String plmn) {
		// Steve.Tang 2014-07-23 change carrier label as air mode change. start
        this.updateNetworkName(showSpn, spn, showPlmn, plmn, false);
    }

    void updateNetworkName(boolean showSpn, String spn, boolean showPlmn, String plmn, boolean isAirModeOn) {
        Xlog.d(TAG, "updateNetworkName, showSpn=" + showSpn + " spn=" + spn + " showPlmn=" + showPlmn + " plmn=" + plmn);
		if(isAirModeOn){
			setText(R.string.lockscreen_airplane_mode_on);//tymy_20150429_bug13090 setText(com.aurora.R.string.lockscreen_carrier_default);
			return;
		}
		// Steve.Tang 2014-07-23 change carrier label as air mode change. end
        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && plmn != null) {
            // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
//            str.append(plmn);
        	str.append(getLocaleString(plmn));
            // Aurora <tongyh> <2014-04-09> add CarrierLabel end
            something = true;
        }

		// Aurora <Steve.Tang> 2014-11-28 only show one info,if show plmn, do not show spn. start
		if(showPlmn && plmn != null){
			showSpn = false;
		}
		// Aurora <Steve.Tang> 2014-11-28 only show one info,if show plmn, do not show spn. end

        if (showSpn && spn != null) {
            if (something) {
                str.append(mNetworkNameSeparator);
            }
            // Aurora <tongyh> <2014-04-09> add CarrierLabel begin
//            str.append(spn);
            str.append(getLocaleString(spn));
            // Aurora <tongyh> <2014-04-09> add CarrierLabel end
            something = true;
        }
        if (something) {
            setText(str.toString());
        } else {
            setText(com.aurora.R.string.lockscreen_carrier_default);
        }
    }
}
