package com.android.settings.wifi;

import java.util.ArrayList;
import java.util.UUID;

import com.mediatek.settings.UtilsExt;
import com.mediatek.wifi.Utf8ByteLengthFilter;
import android.os.SystemProperties;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.CheckBox;
//import android.widget.EditText;
import android.widget.LinearLayout;
//import android.widget.Spinner;
import aurora.widget.*;
//import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.CustomAuroraActionBarItem;

import com.android.settings.R;
import com.mediatek.wifi.Utf8ByteLengthFilter;
import com.mediatek.settings.ext.AuroraIWifiApDialogExt;
import com.mediatek.settings.ext.IWifiApDialogExt;
import com.mediatek.settings.UtilsExt;
import android.util.Log;

public class AuroraWifiApFregment extends AuroraActivity implements OnClickListener,
TextWatcher,OnItemSelectedListener{
	private static final String TAG="AuroraWifiApFregment";
	
    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;
    /// M: max length for ssid and password @{
    private static final int AP_SSID_MAX_LENGTH_BYTES = 32;
    private static final int AP_PSW_MAX_LENGTH_BYTES=63;
    /// @}

    private AuroraEditText mSsid;
    private int mSecurityTypeIndex = OPEN_INDEX;
    private AuroraEditText mPassword;

    WifiConfiguration mWifiConfig;
    /// M: @{
    private int mChannel = 0;
    private int mChannelWidth = 0;
    private WifiManager mWifiManager;
    private Context mContext;
    private String[] mChannelList;
    private LinearLayout    mLinearLayout;
    private AuroraSpinner mSecurity;
    AuroraIWifiApDialogExt mExt;
    private AuroraSpinner mMaxConnSpinner;
    ///@}
    
    private AuroraActionBar mActionBar;
    private Button mSaveBtn;
    
    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }
    
    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = mSsid.getText().toString();
        /// M: get channel  @{
        config.channel = mChannel;
        config.channelWidth = mChannelWidth;
        /// @}

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, " ------onCreate ----");
		mContext=AuroraWifiApFregment.this;
		setAuroraContentView(R.layout.aurora_wifi_ap_activity, AuroraActionBar.Type.Normal);
		mActionBar=getAuroraActionBar();
        mActionBar.addItem(R.layout.aurora_actionbar_wifi_state, 0);
        mActionBar.setTitle(R.string.wifi_tether_configure_ap_text);
        final CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) mActionBar.getItem(0);
        View view = item.getItemView();
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
        mSaveBtn.setText(R.string.wifi_operator_save);
        mSaveBtn.setOnClickListener(this);
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		Bundle bundle = getIntent().getBundleExtra("bundle");
		if(bundle!=null){
			mWifiConfig= bundle.getParcelable("wifi_config");
		}else{
			  mWifiConfig = mWifiManager.getWifiApConfiguration();
		}
		
		 if (mWifiConfig != null) {
	            mSecurityTypeIndex = getSecurityTypeIndex(mWifiConfig);
	            Log.d(TAG, "  WifiConfig :: SSID="+mWifiConfig.SSID+"  SecurityTypeIndex="+mSecurityTypeIndex );
	    }
		
        mSecurity = (AuroraSpinner) findViewById(R.id.security);
        /// M: get plug in and set adapter for mSecurity @{
        mExt = UtilsExt.getAuroraWifiApDialogPlugin(mContext);
        mExt.setAdapter(mContext, mSecurity, R.array.wifi_ap_security);

        mMaxConnSpinner = ((AuroraSpinner) findViewById(R.id.max_connection_num));
        mMaxConnSpinner.setOnItemSelectedListener(this);
        /// @}

        //  setTitle(R.string.wifi_tether_configure_ap_text);
        findViewById(R.id.type).setVisibility(View.VISIBLE);
        mSsid = (AuroraEditText)findViewById(R.id.ssid);
        mPassword = (AuroraEditText) findViewById(R.id.password);

  //      setButton(BUTTON_SUBMIT, mContext.getString(R.string.wifi_save), mListener);
  //      setButton(DialogInterface.BUTTON_NEGATIVE,mContext.getString(R.string.wifi_cancel), mListener);

        if (mWifiConfig != null) {
            mSsid.setText(mWifiConfig.SSID);

            /// M: set selection
            mSecurity.setSelection(mExt.getSelection(mSecurityTypeIndex));

            if (mSecurityTypeIndex == WPA2_INDEX) {
                  mPassword.setText(mWifiConfig.preSharedKey);
            }
            ///M: get configured channel @{
            mChannel = mWifiConfig.channel;
            mChannelWidth = mWifiConfig.channelWidth;
            /// @}
        }

        /// M: init channel @{
        // disabled channel and bandwidth setting when hotspot is not enabled 
        /// M: WifiManager memory leak @{
        /// @}
        AuroraSpinner mChannelSpinner = ((AuroraSpinner) findViewById(R.id.channel));
        AuroraSpinner mChannelWidthSpinner = ((AuroraSpinner)findViewById(R.id.channel_width));

        if (SystemProperties.getInt("mediatek.wlan.channelselect", 0) == 0 || 
                mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_ENABLED) {
            mLinearLayout = (LinearLayout) findViewById(R.id.type);
            mLinearLayout.removeView(findViewById(R.id.channel_text));
            mLinearLayout.removeView(findViewById(R.id.width_text));
            mLinearLayout.removeView(findViewById(R.id.channel));
            mLinearLayout.removeView(findViewById(R.id.channel_width));
        } else {

            // temporarily remove channel bandwidth which is not supported
            mLinearLayout = (LinearLayout)findViewById(R.id.type);
            mLinearLayout.removeView(findViewById(R.id.width_text));
            mLinearLayout.removeView(findViewById(R.id.channel_width));
            // end

            ArrayList<String> mTmpChannelList = new ArrayList<String>();
            mTmpChannelList.add(mContext.getString(R.string.wifi_tether_auto_channel_text));
            for (String s : mWifiManager.getAccessPointPreferredChannels()) {
                mTmpChannelList.add(s);
            }

            mChannelList = (String[]) mTmpChannelList.toArray(new String[mTmpChannelList.size()]);

            if (mChannelList != null) {
                int i = 0;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, 
                    android.R.layout.simple_spinner_item, mChannelList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mChannelSpinner.setAdapter(adapter);
                if (mChannel != 0) {
                    for (i = 1; i < mChannelList.length; i++) {
                        if (mChannelList[i].equals(mChannel + "")) {
                            break;
                        }
                    }
                    if (i == mChannelList.length) {
                        i = 0;
                    }
                }

                mChannelSpinner.setSelection(i);
                mChannelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView parent, View view, int position, long id) {
                            try {
                                if (position == 0) {
                                    mChannel = 0;
                                } else {
                                    mChannel = Integer.parseInt(mChannelList[position]);
                            }
                            } catch (NumberFormatException e) {
                                // channel error
                                e.printStackTrace();
                            }
                        }
                        public void onNothingSelected(AdapterView parent) {
                            }
                });    
            }
            mChannelWidthSpinner.setSelection(mChannelWidth);
            if (mChannelWidthSpinner != null) {
                mChannelWidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                        public void onItemSelected(AdapterView parent, View view, int position, long id) { 
                            if (position == 0) {
                                mChannelWidth = 0;
                            } else {
                                mChannelWidth = 1;
                            }
                        }
                        public void onNothingSelected(AdapterView parent) {
                        }
               });
            }

        }

        //SSID max length must shorter than 32 bytes
        mSsid.setFilters(new InputFilter[] {
                    new Utf8ByteLengthFilter(AP_SSID_MAX_LENGTH_BYTES)});
        mPassword.setFilters(new InputFilter[]  {
                    new Utf8ByteLengthFilter(AP_PSW_MAX_LENGTH_BYTES)});

        ((AuroraButton) findViewById(R.id.reset_oob)).setOnClickListener(this);
        ((AuroraButton) findViewById(R.id.reset_oob)).setVisibility(View.GONE);
        
        int maxConnValue = System.getInt(mContext.getContentResolver(),System.WIFI_HOTSPOT_MAX_CLIENT_NUM,
                            System.WIFI_HOTSPOT_DEFAULT_CLIENT_NUM);
        mMaxConnSpinner.setSelection(maxConnValue - 1);
        /// @}
        mSsid.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        ((AuroraCheckBox) findViewById(R.id.show_password)).setOnClickListener(this);
        ((AuroraCheckBox) findViewById(R.id.show_password)).setVisibility(View.GONE);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mSecurity.setOnItemSelectedListener(this);
        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();
	}

    /*aurora, linchunhui 20150815, if save the password as null, the devices will reboot, begin*/
    private void validate() {
        if ((mSsid != null && mSsid.length() == 0) ||
                   ((mSecurityTypeIndex == WPA2_INDEX) && mPassword.length() < 8)) {
            mSaveBtn.setEnabled(false);
        } else {
            mSaveBtn.setEnabled(true);
        }
    }
    /*aurora, linchunhui 20150815, if save the password as null, the devices will reboot, end*/

    public void onClick(View view) {
        if (view.getId() == R.id.show_password) {
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((AuroraCheckBox) view).isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
        } else if (view.getId() == R.id.reset_oob) {
            String s = com.mediatek.custom.CustomProperties.getString(com.mediatek.custom.CustomProperties.MODULE_WLAN, 
                com.mediatek.custom.CustomProperties.SSID, 
                mContext.getString(com.android.internal.R.string.wifi_tether_configure_ssid_default));
            mSsid.setText(s);
            mSecurityTypeIndex = WPA2_INDEX;
            mSecurity.setSelection(mExt.getSelection(mSecurityTypeIndex));

            String randomUUID = UUID.randomUUID().toString();
            //first 12 chars from xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
            String randomPassword = randomUUID.substring(0, 8) + randomUUID.substring(9,13);
            mPassword.setText(randomPassword);
        }else if(view.getId() ==R.id.btn_save)
        {
            mWifiConfig = getConfig();
            if (mWifiConfig != null) {
            	Log.d(TAG, "  SSID="+mWifiConfig.SSID+"   SecurityType="+mSecurityTypeIndex + "  RESULT_OK="+RESULT_OK);
                /**
                 * if soft AP is stopped, bring up
                 * else restart with new config
                 * TODO: update config on a running access point when framework support is added
                 */
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    mWifiManager.setWifiApEnabled(null, false);
                    mWifiManager.setWifiApEnabled(mWifiConfig, true);
                } else {
                    mWifiManager.setWifiApConfiguration(mWifiConfig);
                }
                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                if (index == 0) {
                    Toast.makeText(this, R.string.security_not_set,Toast.LENGTH_LONG).show();
                }
Log.d(TAG,   "Taskid= "+getTaskId());  
                Intent mIntent=new Intent();  
                mIntent.putExtra("SSID", mWifiConfig.SSID);  
                mIntent.putExtra("index", index);
                setResult(RESULT_OK, mIntent);  
                finish();  
            }
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(mSecurity)) {
            ///M: set index after item is selected @{
            mSecurityTypeIndex = mExt.getSecurityType(position);
            Log.d(TAG,"mSecurityTypeIndex: " + mSecurityTypeIndex);
            /// @}
            showSecurityFields();
            validate();
        } else if (parent.equals(mMaxConnSpinner)) {
            int maxConnValue = position + 1;
            System.putInt(mContext.getContentResolver(),System.WIFI_HOTSPOT_MAX_CLIENT_NUM, maxConnValue);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        if (mSecurityTypeIndex == OPEN_INDEX) {
            findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }

    
    public void closeSpinnerDialog() {
        if (mSecurity != null && mSecurity.isPopupShowing()) {
            mSecurity.dismissPopup();
        }
    }
}
