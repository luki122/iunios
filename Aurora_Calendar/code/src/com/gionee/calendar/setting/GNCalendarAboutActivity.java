//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 begin

package com.gionee.calendar.setting;

import java.text.DecimalFormat;

import com.android.calendar.CalendarApplication;
import com.android.calendar.R;
import com.android.calendar.extensions.AboutPreferences;
import com.gionee.calendar.statistics.Statistics;

import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class GNCalendarAboutActivity extends AuroraActivity{
	
	private AuroraButton mAboutUpdateButton;
	
	
	private View customView;
	private RelativeLayout backButton;

	private TextView  actionTitle;
	private View addAcount;
	private TextView  aboutVersion;

	protected void onCreate(Bundle savedInstanceState) {

//		switch (NoteApplication.thremeType) {
//
//		case NoteApplication.THEME_TYPE_WHITE:
//			setTheme(android.R.style.Theme_GioneeView_Light);
//			break;
//		case NoteApplication.THEME_TYPE_BLACK:
//		default:
//			// setTheme(R.style.Theme_AndroidDevelopers);
//			break;
//
//		}
//		CommonUtils.setTheme(this);
		
		super.onCreate(savedInstanceState);
//		initActionBar();

		
//		setContentView(R.layout.gn_about_activity_layout);
		setAuroraContentView(R.layout.gn_about_activity_layout,
                AuroraActionBar.Type.Normal);

		initResources();
		initViews();
		setTitle();
		
		initActionBar();
	}
	
	private ImageView mTengxunLogo;
	private ImageView mChineseLunarLogo;
	private void initViews(){
		mTengxunLogo = (ImageView) findViewById(R.id.tengxun_logo);
		mTengxunLogo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    Uri uri = Uri.parse("http://astro.lady.qq.com/");  
			    Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
			    intent.addCategory(Intent.CATEGORY_BROWSABLE);
			    intent.setPackage("com.android.browser");
			    startActivity(intent);
			}
		});
		mChineseLunarLogo = (ImageView) findViewById(R.id.chinese_lunar_calendar_logo);
		mChineseLunarLogo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    Uri uri = Uri.parse("http://www.nongli.com/");  
			    Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
			    intent.addCategory(Intent.CATEGORY_BROWSABLE);
			    intent.setPackage("com.android.browser");
			    startActivity(intent);
			}
		});
	}
	
	private void initActionBar() {
		// TODO Auto-generated method stub
    	AuroraActionBar bar = getAuroraActionBar();
		if (bar != null) {
			bar.setTitle(R.string.menu_about_preferences);
//			bar.setIcon(R.drawable.gn_event_actionbar_icon);
//            bar.setDisplayHomeAsUpEnabled(false);
//            bar.setDisplayShowTitleEnabled(false);
//            bar.setDisplayShowHomeEnabled(false);
//            bar.setDisplayShowCustomEnabled(true);
//    		customView = LayoutInflater.from(bar.getThemedContext())
//    				.inflate(R.layout.gn_calendar_setting_activity_action_bar_custom, null);
//    		 bar.setCustomView(customView, new AuroraActionBar.LayoutParams(
//    				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//    		 backButton=(RelativeLayout)customView.findViewById(R.id.gn_setting_actionbar_back);
//    		 addAcount=customView.findViewById(R.id.actionbar_add_acount);
//    		 addAcount.setVisibility(View.GONE);
//     		actionTitle=(TextView)customView.findViewById(R.id.actionbar_title);
//     		
//     		actionTitle.setText(R.string.menu_about_preferences);
//     		
//     		backButton.setOnClickListener(new OnClickListener() {
// 				
// 				@Override
// 				public void onClick(View v) {
// 					// TODO Auto-generated method stub
// 					finish();
// 				}
// 			});
//     		
		}
    }
	
	
	

	private void initResources() {

		mAboutUpdateButton = (AuroraButton) findViewById(R.id.about_update_button);
		
		mAboutUpdateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
		        if (CalendarApplication.isEnableUpgrade()) {
		            Intent checkIntent = new Intent("android.intent.action.GN_APP_UPGRADE_CHECK_VERSION");
		            checkIntent.putExtra("package", GNCalendarAboutActivity.this.getApplicationContext().getPackageName());
		            checkIntent.putExtra("isauto", false);
		            GNCalendarAboutActivity.this.startService(checkIntent);
		            
		           
		            Log.d("upgrade", "about  "+GNCalendarAboutActivity.this.getApplicationContext().getPackageName());
		        }
		        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
			}
		});

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
		
		((CalendarApplication) getApplication()).registerVersionCallback(this);
		
        Log.d("upgrade", "onResume     "+"1111111111111111111111");
        
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
        
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		//Gionee <Author: lihongyu> <2013-04-11> add for CR000000 begin
		
		((CalendarApplication) getApplication()).unregisterVersionCallback(this);
		
        Log.d("upgrade", "unregisterVersionCallback2");
        //Gionee <Author: lihongyu> <2013-04-11> add for CR000000 end
	}

	public void setTitle() {
		
		aboutVersion = (TextView) findViewById(R.id.tv_about_version);
		
		Statistics statistics = new Statistics();
		try {
			
			statistics.getInfos(this);

			String versionTitle = getResources().getString(R.string.gn_about_version);
			versionTitle = versionTitle + statistics.getVersionName();
			aboutVersion.setText(versionTitle);

		} catch (Exception e) {
			// TODO: handle exception
			Log.d("version","AboutActivity---setVersion!");
			e.printStackTrace();
		}
	}
	

//	@Override
//	public void onClick(View v) {
//
//		switch (v.getId()) {
//		case R.id.about_update_button:
//			boolean networkBool = judgeNetwork();
//			if(!networkBool){
//				return;
//			}
//			mCheckPogressDialog = BaseHelper.showProgress(this,
//					getString(R.string.pay_show_title),
//					getString(R.string.message_update_check), false, false);
//			mGnUpgrade = new GnUpgrade(this, handler);
//			mGnUpgrade.startCheck();
//			break;
//		default:
//
//			break;
//		}
//
//	}

		
//		@Override
//		public boolean onOptionsItemSelected(MenuItem item) {
//			switch (item.getItemId()) {
//			case android.R.id.home:
//				finish();
//				break;
//			default:
//				break;
//			}
//			return true;
//		}
	
}


//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 end