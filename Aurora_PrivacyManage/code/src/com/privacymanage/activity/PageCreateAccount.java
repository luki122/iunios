package com.privacymanage.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import com.privacymanage.utils.LogUtils;
import com.privacymanage.utils.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraTextView;

public class PageCreateAccount extends FounderPage {

	/**
	 * 
	 */
	public PageCreateAccount() {
		super();
		mLayoutResId = ResIdMan.LAYOUT_FILE_INPUT_PASSWORD;
		mStringActionBarTitle = ResIdMan.STRING_CREATE_PRIVACY_ACCOUNT;
		mStringNextStepResId = ResIdMan.STRING_NEXT_STEP;
		mPageId = PAGE_ID_CREATE_ACCOUNT;
		LogUtils.printWithLogCat("vanr", "PageCreateAccount: enter");
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月24日 上午11:40:31 .
	 * @return
	 */
	protected int loadFirstBitOfPassword() {
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.loadFirstBitOfPassword: enter");
		Random random = new Random();
		int firstBit = random.nextInt(10);
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.loadFirstBitOfPassword: exit");
		return firstBit;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onCreate: enter");
		int firstBit = loadFirstBitOfPassword();
		TextView tvMostLeftBit = (TextView)findViewById(ResIdMan.TEXTVIEW_MOST_LEFT_BIT);
		tvMostLeftBit.setText(Integer.toString(firstBit));
		AccountCreateInfo.mFirstBit = firstBit;
		
		final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
		final EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		aetPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setErrorInfoText(ResIdMan.STRING_NULL);
				ivPasswordFrame.setSelected(false);
			}
			
		});
		
		//InputMethodManager imm = ( InputMethodManager )getSystemService( Context.INPUT_METHOD_SERVICE );
//		imm.showSoftInput(aetPassword, InputMethodManager.SHOW_FORCED);
//		Utils.showSoftInput(this, aetPassword);
		
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onCreate: exit");
		return;
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onResume: enter");
		EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		Utils.showSoftInput(this, aetPassword);
		super.onResume();
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onResume: exit");
	}

	/**
	 * 
	 * Vulcan created this method in 2014年10月8日 下午4:21:11 .
	 * @param password
	 * @return
	 */
	protected boolean passwordLengthIsGood(String password) {
		int length = password.length();
		return length >= 4 && length <= 10;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月8日 下午4:29:36 .
	 * @param resid
	 */
	protected void setErrorInfoText(int resid) {
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.setErrorInfoText: enter");
		AuroraTextView tvErrorMsg = (AuroraTextView)findViewById(ResIdMan.TEXTVIEW_ERROR_MSG);
		tvErrorMsg.setText(resid);
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.setErrorInfoText: exit");
	}
	
	@Override
	public void onClick(View v) {
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onClick: enter");
		EditText aetPassword = (EditText)findViewById(ResIdMan.EDITTEXT_PASSWORD);
		String password = aetPassword.getText().toString();
		if(!passwordLengthIsGood(password)) {
			final ImageView ivPasswordFrame = (ImageView)findViewById(ResIdMan.IMAGEVIEW_PASSWORD_FRAME);
			ivPasswordFrame.setSelected(true);
			setErrorInfoText(ResIdMan.STRING_PASSWORD_LENGTH_NOT_GOOD);
			LogUtils.printWithLogCat("vanr", "PageCreateAccount.onClick: exit");
			return;
		}
		
		String finalPassword = new String(AccountCreateInfo.mFirstBit + password);
		if(UnderlyingWrapper.passwordExists(finalPassword)) {
			//UnderlyingWrapper.sendConflictEmail(finalPassword,null);
			forceStopPackage(this);
			LogUtils.printWithLogCat("vanr", "PageCreateAccount.onClick: exit");
			return;
		}

		setErrorInfoText(ResIdMan.STRING_NULL);
		AccountCreateInfo.mPassword = password;
		super.onClick(v);
		LogUtils.printWithLogCat("vanr", "PageCreateAccount.onClick: exit");
		return;
	}
	

	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 下午1:51:53 .
	 * @param context
	 */
	protected void forceStopPackage(final Context context){
		LogUtils.printWithLogCat("vanr", "forceStopPackage: enter");
		
		//finish();
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
				Method method = null;
				try {
					method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
					method.invoke(mActivityManager, context.getPackageName());
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
		});
		LogUtils.printWithLogCat("vanr", "forceStopPackage: exit");
		return;
	}

}
