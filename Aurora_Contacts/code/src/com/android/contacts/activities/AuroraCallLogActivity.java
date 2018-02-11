package com.android.contacts.activities;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.calllog.AuroraCallLogAdapterV2;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.calllog.AuroraCallLogFragmentV2.IAuroraCallLogFragment;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.SharedPreferencesUtil;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.android.contacts.R;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.NormalAuroraActionBarItem;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraAlertDialog; 

//aurora change liguangyu 20131113 for start
public class AuroraCallLogActivity extends Activity{
	//aurora change liguangyu 20131113 for end
//	private AuroraDialActivityV3 activityparent;
	private AuroraCallLogFragmentV2 mCallLogFragment;
//	private static final int AURORA_EDIT_CALLLOG = 1;
//	private AuroraActionBar actionBar;
	private static String TAG="liyang-AuroraCallLogActivity";
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "AuroraCallLogActivity onCreate");		

//		setAuroraContentView(R.layout.aurora_call_log_view,
//				mIsPrivate ? AuroraActionBar.Type.Normal: AuroraActionBar.Type.Empty);
		setContentView(R.layout.aurora_call_log_view);
//		actionBar = getAuroraActionBar();
//		getAuroraActionBar().setVisibility(View.GONE);
		FragmentManager fm = getFragmentManager();

		// Create the list fragment and add it as our sole content.
		Fragment frag = fm.findFragmentById(R.id.call_log_frag);
		if (null != frag && frag instanceof AuroraCallLogFragmentV2) {
			mCallLogFragment = (AuroraCallLogFragmentV2) frag;
		} else if (mCallLogFragment == null) {
			mCallLogFragment = new AuroraCallLogFragmentV2();
			getFragmentManager().beginTransaction()
			.add(R.id.call_log_frag, mCallLogFragment).commitAllowingStateLoss();
		}
		//		mCallLogFragment.setPrivate(mIsPrivate);
//		mCallLogFragment.setIAuroraCallLogFragment(myIAuroraCallLogFragment);

//		if(mIsPrivate) {
//			ContactsApplication.mPrivacyActivityList.add(this);
//		}
		
//		mCallLogFragment.getAuroraListView().setOnTouchListener(this);
				
		
	}


	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.d("liyang","onKeyDown1");
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_MENU:{
////			setAuroraMenuItems(R.menu.aurora_action);
//			break;
//		}
//		case KeyEvent.KEYCODE_BACK: {
//			
//			//aurora modify liguangyu 20140423 for BUG #4491 start
//			if (actionBar != null && 
//					(actionBar.auroraIsExitEditModeAnimRunning() || actionBar.auroraIsEntryEditModeAnimRunning() 
//							|| (mCallLogFragment.getAdapter() != null && mCallLogFragment.getAdapter().isIs_listitem_changing()))) {
//				//aurora modify liguangyu 20140423 for BUG #4491 end
//				return true;
//			}
//
//			//aurora add liguangyu 20131108 for BUG #508 start
//			try {
//				boolean deleteIsShow = mCallLogFragment.getAuroraListView().auroraIsRubbishOut();
//				if (deleteIsShow) {
//					mCallLogFragment.getAuroraListView().auroraSetRubbishBack();
//					return true;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			//aurora add liguangyu 20131108 for BUG #508 end
//
//			if (mCallLogFragment.getEditMode() && null != actionBar) {
////				onSetViewPagerListener.setVisibity(true);
////				actionBar.setVisibility(View.GONE);
////				AuroraCallLogFragmentV2.mToolBar.setVisibility(View.GONE);
//				
//				mCallLogFragment.switch2NormalMode();
//				mCallLogFragment.updateMenuItemState(false);
//				mCallLogFragment.setBottomMenuEnable(false);
//				actionBar.setShowBottomBarMenu(false);
//				actionBar.showActionBarDashBoard();			
//				
//
//				return true;
//			}
//
//			break;
//		}
//
//		default: {
//
//		}
//
//		}
//		//        moveTaskToBack(true);
//		return super.onKeyDown(keyCode, event);
//	}
//
//	@Override
//	protected void onStop() {
//		//		if (mCallLogFragment.getEditMode()) {
//		//			updateMenuItemState(false);
//		//			 Log.v(TAG, "setShowBottomBarMenu3");
//		//			getAuroraActionBar().setShowBottomBarMenu(false);
//		//			getAuroraActionBar().showActionBarDashBoard();
//		//			setBottomMenuEnable(false);
//		//			mCallLogFragment.switch2NormalMode();
//		//		}
//		//aurora modify liguangyu 20140410 for #4037 start
//		//		if(mCleanCallLogDialog != null) {
//		//			mCleanCallLogDialog.dismiss();
//		//		}
//		//aurora modify liguangyu 20140410 for #4037 end
//		super.onStop();
//		System.out.println("onStop()1");
//	};
//
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//		System.out.println("onPause()1");
//	}

	private IAuroraCallLogFragment myIAuroraCallLogFragment = new IAuroraCallLogFragment() {

		@Override
		public void setTabWidget(int visible) {
			if(!mIsPrivate) {
				//		    	activityparent.setTabWidgetVisible(visible);
			}
		}
	};

	public void animationBeforeSetTab() {
		mCallLogFragment.animationBeforeSetTab();
	}

	public void animationAfterSetTab() {
		mCallLogFragment.animationAfterSetTab();
	}

	protected boolean mIsPrivate = false;

	public boolean isPrivate(){
		return mIsPrivate;
	}

//	@Override
//	protected void onResume() {
//		Log.d("liyang", "AuroraCallLogActivity onResume");
//		super.onResume();
////		SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK, this);
//
//	}

//	protected void onDestroy() {
//		Log.v(TAG, "onDestroy");
//		super.onDestroy();
//		if (mIsPrivate) {
//			ContactsApplication.mPrivacyActivityList.remove(this);
//		}
//	}
//
//
//
//
//
//
//
//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		// TODO Auto-generated method stub
//		return false;
//	}


	// 	@Override
	//	  public void onBackPressed() {
	//	   	 activityparent = (AuroraDialActivityV1) getParent();
	//	   	 if(activityparent != null) {
	//	   		 activityparent.moveTaskToBack(true);
	//	   	 } else {
	//		     super.onBackPressed();
	//	   	 }
	//	  }
}
