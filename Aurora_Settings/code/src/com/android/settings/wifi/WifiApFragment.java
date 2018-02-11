package com.android.settings.wifi;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraSpinner;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.widget.CustomAuroraActionBarItem;
import android.os.SystemProperties;
import com.android.settings.*;

/**
* CopyRright (c)2014-xxxx:                       
    * Project:                                                          
* Module ID:   
    * Comments:                                         
* JDK version used:      <jdk1.7>                             
* Namespace:           Settings.java                           
* Author：              penggangding            
* Create Date：  2014-09-01
* Modified By：                                          
* Modified Date:                                     
    * Why & What is modified    
* Version:                 v1.0                      
*/
public class WifiApFragment extends Fragment implements OnClickListener,
		TextWatcher,OnItemSelectedListener {
	private static final String TAG="WifiApFragment";
	
    private static final int OPEN_INDEX = 0;
    private static final int WPA_INDEX = 1;
    private static final int WPA2_INDEX = 2;

	private static final String PRODUCT=SystemProperties.get("ro.gn.iuniznvernumber");
	private static final String ONEPLUE="OnePlusOne";
	private static final String FIND7a="FIND7a";
	private static final int UPDATE_HANDLE_AP_MSG=1;
	
	public static WifiConfiguration wifiConfig;
	
	private AuroraActionBar mActionBar;
    private Context mContext;
    private View mLayView;
    private Button mSaveBtn;
    private TextView mSsidView;
    private AuroraEditText mPassword;
    private AuroraSpinner mSecuritySpinner;
    
    private WifiManager wifiManager;
//    private WifiConfiguration wifiConfig;
    
    private int mSecurityTypeIndex=-1;
    
    private CharSequence inputLimit;
    private CharSequence inputBefore;
    
    private boolean isFocused = false;
    /**
     * 
     */
    private Handler attiHandler=new Handler()
    {
    		 @Override
        public void handleMessage(Message msg) 
        {
            refreshApAttibute();    
            super.handleMessage(msg);
		}
	};
	
	
	/**
	 * 
	 * 构造方法 的描述
	 * Structure of the test object
	 * @param name
	 *
	 */
/*	private WifiConfiguration mWifiConfiguration;
	public WifiApFragment(WifiConfiguration mWifiConfiguration)
	{
		this.mWifiConfiguration=mWifiConfiguration;
	}*/
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		/**
		 * Get serialized data
		 */
/*		if(savedInstanceState != null){
			WifiConfiguration mWifiConfig = (WifiConfiguration)(savedInstanceState.getParcelable("key"));
			Log.d("Aurora", "  mWifiConfig  >>  "+ (mWifiConfig==null));
		}else{
			Log.d("Aurora", "  mWifiConfig  >>  is null");
		}*/
		mContext=getActivity();
		mActionBar=((AuroraActivity)mContext).getAuroraActionBar();
        mActionBar.addItem(R.layout.aurora_actionbar_wifi_state, 0);
        final CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) mActionBar.getItem(0);
        View view = item.getItemView();
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
        mSaveBtn.setText(R.string.wifi_operator_save);
        mSaveBtn.setOnClickListener(this);
//		mSaveBtn.setOnTouchListener(onTouchListener);
	}


	private OnTouchListener onTouchListener = new OnTouchListener() 
	{
		@Override
		public boolean onTouch(View view, MotionEvent event) 
		{
			if (view.getId() == R.id.btn_save) 
			{
				if (event.getAction() == MotionEvent.ACTION_UP) 
				{
					((Button) view).setTextColor(Color.parseColor("#000000"));
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) 
				{
					((Button) view).setTextColor(Color.parseColor("#0ec16f"));
				}
			}
			return false;
		}
	};

	/**
	 * Get choose to encrypt subscript
	 * @param numble
	 * @return index 
	 * @exception
	 * @author penggangding
	 * @Time
	 */
	private int getSecurityTypeIndex(WifiConfiguration wifiConfig)
	{
	    if(PRODUCT.contains(ONEPLUE) || PRODUCT.contains(FIND7a))
	    {
	    	if(wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK))
	    	{
	    		return WPA_INDEX;
	    	}else if(wifiConfig.allowedKeyManagement.get(6))
	    	{
	    		return WPA2_INDEX;
	    	}else
	    	{
	    		return OPEN_INDEX;
	    	}
	    }
	    if(wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK))
	    {
	    	return WPA_INDEX;
	    }else if(wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK))
	    {
	    	return WPA2_INDEX;
	    }else
	    {
	    	return OPEN_INDEX;
	    }
	}
	
	/**
	 *Get information about the current configuration settings
	 *@param numble
	 *@return WifiConfiguration
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private WifiConfiguration getConfig()
	{
		WifiConfiguration config=new WifiConfiguration();
		config.SSID=mSsidView.getText().toString();
		switch (mSecurityTypeIndex) {
		case OPEN_INDEX:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			break;
		case WPA_INDEX:
            config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            if(mPassword.length()!=0)
            {
            	String password=mPassword.getText().toString();
            	config.preSharedKey=password;
            }
			break;
		case WPA2_INDEX:
			if(PRODUCT.contains(ONEPLUE) || PRODUCT.contains(FIND7a))
			{
			    config.allowedKeyManagement.set(6);
			}else
			{
				config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
			}
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            if (mPassword.length()!=0) {
                String password = mPassword.getText().toString();
                config.preSharedKey = password;
            }
			break;
		default:
			break;
		}
		return config;
	}
	
	/**
	 * Update properties and show
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void refreshApAttibute()
	{
		
	}
	
	/**
	 * Show encrypted attributes
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void showSecurityFields()
	{
		if(mSecurityTypeIndex==OPEN_INDEX)
		{
            mLayView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
		}
		mLayView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
	}
	
	/**
	 * Verify the status of the Save button
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void validateSavaBtn()
	{
		if((mSsidView!=null && mSsidView.length()==0) || (TextUtils.isEmpty(mSsidView.getText().toString().trim())) || (((mSecurityTypeIndex == WPA_INDEX) || (mSecurityTypeIndex == WPA2_INDEX))&&
                mPassword.getText().toString().getBytes().length < 8))
		{
			mSaveBtn.setEnabled(false);
			mSaveBtn.setOnClickListener(null);
		}else
		{
			mSaveBtn.setEnabled(true);
			mSaveBtn.setOnClickListener(this);
		}
	}

	/**
	 * Obtain configuration information and saved to the system
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void sava()
	{
		wifiConfig=getConfig();
		
        /**
         * if soft AP is stopped, bring up
         * else restart with new config
         * TODO: update config on a running access point when framework support is added
         */
		
        if (wifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
            wifiManager.setWifiApEnabled(null, false);
            wifiManager.setWifiApEnabled(wifiConfig, true);
        } else {
            wifiManager.setWifiApConfiguration(wifiConfig);
        }
	}
	
	/**
	 * Initialization attribute display
	 *@param numble
	 *@return
	 *@exception 
	 *@author penggangding
	 *@Time
	 */
	private void initProperties(WifiConfiguration wifiConfig)
	{
    	mSsidView.setText(wifiConfig.SSID);
    	mSecuritySpinner.setSelection(mSecurityTypeIndex);
    	if(mSecurityTypeIndex==WPA_INDEX || mSecurityTypeIndex==WPA2_INDEX)
    	{
    		mPassword.setText(wifiConfig.preSharedKey);
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		mLayView=inflater.inflate(R.layout.wifi_ap_fragment, null);
		mLayView.findViewById(R.id.type).setVisibility(View.VISIBLE);
		mSsidView=(TextView)mLayView.findViewById(R.id.ssid);
        mSecuritySpinner = ((AuroraSpinner) mLayView.findViewById(R.id.security));
        mPassword = (AuroraEditText) mLayView.findViewById(R.id.password);
        mSsidView.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mSecuritySpinner.setOnItemSelectedListener(this);
        /**
         * Gets the default WIFI AP basic configuration information
         */
        wifiManager=(WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mSecurityTypeIndex = getSecurityTypeIndex(wifiConfig);
        if(wifiConfig!=null)
        {
            initProperties(wifiConfig);
        }else
        {
        	wifiConfig=wifiManager.getWifiApConfiguration();
            initProperties(wifiConfig);
        }
    	showSecurityFields();
    	validateSavaBtn();
		return mLayView;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}

	@Override
	public void onPause() 
	{
		super.onPause();
	}

	@Override
	public void onResume() 
	{
		super.onResume();
	}

	@Override
	public void afterTextChanged(Editable s) 
	{
    	validateSavaBtn();
	if(inputLimit.toString().getBytes().length > 32) {
		Toast.makeText(getActivity(),R.string.input_exceed_limit,Toast.LENGTH_SHORT).show();
		s.delete(inputLimit.length()-1, inputLimit.length());
	}

/*	if(null != mPassword) {
		mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					isFocused = true;
				} else {
					isFocused = false;
				}
			}
		});
	}
	if(isFocused) {
		if(inputLimit.toString().getBytes().length > 32) {
			s.delete(inputLimit.length()-1, inputLimit.length());
		}
		try {
			String inputAfter = s.toString();
			String inputSub = inputAfter.substring(inputBefore.length(), inputAfter.length());
			char[] inputSubChar = inputSub.toCharArray();
			int index = 0;
			boolean delete = false;
			for(int i = 0; i < inputSub.length(); i++) {
				index = inputSubChar[i];
				*//**
				 *  Password is only numeral
				 *//*
				if ((index >= 33 && index <= 57) || (index >= 65 && index <= 90) || (index >= 97 && index <= 122)) {
					continue;
				} else {
					delete = true;
					break;
				}
			}
			if(delete) {
				s.delete(inputBefore.length(), inputAfter.length());
				Toast.makeText(getActivity(), R.string.input_type_limit, Toast.LENGTH_SHORT).show();
				delete = false;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}	*/
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) 
	{
		inputLimit=s;
		inputBefore=s.toString();
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
	{
		
	}

	@Override
	public void onClick(View arg0) 
	{
		sava();
		((Activity) mContext).finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		 mSecurityTypeIndex=position;
		 showSecurityFields();
		 validateSavaBtn();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

}
