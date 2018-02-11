/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.dashboard;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraListView;

import com.android.settings.AuroraAirplaneModeEnabler;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISettingsMiscExt;

import android.os.SystemProperties;

import java.util.List;

public class DashboardSummary extends Fragment {
    private static final String LOG_TAG = "DashboardSummary";
	private static final String CUSTOMIZE_ITEM_INDEX = "customize_item_index";
    private ISettingsMiscExt mExt;

    private LayoutInflater mLayoutInflater;
    private AuroraListView mDashboard;
    
    private DashBoardCatAdapter mAdapter;

    private AuroraAirplaneModeEnabler mAirplaneEnabler;
    
    private DashboardTileView mMobileNetworkView;
    
    private boolean mHasFingerPrints  = "FPC GOODIX".equals(SystemProperties.get("ro.gn.fingerprint.support", "no"));
    
    
    private static final int MSG_REBUILD_UI = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REBUILD_UI: {
                    final Context context = getActivity();
                    rebuildUI(context);
                } break;
            }
        }
    };

    private class HomePackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            rebuildUI(context);
        }
    }

    /*
     * Aurora linchunhui 20160307 add
     */
    private class AirPlaneModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                   if (null != mMobileNetworkView) {
                        if(AuroraAirplaneModeEnabler.isAirplaneModeOn(context)) {
                             mMobileNetworkView.getImageView().setAlpha(IMAGE_GRAY);
                             mMobileNetworkView.getRightView().setAlpha(IMAGE_GRAY);
                             mMobileNetworkView.getTitleTextView().setEnabled(false);
                             mMobileNetworkView.setEnabled(false);
                        } else {
                             mMobileNetworkView.getImageView().setAlpha(ORIGINAL_IMAGE);
                             mMobileNetworkView.getRightView().setAlpha(ORIGINAL_IMAGE);
                             mMobileNetworkView.getTitleTextView().setEnabled(true);
                             mMobileNetworkView.setEnabled(true);
                        }
                   }
            }
        }
    }
    private HomePackageReceiver mHomePackageReceiver = new HomePackageReceiver();
    private AirPlaneModeReceiver mAirPlaneModeReceiver = new AirPlaneModeReceiver();

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
//        sendRebuildUI();

        final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED); 
        filter.addDataScheme("package");
        getActivity().registerReceiver(mHomePackageReceiver, filter);
        
        // Aurora linchunhui 20160307 add
        final IntentFilter filter_airplane = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getActivity().registerReceiver(mAirPlaneModeReceiver, filter_airplane);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAirplaneEnabler != null){
        	mAirplaneEnabler.pause();
        	mAirplaneEnabler.destroy();
        }
        
        getActivity().unregisterReceiver(mHomePackageReceiver);
        getActivity().unregisterReceiver(mAirPlaneModeReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLayoutInflater = inflater;
        mExt = UtilsExt.getMiscPlugin(this.getActivity());

        final View rootView = inflater.inflate(R.layout.dashboard, container, false);
        mDashboard = (AuroraListView) rootView.findViewById(R.id.dashboard_container);
      Log.d(LOG_TAG, "onCreateView");
      sendRebuildUI();
        return rootView;
    }

    private static int[] mPreferenceBackgroundIndexs;
    private static int[] mPreferenceBackgroundRes;

    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;

    private void getFrameListBackground(Context context) {
        mPreferenceBackgroundRes = new int[FRAME_LIST_BACKGROUND_TOTAL];
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_NULL] = 0;
        TypedValue outValue = new TypedValue();

        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListTopBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListMiddleBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBottomBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_BOTTOM] = com.aurora.R.drawable.aurora_list_item_bg_bottom/*outValue.resourceId*/;

    }
    private void rebuildUI(Context context) {
        if (!isAdded()) {
            Log.w(LOG_TAG, "Cannot build the DashboardSummary UI yet as the Fragment is not added");
            return;
        }

        getFrameListBackground(context);

        long start = System.currentTimeMillis();
        final Resources res = getResources();

//        mDashboard.removeAllViews();
       
        List<DashboardCategory> categories =
                ((SettingsActivity) context).getDashboardCategories(true);

        final int count = categories.size();

        if(mAdapter == null){
        	mAdapter =  new DashBoardCatAdapter(categories, getActivity());
        }
        mDashboard.setAdapter(mAdapter);
        long delta = System.currentTimeMillis() - start;
        Log.d(LOG_TAG, "rebuildUI took: " + delta + " ms");
    }
    
    private static final int IMAGE_GRAY = 75;//30% of 0xff in transparent
    private static final int ORIGINAL_IMAGE = 255;

    private void updateTileView(Context context, Resources res, DashboardTile tile,
            ImageView tileIcon, TextView tileTextView, TextView statusTextView,DashboardTileView titleView) {

        if (tile.iconRes > 0) {
            tileIcon.setImageResource(tile.iconRes);
        } else {
            //TODO:: Ask HIll to re-do this part
            mExt.customizeDashboardTile(tile, null, null, tileIcon, 2);
        }
        
        if(tile.id == R.id.mobile_network){
        	if(AuroraAirplaneModeEnabler.isAirplaneModeOn(context)){
        		titleView.getImageView().setAlpha(IMAGE_GRAY);
        		titleView.getRightView().setAlpha(IMAGE_GRAY);
        		tileTextView.setEnabled(false);
        		titleView.setEnabled(false);
        	}
        	mMobileNetworkView = titleView;
        }
        if(tile.id == R.id.about_settings){
			try {
				Context remote = context.createPackageContext(
						"gn.com.android.update", Context.CONTEXT_IGNORE_SECURITY);
				SharedPreferences m = remote.getSharedPreferences(
						"gn.com.android.update_preferences",
						Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE
								| Context.MODE_MULTI_PROCESS);

				Boolean b = m.getBoolean("show_notify", false);
				if(b){
					titleView.setRightView(R.drawable.aurora_settings_right_update);
				}
			}catch(PackageManager.NameNotFoundException e){
	        	Log.d(LOG_TAG, "createPackageContext" + e.getMessage());
	        	Log.d(LOG_TAG, "createPackageContext",e);
	        }
        }
        
  
       
        ///M: feature replace sim to uim
        tileTextView.setText(mExt.customizeSimDisplayString(
            tile.getTitle(res).toString(), SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        if(tile.id == R.id.security_settings){
        	if(!mHasFingerPrints){
        		tileTextView.setText(getResources().getString(R.string.security));
        	}
        }
        CharSequence summary = tile.getSummary(res);
        if (!TextUtils.isEmpty(summary)) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(summary);
        } else {
            statusTextView.setVisibility(View.GONE);
        }
    }

    private void sendRebuildUI() {
        if (!mHandler.hasMessages(MSG_REBUILD_UI)) {
            mHandler.sendEmptyMessage(MSG_REBUILD_UI);
        }
    }
    
    class DashBoardCatAdapter extends BaseAdapter{
    	 List<DashboardCategory> categories;
    	 Context context;
    	public DashBoardCatAdapter( List<DashboardCategory> cates,Context cxt){
    		this.categories = cates;
    		context = cxt;
    	}
    	
    	@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return categories.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return categories.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View categoryView, ViewGroup parent) {
		            DashboardCategory category = categories.get(position);
		            final Resources res = getResources();
		            	categoryView = mLayoutInflater.inflate(R.layout.aurora_dashboard_category, mDashboard,
			                    false);
		            	TextView categoryLabel =  (TextView) categoryView.findViewById(R.id.category_title);
		            	ViewGroup categoryContent = (ViewGroup) categoryView.findViewById(R.id.category_content);
		            if(position == 0){
		            	categoryView.setPadding(categoryView.getPaddingLeft(), res.getDimensionPixelSize(R.dimen.dashboard_page_margin_top), 
		            			categoryView.getPaddingRight(), categoryView.getPaddingBottom());
		            }else if(position == categories.size()-1){
		            	categoryView.setPadding(categoryView.getPaddingLeft(), categoryView.getPaddingTop(), 
		            			categoryView.getPaddingRight(), res.getDimensionPixelSize(R.dimen.dashboard_page_margin_bottom));
		            }
		            
		            categoryLabel.setText(category.getTitle(res));

		                    

		            final int tilesCount = category.getTilesCount();

		            for (int i = 0; i < tilesCount; i++) {
		                DashboardTile tile = category.getTile(i);
		                DashboardTileView tileView   = new DashboardTileView(context);
		                if(tile.id == R.id.mobile_network){
		           		 TelephonyManager telephonyManager=(TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		           		if((telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY) ){
		           			tileView.setEnabled(false);
		           		}else{
		           			tileView.setEnabled(true);
		           		}
		           	}
//		                if(tile.id != R.id.airplane_mode){
//		                 tileView = new DashboardTileView(context);
//		                }else {
//		                	tileView = new DashboardTileView(context,true);
//		                	mAirplaneEnabler =  new AuroraAirplaneModeEnabler(context,tileView.getAuroraSwitch());
//		                	mAirplaneEnabler.resume();
//		                }
		                updateTileView(context, res, tile, tileView.getImageView(),
		                        tileView.getTitleTextView(), tileView.getStatusTextView(),tileView);
		                //飞行模式的时候，移动网络置成不可点击
		                if(mAirplaneEnabler != null &&mMobileNetworkView != null){
		                	mAirplaneEnabler.setMobileView(mMobileNetworkView);
		                }
		                //wolfu add
		                    int mres = 0;
		                    boolean showDivider = true;
						if (tilesCount == 1) {
							showDivider = false;
							mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL];
						} else {
							if (i == 0) {
								mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP];
							} else if (i == (tilesCount - 1)) {
								showDivider = false;
								mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE];
								categoryView.setElevation(6f);
							} else {
								mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE];
							}
						}
		                    tileView.setBackgroundResource(mres);
		                    tileView.showDivider(showDivider);
		                tileView.setTile(tile);

		                if(tile != null && tile.extras != null && tile.extras.containsKey(CUSTOMIZE_ITEM_INDEX)){
		                    int index = tile.extras.getInt(CUSTOMIZE_ITEM_INDEX, -1);
		                    categoryContent.addView(tileView, index);
		                } else {
		                	categoryContent.addView(tileView);
		                }
		            
		            }

		            // Add the category
//		            mDashboard.addView(categoryView);
			return categoryView;
		}
		
		class Holder {
			TextView categoryLabel ;
			ViewGroup categoryContent;
		}
    	
    }
    
    
    
}
