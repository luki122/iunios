package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import com.android.settings.R;
import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;
import org.xmlpull.v1.XmlPullParser;
import android.content.res.ColorStateList;

public class AuroraWifiModify extends AuroraActivity implements WifiConfigUiBase{

    private AuroraActionBar mActionBar;
    private WifiConfiguration mWifiConfig;
    private WifiConfigController mController;
    private WifiManager mWifiManager;
    private boolean mEdit;

    private AccessPoint mAccessPoint;
    private Button saveBtn;
    private TextView cancleBtn;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mConnectListener;
    private static final int MENU_ID_DONE = Menu.FIRST;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
                /*BUG14662 aurora linchunhui 不能保存修改后的代理，IP等 20150821 begin*/
		setAuroraContentView(R.layout.wifi_dialog, AuroraActionBar.Type.Normal);

                /*Aurora linchunhui 20160303 modify*/
		View scrollView = (View) findViewById(R.id.wifi_scrollview);
		final float scale = getResources().getDisplayMetrics().density;
		int px = (int) (16 * scale + 0.5f);
		scrollView.setPadding(px, 0, px, 0);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mActionBar = ((AuroraActivity) this).getAuroraActionBar();
		//mActionBar.addItem(AuroraActionBarItem.Type.Done, MENU_ID_DONE);
		//mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);

                
                mActionBar.addItem(R.layout.aurora_actionbar_wifi_state, 0);
                View view = (View)mActionBar.getItem(0).getItemView();
                saveBtn = (Button) view.findViewById(R.id.btn_save);
                saveBtn.setText(R.string.wifi_operator_save);
                saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				Intent intent = new Intent("android.net.wifi.WIFI_INFOR_MODIFY");
				sendBroadcast(intent);
				onBackPressed();
			}
		});
                
                if (mActionBar.getTitleView() != null) {
        	     cancleBtn = mActionBar.getTitleView();
                     //cancleBtn.setTextColor(R.color.black);
                     XmlPullParser xrp = getResources().getXml(R.color.aurora_action_bar_save_color);  
                     try {  
                          ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);  
                          //cancleBtn.setTextColor(csl);  
                     } catch (Exception e) {  
                     }  
                     cancleBtn.setTextSize(16);
                     cancleBtn.setEnabled(true);
                     mActionBar.setTitle(R.string.wifi_modify);
		     cancleBtn.setOnClickListener(new OnClickListener() {
			  @Override
			  public void onClick(View arg0) {
                              onBackPressed();
			  }
		     });
                }
                /*BUG14662 aurora linchunhui 不能保存修改后的代理，IP等 20150821 end*/
		mSaveListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}
			public void onFailure(int reason) {
			}
		};
		mConnectListener = new WifiManager.ActionListener() {
			public void onSuccess() {
				finish();
			}
			public void onFailure(int reason) {
			}
		};
		Bundle wifiInfoBundle = getIntent().getExtras();
		if (wifiInfoBundle != null) {
		      mEdit = wifiInfoBundle.getBoolean(WifiSettings.KEY_CAN_EDIT);
		      if(wifiInfoBundle.getBundle(WifiSettings.KEY_ACCESS_POINT)!=null){
		           mAccessPoint=new AccessPoint(this, wifiInfoBundle.getBundle(WifiSettings.KEY_ACCESS_POINT));
		      }
		}
		if(mAccessPoint==null){
		      return ;
		}
		 mController = new WifiConfigController(this, getCurrentView(), mAccessPoint, mEdit);
		 saveBtn.setEnabled(mController.canEnableSubmitIfAppropriate());
		 //mActionBar.getItem(0).getItemView().setEnabled(mController.canEnableSubmitIfAppropriate());chunhui
		 //mActionBar.getItem(0).getItemView().setAlpha(0); 
	}

    /*
     * aurora action bar 点击事件处理方法
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int arg0) {
			switch (arg0) {
			case MENU_ID_DONE:
                                mController.canEnableSubmitIfAppropriate(); // Aurora linchunhui 20160226 modify for BUG-19790
                                save();
				Intent intent = new Intent("android.net.wifi.WIFI_INFOR_MODIFY");
				sendBroadcast(intent);
				onBackPressed();
				break;
			default:
				break;
			}
		}
     };
	
	public void save() {
		mWifiConfig = mController.getConfig();
		if (mWifiConfig != null) {
			mWifiManager.save(mWifiConfig, mSaveListener);
			if(mWifiConfig.networkId != INVALID_NETWORK_ID)
			{
				mWifiManager.connect(mWifiConfig.networkId, mConnectListener);
			}
		}
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public WifiConfigController getController() {
		return mController;
	}

	@Override
	public boolean isEdit() {
		return false;
	}

	@Override
	public Button getSubmitButton() {
		return saveBtn; //BUG14662 aurora linchunhui 不能保存修改后的代理，IP等 20150821
	}

	@Override
	public Button getForgetButton() {
		return null;
	}

	@Override
	public Button getCancelButton() {
		return null;
	}


	@Override
	public void setSubmitButton() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setForgetButton() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCancelButton() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setModifyButton() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Button getModifyButton() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
