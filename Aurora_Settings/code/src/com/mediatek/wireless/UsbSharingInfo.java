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

package com.mediatek.wireless;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
// Gionee <wangyaohui><2013-05028> add for CR00820266 begin
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.Context;
import android.net.ConnectivityManager;

// Aurora <likai> <2013-10-29> modify begin
//import com.mediatek.xlog.Xlog;
import android.util.Log;
// Aurora <likai> <2013-10-29> modify end

// Gionee <wangyaohui><2013-05028> add for CR00820266 end

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
// Gionee <wangyaohui><2013-05028> modify for CR00820266 begin
// public class UsbSharingInfo extends SettingsPreferenceFragment {
public class UsbSharingInfo extends SettingsPreferenceFragment implements Button.OnClickListener {
// Gionee <wangyaohui><2013-05028> modify for CR00820266 end
    public static final String TAG = "UsbSharingInfo";
    private static final int MENU_OK = Menu.FIRST;
    // Gionee <wangyaohui><2013-05028> add for CR00820266 begin
	private static final int WIN_XP = 1;
    private static final int WIN_VISTA = 2;
    private static final int WIN_SEVEN = 3;
    private static final int WIN_EIGHT = 4;

    private Button mBackBtn;
    private Button mNextBtn;
    private int mSelectedSystemIndex;
    private ConnectivityManager mConnectivityManager;
	// Gionee <wangyaohui><2013-05028> add for CR00820266 end

    public UsbSharingInfo() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        // Gionee <wangyaohui><2013-05028> add for CR00820266 begin
		mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        Bundle bundle = this.getArguments();
        Log.d(TAG, "onCreate activity,bundle = " + bundle + ",this = " + this);

        if (bundle != null) {
            mSelectedSystemIndex = bundle.getInt(UsbSharingChoose.SYSTEM_TYPE);
        }
        Log.d(TAG, "index is " + mSelectedSystemIndex);
		// Gionee <wangyaohui><2013-05028> add for CR00820266 end
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Gionee <wangyaohui><2013-05028> modify for CR00820266 begin
        // return getActivity().getLayoutInflater().inflate(R.layout.usb_sharing_info, null);
		View view = getActivity().getLayoutInflater().inflate(R.layout.usb_sharing_info, null);
        LinearLayout mProgressbarLayout = (LinearLayout) view.findViewById(R.id.progressbar_layout);
        mBackBtn = (Button) view.findViewById(R.id.panel_button_back);
        mBackBtn.setOnClickListener(this);
        mNextBtn = (Button) view.findViewById(R.id.panel_button_next);
        mNextBtn.setText(R.string.wifi_display_options_done);
        mNextBtn.setOnClickListener(this);
        
        ImageView child = (ImageView) mProgressbarLayout.getChildAt(1);
        child.setImageResource(R.drawable.progress_radio_on); 
        return view;
		// Gionee <wangyaohui><2013-05028> modify for CR00820266 end
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.add(0, MENU_OK, 0, android.R.string.ok);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == MENU_OK) {
            finishFragment();
        }
        return super.onOptionsItemSelected(item);
    }
	// Gionee <wangyaohui><2013-05028> add for CR00820266 begin
	@Override
    public void onClick(View v) {
        if (v == mNextBtn) {
            //set the value to framework
            if (mSelectedSystemIndex == WIN_XP || mSelectedSystemIndex == WIN_VISTA) {
//                mConnectivityManager.setUsbInternet(true, ConnectivityManager.USB_INTERNET_SYSTEM_WINXP);
            } else if (mSelectedSystemIndex == WIN_SEVEN || mSelectedSystemIndex == WIN_EIGHT) {
//                mConnectivityManager.setUsbInternet(true, ConnectivityManager.USB_INTERNET_SYSTEM_WIN7);
            }
        } else if (v == mBackBtn) {
            Bundle bundle = new Bundle();
            bundle.putInt(UsbSharingChoose.SYSTEM_TYPE, mSelectedSystemIndex);
            startFragment(this, UsbSharingChoose.class.getName(), 0, bundle, R.string.usb_sharing_title);
            getActivity().overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
        }
        finishFragment();
    }
	// Gionee <wangyaohui><2013-05028> add for CR00820266 end
}
