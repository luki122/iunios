/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import com.android.settings.R;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import com.android.settings.*;
import android.security.Credentials;
import android.security.KeyStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import aurora.app.AuroraActivity;
import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraButton;
// penggangding begin 08-06
import android.view.View.OnClickListener;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;
// penggangding end 08-06
public class WifiInfoActivity extends AuroraActivity implements WifiConfigUiBase {

	public static final String TAG = "WifiInfo";

    private boolean mEdit = true;
    private AccessPoint mAccessPoint = null;

    private WifiConfigController mController;

    private AuroraActivity mActivity;
    private WifiInforModify mwifInforModify;
    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    private WifiManager mWifiManager;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
    private WifiManager.ActionListener mConnectListener;

    private AuroraActionBar mActionBar;
    private static final int ITEM_ID_ACTION_SAVE = 1;
    private static final int ITEM_ID_ACTION_FORGET = 2;
    private static final int ITEM_ID_ACTION_CONNECT = 3;
    private static final int ITEM_ID_ACTION_MODIFY = 4;
    
//    penggangding begin 08-04
    private ActionStates actionState;
    private View view;
    private Button btnState;
    private TextView titleView;
    private enum ActionStates
    {
    	UNASSIGNED,
    	SAVA,
    	FORGET,
    	CONNECT,
    	MODIFY
    }
    
	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.d("gd","  ActionStates:"+actionState);
			if(actionState==ActionStates.SAVA)
			{
				save();
			}else if(actionState==ActionStates.FORGET)
			{
				forget();
			}else if(actionState==ActionStates.CONNECT)
			{
				connect();
			}else if(actionState==ActionStates.MODIFY)
			{
				modifyWifiAp();
			}
		}
	};
	
	private void setBtnStateColorView(Button colbtn,boolean change)
	{
		colbtn.setEnabled(change);
//		if(change)
//		{
//			colbtn.setTextColor(Color.parseColor("#000000"));
//		}else
//		{
//			colbtn.setTextColor(Color.parseColor("#BFBFBF"));
//		}
	}
	
//    private OnTouchListener onTouchListener = new OnTouchListener() {
//		@Override
//		public boolean onTouch(View view, MotionEvent event) {
//			if(view.getId() == R.id.btn_save)
//			{
//				if(event.getAction() == MotionEvent.ACTION_UP)
//				{
//					((Button)view).setTextColor(Color.parseColor("#000000"));
//				}
//				if(event.getAction() == MotionEvent.ACTION_DOWN)
//				{
//					((Button)view).setTextColor(Color.parseColor("#019c73"));
//				}
//			}
//			return false;
//		}
//	};
//    penggangding  end 08-04
    
    
/*    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case ITEM_ID_ACTION_SAVE:
                save();
                break;
            case ITEM_ID_ACTION_FORGET:
                forget();
                break;
            case ITEM_ID_ACTION_CONNECT:
                connect();
                break;
            case ITEM_ID_ACTION_MODIFY:
            	modifyWifiAp();
                break;
            default:
                break;
            }
            //mActivity.setResult(Activity.RESULT_OK);
            //mActivity.finish();
        }
    };*/

    @Override
    public WifiConfigController getController() {
        return mController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this;
//      penggangding begin 08-04       
        setAuroraContentView(R.layout.aurora_wifi_info_layout, AuroraActionBar.Type.Normal);
//      penggangding  end 08-04
        Bundle wifiInfoBundle = getIntent().getExtras();
        if (wifiInfoBundle != null && wifiInfoBundle.containsKey(WifiSettings.KEY_ACCESS_POINT)) {
        	mEdit = wifiInfoBundle.getBoolean(WifiSettings.KEY_CAN_EDIT);
            mAccessPoint = new AccessPoint(this, wifiInfoBundle.getBundle(WifiSettings.KEY_ACCESS_POINT));
        }

        mActionBar = getAuroraActionBar();
//      penggangding begin 08-04
//      mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        actionState = ActionStates.UNASSIGNED;
    	mActionBar.addItem(R.layout.aurora_actionbar_wifi_state, 0);
        view=(View)mActionBar.getItem(0).getItemView();
        btnState=(Button) view.findViewById(R.id.btn_save);
        if(mActionBar.getTitleView()!=null)
        {
        	titleView=mActionBar.getTitleView();
        }
        btnState.setOnClickListener(listener);
//        btnState.setOnTouchListener(onTouchListener);
//      penggangding  end 08-04

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mSaveListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            	mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
            public void onFailure(int reason) {
                Toast.makeText(mContext, R.string.wifi_failed_save_message, Toast.LENGTH_SHORT).show();
                mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
        };
        mForgetListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            	CharSequence info = (CharSequence) mContext.getString(R.string.aurora_wifi_success_forget_message,
            			mAccessPoint.ssid);
            	Toast.makeText(mContext, info, Toast.LENGTH_SHORT).show();
            	mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
            public void onFailure(int reason) {
                Toast.makeText(mContext, R.string.wifi_failed_forget_message, Toast.LENGTH_SHORT).show();
            }
        };
        mConnectListener = new WifiManager.ActionListener() {
            public void onSuccess() {
            	mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
            public void onFailure(int reason) {
                Toast.makeText(mContext, R.string.wifi_failed_connect_message, Toast.LENGTH_SHORT).show();
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction("android.net.wifi.WIFI_INFOR_MODIFY");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	if(intent.getAction().equals("android.net.wifi.WIFI_INFOR_MODIFY")) {
            		mController.update();
            	}
            }
        };
        mController = new WifiConfigController(this, getCurrentView(), mAccessPoint, mEdit);
        mController.enableSubmitIfAppropriate();
        getSubmitButton();
    }
    
//  penggangding begin 08-06
    private boolean getLanguage()
    {
    	Resources resources = getResources();
    	Configuration configLan = resources.getConfiguration();
    	if(configLan.locale.toString().equals("en_US"))
    	{
    		return true;
    	}
    	return false;
    }
//  penggangding end 08-06
    
    @Override
	public void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mFilter);
//		mController.update();
	}
    
    @Override
    public void onDestroy() {
        super.onDestroy(); 
        unregisterReceiver(mReceiver);
    }

    public Context getContext() {
        return this;
    }

    public LayoutInflater getLayoutInflater() {
    	return (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTitle(int id) {
    	mActionBar.setTitle(id);
    }

    public void setTitle(CharSequence title) {
//    	penggangding begin 08-06
		if (getLanguage()) 
		{
			final int maxTitleLength=(int)getResources().getDimension(R.dimen.aurora_action_bar_title_maxwidth);   // Pixels
			if (title.toString().length() > 10) 
			{
		    	title = TextUtils.ellipsize(title, titleView.getPaint(), maxTitleLength,
	    				TextUtils.TruncateAt.END);
			}
		}
//		penggangding end 08-06
        mActionBar.setTitle(title);
    }

    @Override
    public boolean isEdit() {
        return mEdit;
    }

    @Override
/*	// Aurora liugj 2013-10-24 modified for aurora's new feature start
    public Button getSubmitButton() {
	// Aurora liugj 2013-10-24 modified for aurora's new feature end
        if (mAccessPoint != null && mAccessPoint.networkId != INVALID_NETWORK_ID) return null;
        boolean enabled = mController.canEnableSubmitIfAppropriate();
        if (mAccessPoint == null) {
        	//mActionBar.setDisplayOptions(ITEM_ID_ACTION_SAVE, enabled);
        	ImageButton saveItemView = (ImageButton) mActionBar.getItem(0).getItemView();
            if (enabled) {
            	saveItemView.setImageResource(R.drawable.aurora_wifi_save);
            	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
            } else {
            	saveItemView.setImageResource(R.drawable.aurora_wifi_save_unable);
            	mActionBar.setOnAuroraActionBarListener(null);
            }
        } else {
            //mActionBar.setDisplayOptions(ITEM_ID_ACTION_CONNECT, enabled);
        	ImageButton connectItemView = (ImageButton) mActionBar.getItem(0).getItemView();
            if (enabled) {
            	connectItemView.setImageResource(R.drawable.aurora_wifi_connect);
            	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
            } else {
            	connectItemView.setImageResource(R.drawable.aurora_wifi_connect_unable);
            	mActionBar.setOnAuroraActionBarListener(null);
            }
        }
    	return null;
    }*/
    
    public Button getSubmitButton()
    {
    	if(mAccessPoint!=null && mAccessPoint.networkId!=INVALID_NETWORK_ID)return null;
    	boolean enabled=mController.canEnableSubmitIfAppropriate();
    	setBtnStateColorView(btnState,enabled);
    	if(mAccessPoint==null)
    	{
    		if(enabled)
    		{
    			actionState=ActionStates.SAVA;
    		}else
    		{
    			actionState=ActionStates.UNASSIGNED;
    		}
    	}else
    	{
    		if(enabled)
    		{
    			actionState=ActionStates.CONNECT;
    		}else
    		{
    			actionState=ActionStates.UNASSIGNED;
    		}
    	}
    	return null;
    }

    // Aurora liugj 2013-10-24 modified for aurora's new feature start
    @Override
    public Button getForgetButton() {
    	return null;
    }

    @Override
    public Button getCancelButton() {;
    	return null;
    }
	// Aurora liugj 2013-10-24 modified for aurora's new feature end
    
//  penggangding begin 08-04
    @Override
    public void setSubmitButton(CharSequence text) {
    	setBtnStateColorView(btnState,false);
        if (mAccessPoint == null) {
        	btnState.setText(R.string.wifi_operator_save);
        } else {
        	btnState.setText(R.string.wifi_operator_connect);
        }
    }

    @Override
    public void setForgetButton(CharSequence text) {
    	btnState.setText(R.string.wifi_operator_forget);
        actionState=ActionStates.FORGET;
    }
//  penggangding begin 08-05
    @Override
    public void setModifyButton(CharSequence text) {
        btnState.setText(R.string.wifi_operator_modify);
        
    	SharedPreferences sharedPreferences=WifiSettings.getWifiSettingsInstance().getPrefrerence();
    	final int state = sharedPreferences.getInt(mAccessPoint.ssid, 0);
    	Log.d("gd","modify state:"+state);
    	if(state == 2)
    	{
//    		mActionBar.addItem(R.drawable.aurora_wifi_modify_unable, 0, getString(R.string.wifi_modify));
    		setBtnStateColorView(btnState,true);
    		actionState=ActionStates.MODIFY;
    		return;
    	}
    	if(state == 1)
    	{
    		final DetailedState disableState = mAccessPoint.getState();
            final WifiConfiguration configuration=mAccessPoint.getConfig();
    		if(configuration!=null)
    		{
    			if(disableState==null)
    			{
//    				mActionBar.addItem(R.drawable.aurora_wifi_modify, ITEM_ID_ACTION_MODIFY, getString(R.string.wifi_modify));
    				actionState=ActionStates.MODIFY;
    				setBtnStateColorView(btnState,true);
    			}else
    			{
    				if(disableState.toString().equals("DISCONNECTED")
    					|| disableState.toString().equals("FAILED")
    					|| disableState.toString().equals("BLOCKED")
    					|| disableState.toString().equals("SUSPENDED")
    					|| disableState.toString().equals("VERIFYING_POOR_LINK"))
    			{
        			actionState=ActionStates.MODIFY;
        			setBtnStateColorView(btnState,true);
    			}else
    			{
    				setBtnStateColorView(btnState,false);
    				}
    			}
    		}
        }else
        {
        	btnState.setVisibility(View.GONE);
        }
    	//IUNI <penggangding><2014-06-17> added for end
    }
//  penggangding end 08-05
//  penggangding end 08-04
    
    @Override
    public void setForgetCommit() {
    	forget();
    }
    
    public void modifyWifiAp() {
    	Intent intent = new Intent();
    	intent.setClass(WifiInfoActivity.this, WifiInforModify.class);
    	mwifInforModify = new WifiInforModify(mAccessPoint);
    	startActivity(intent);
    }

    @Override
    public void setCancelButton(CharSequence text) {

    }

    /* package */ void submit() {

        final WifiConfiguration config = mController.getConfig();

        if (config == null) {
            if (mAccessPoint != null
                    && !requireKeyStore(mAccessPoint.getConfig())
                    && mAccessPoint.networkId != INVALID_NETWORK_ID) {
                mWifiManager.connect(mAccessPoint.networkId,
                        mConnectListener);
            }
        } else if (config.networkId != INVALID_NETWORK_ID) {
            if (mAccessPoint != null) {
                mWifiManager.save(config, mSaveListener);
            }
        } else {
            if (mController.isEdit() || requireKeyStore(config)) {
                mWifiManager.save(config, mSaveListener);
            } else {
                mWifiManager.connect(config, mConnectListener);
            }
        }
    }

    /* package */ void connect() {
        final WifiConfiguration config = mController.getConfig();

        if (mAccessPoint != null && mAccessPoint.networkId == INVALID_NETWORK_ID) {
            mWifiManager.connect(config, mConnectListener);
        }
    }

    /* package */ void save() {
        final WifiConfiguration config = mController.getConfig();

        if (mAccessPoint == null) {
            mWifiManager.save(config, mSaveListener);
        }
    }

    /* package */ void forget() {
        if (mAccessPoint == null || mAccessPoint.networkId == INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e(TAG, "Failed to forget invalid network " + mAccessPoint.getConfig());
            return;
        }
        
         if(Build.MODEL.contains("SM-N9008"))
         {
        	 mWifiManager.disableNetwork(mAccessPoint.networkId);
        	 finish();
        	 return;
         }
         mWifiManager.forget(mAccessPoint.networkId, mForgetListener);
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiConfigController.requireKeyStore(config) &&
                KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
            Credentials.getInstance().unlock(this);
            return true;
        }
        return false;
    }
}
