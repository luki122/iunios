package com.android.contacts.activities;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.calllog.AuroraCallLogAdapterV2;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.android.contacts.R;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
public class AuroraPrivateCallLogActivity extends AuroraActivity {
	private AuroraCallLogFragmentV2 mCallLogFragment;
	private static String TAG="liyang-AuroraPrivateCallLogActivity";
	protected boolean mIsPrivate = true;
	private AuroraActionBar actionBar;
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "AuroraPrivateCallLogActivity onCreate");		
		mIsPrivate = true;
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_call_log_view,
				mIsPrivate ? AuroraActionBar.Type.Normal: AuroraActionBar.Type.Empty);
		
		actionBar = getAuroraActionBar();
		
		actionBar.setTitle(R.string.private_call_log);	

		FragmentManager fm = getFragmentManager();

		Fragment frag = fm.findFragmentById(R.id.call_log_frag);
		if (null != frag && frag instanceof AuroraCallLogFragmentV2) {
			mCallLogFragment = (AuroraCallLogFragmentV2) frag;
		} else if (mCallLogFragment == null) {
			mCallLogFragment = new AuroraCallLogFragmentV2();
			getFragmentManager().beginTransaction()
			.add(R.id.call_log_frag, mCallLogFragment).commitAllowingStateLoss();
		}
		mCallLogFragment.setPrivate(mIsPrivate);

		if(mIsPrivate) {
			ContactsApplication.mPrivacyActivityList.add(this);
		}
	}

	public boolean isPrivate(){
		return mIsPrivate;
	}
}
