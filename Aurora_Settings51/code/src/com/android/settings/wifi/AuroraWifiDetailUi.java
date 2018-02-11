package com.android.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import com.android.settings.R;

public class AuroraWifiDetailUi extends AuroraActivity implements OnClickListener,WifiConfigUiBase{
	private static final String TAG="AuroraWifiDetailUi";
	private AuroraActionBar mActionBar;
    private View view;
    private Button btnState;
    private TextView titleView;
    private boolean mEdit;
    private AccessPoint mAccessPoint;
    private WifiManager mWifiManager;
    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private Context mContext;
    AuroraActivity mActivity;
    private WifiConfigController mController;
    private ActionStates actionState;
    private static final int MENU_ID_EDIT = Menu.FIRST;
    
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
    private WifiManager.ActionListener mConnectListener;
    
    private enum ActionStates
    {
    	UNASSIGNED,
    	SAVA,
    	FORGET,
    	CONNECT,
    	MODIFY
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setAuroraContentView(R.layout.wifi_dialog, AuroraActionBar.Type.Normal);
    	mContext = this;
    	mActivity = this;
        /*Aurora linchunhui 20160303 modify*/
        View scrollView = (View) findViewById(R.id.wifi_scrollview);
        final float scale = mContext.getResources().getDisplayMetrics().density;
        int px = (int) (16 * scale + 0.5f);
        scrollView.setPadding(px, 0, px, 0);

    	mActionBar = getAuroraActionBar();
    	mActionBar.addItem(AuroraActionBarItem.Type.Edit, MENU_ID_EDIT);
    	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        /*
    	mActionBar.addItem(R.layout.aurora_actionbar_wifi_state, 0);
    	view = (View)mActionBar.getItem(0).getItemView();
    	btnState = (Button) view.findViewById(R.id.btn_save);
    	btnState.setOnClickListener(this);
        */

    	if (mActionBar.getTitleView()!=null) {
               titleView = mActionBar.getTitleView();
        }

    	Bundle wifiInfoBundle = getIntent().getExtras();
    	if (wifiInfoBundle != null && wifiInfoBundle.containsKey(WifiSettings.KEY_ACCESS_POINT)) {
    	       mEdit = wifiInfoBundle.getBoolean(WifiSettings.KEY_CAN_EDIT);
    	       mAccessPoint = new AccessPoint(this, wifiInfoBundle.getBundle(WifiSettings.KEY_ACCESS_POINT));
        }
        
    	mSaveListener = new WifiManager.ActionListener() {
                           public void onSuccess() {
                                 mActivity.setResult(Activity.RESULT_OK);
                                 mActivity.finish();
                           }
                           public void onFailure(int reason) {
                                 mActivity.setResult(Activity.RESULT_OK);
                                 mActivity.finish();
                           }
        };
    	mForgetListener = new WifiManager.ActionListener() {
                           public void onSuccess() {
                                 CharSequence info = (CharSequence) mContext.getString(R.string.aurora_wifi_success_forget_message, mAccessPoint.ssid);
                                 mActivity.setResult(Activity.RESULT_OK);
                                 mActivity.finish();
                           }
                           public void onFailure(int reason) {
                           }
        };
    	mConnectListener = new WifiManager.ActionListener() {
                           public void onSuccess() {
                                 mActivity.setResult(Activity.RESULT_OK);
                                 mActivity.finish();
                           }
                           public void onFailure(int reason) {
                           }
        };
    	mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	mFilter = new IntentFilter();
    	mFilter.addAction("android.net.wifi.WIFI_INFOR_MODIFY");
    	mReceiver = new BroadcastReceiver() {
                           @Override
                           public void onReceive(Context context, Intent intent) {
                                 Log.d(TAG, "  action="+intent.getAction().toString());
                                 if (intent.getAction().equals("android.net.wifi.WIFI_INFOR_MODIFY")) {
                                       //mController.update();
                                       mActivity.finish(); //BUG14662 aurora linchunhui 修改IP后没有更新，直接返回WLAN列表 20150821
                                 }
                           }
        };
        mController = new WifiConfigController(this, getCurrentView(), mAccessPoint, mEdit);
        mController.enableSubmitIfAppropriate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContext.registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View mView) {
        /*
        switch (mView.getId()) {
        case R.id.btn_save:
                distributeEvent();
                break;
        default:
                break;
        }
        */
    }

    /*
     * aurora action bar 点击事件处理方法
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int arg0) {
			switch (arg0) {
			case MENU_ID_EDIT:
                                distributeEvent();
				break;
			default:
				break;
			}
		}
    };
	
    private void distributeEvent() {
        if(null != actionState) { //aurora linchunhui add for null pointer exception 20150924
		Log.d(TAG, " actionState="+actionState.toString());
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
    }
	
    public void modifyWifiAp() {
    	Intent intent = new Intent();
    	Bundle savedState=new Bundle();
    	savedState.putParcelable(AccessPoint.KEY_CONFIG,mAccessPoint.getConfig());
    	savedState.putParcelable(AccessPoint.KEY_SCANRESULT, mAccessPoint.mScanResult);
    	savedState.putParcelable(AccessPoint.KEY_WIFIINFO, mAccessPoint.getInfo());
        if (mAccessPoint.getState() != null) {
        	savedState.putString(AccessPoint.KEY_DETAILEDSTATE, mAccessPoint.getState().toString());
        }
        Bundle bundle=new Bundle();
        bundle.putBoolean(WifiSettings.KEY_CAN_EDIT, true);
        bundle.putBundle(WifiSettings.KEY_ACCESS_POINT, savedState);
        intent.putExtras(bundle);
        intent.setClass(AuroraWifiDetailUi.this, AuroraWifiModify.class);
    	startActivity(intent);
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

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	public WifiConfigController getController() {
		return mController;
	}

	@Override
	public boolean isEdit() {
		return mEdit;
	}
	
	
    public void setTitle(int id) {
    	mActionBar.setTitle(id);
    }
	
    public void setTitle(CharSequence title) {
		if (getLanguage()) 
		{
			final int maxTitleLength=(int)getResources().getDimension(R.dimen.aurora_action_bar_title_maxwidth);   // Pixels
			if (title.toString().length() > 10) 
			{
		    	title = TextUtils.ellipsize(title, titleView.getPaint(), maxTitleLength,
	    				TextUtils.TruncateAt.END);
			}
		}
        mActionBar.setTitle(title);
    }

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
    
	private void setBtnStateColorView(Button colbtn,boolean change)
	{
		//colbtn.setEnabled(change);
                 mActionBar.getItem(0).getItemView().setEnabled(change);
	}

	
	@Override
	public void setForgetButton() {
		forget();
	}

	@Override
	public void setModifyButton() {
		   //btnState.setText(R.string.wifi_operator_modify);
		   actionState=ActionStates.MODIFY;
	}


	@Override
	public void setCancelButton() {
	}

	
	@Override
	public void setSubmitButton() {
    	setBtnStateColorView(btnState, true); //linchunhui false -> true
        if (mAccessPoint == null) {
        	//btnState.setText(R.string.wifi_operator_save);
        	actionState=ActionStates.SAVA;
        } else {
        	//btnState.setText(R.string.wifi_operator_connect);
        	actionState=ActionStates.CONNECT;
        }
	}
	
	@Override
	public Button getSubmitButton() {
    	return btnState;
	}

	@Override
	public Button getForgetButton() {
		return btnState;
	}

	@Override
	public Button getCancelButton() {
		return null;
	}

	@Override
	public Button getModifyButton() {
		return btnState;
	}
}
