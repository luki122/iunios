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

package com.aurora.setupwizard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;
import com.aurora.setupwizard.HanziToPinyin.Token;
import com.aurora.setupwizard.utils.Constants;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class AuroraInputMethodPickerActivity extends BaseActivity implements
		SetupWizardNavBar.NavigationBarListener {

	private ImageView mImgInputMethodIcon;
	private TextView mTvInfoTitle;
	// private TextView mTvNext;

	private InputMethodManager mImm;
	private List<InputMethodInfo> mImis;

	private String[] entries;
	private String[] values;
	private boolean mIsIUNIInputMethod = false;
	private ListView mListView;
	private ListAdapter mAdapter;
	private List<InputMethodInformation> mMInputMethodInfoList;

	private boolean hasChange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		setAuroraContentView(R.layout.aurora_inputmethod_picker_layout,
				AuroraActionBar.Type.Empty);
		getAuroraActionBar().setVisibility(View.GONE);
		// setAuroraContentView(R.layout.aurora_inputmethod_picker_layout);
		// getAuroraActionBar().setTitle(R.string.aurora_inputmethod_picker_title);
		initView();
		initData();
		initEvent();
	}

	@Override
	public void onResume() {
		super.onResume();
		com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);
	}

	private void initEvent() {
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < values.length) {
					Settings.Secure.putString(getContentResolver(),
							Settings.Secure.DEFAULT_INPUT_METHOD,
							values[position]);

					// hasChange = !hasChange;
					// mTvNext.setText(hasChange ? R.string.next :
					// R.string.skip);
				}
			}
		});

	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.inputmethod_list);
		mImgInputMethodIcon = (ImageView) findViewById(R.id.iv_icon);
		mTvInfoTitle = (TextView) findViewById(R.id.tv_info_title);
		// mTvNext = (TextView) findViewById(R.id.tv_next);

	}

	private void initData() {

		mImgInputMethodIcon.setImageResource(R.drawable.ic_keyboard);

		mTvInfoTitle.setText(R.string.inputmethod_info);

		mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mImis = mImm.getEnabledInputMethodList();

		PackageManager pm = getPackageManager();

		mMInputMethodInfoList = new ArrayList<InputMethodInformation>();

		int n = (mImis == null ? 0 : mImis.size());
		entries = new String[n];
		values = new String[n];
		for (int i = 0; i < n; i++) {
			InputMethodInformation inputMethodInformation = new InputMethodInformation();
			inputMethodInformation.label = mImis.get(i).loadLabel(pm)
					.toString();
			inputMethodInformation.value = mImis.get(i).getId();
			mMInputMethodInfoList.add(inputMethodInformation);
		}

		Collections.sort(mMInputMethodInfoList, sDisplayNameComparator);

		for (int i = 0; i < mMInputMethodInfoList.size(); i++) {
			entries[i] = mMInputMethodInfoList.get(i).label;
			values[i] = mMInputMethodInfoList.get(i).value;
		}

		// String current = Settings.Secure.getString(getContentResolver(),
		// Settings.Secure.DEFAULT_INPUT_METHOD);
		String systemDefaultInputMethod = "com.sohu.inputmethod.sogouoem/.SogouIME";
		for (int i = 0; i < n; i++) {
			if (systemDefaultInputMethod.equals(mImis.get(i).getId())) {
				mIsIUNIInputMethod = true;
			}
		}
		/*
		 * if(mIsIUNIInputMethod) { for(int i = 0; i < n; i++) {
		 * if(systemDefaultInputMethod.equals(mImis.get(i).getId())) {
		 * entries[0] = mImis.get(i).loadLabel(pm).toString(); values[0] =
		 * mImis.get(i).getId(); } } if(n > 1) { int count = 1; for(int i = 0; i
		 * < n; i++) {
		 * if(!systemDefaultInputMethod.equals(mImis.get(i).getId())) {
		 * entries[count] = mImis.get(i).loadLabel(pm).toString(); values[count]
		 * = mImis.get(i).getId(); count++; } } } }
		 */
		if (mIsIUNIInputMethod) {
			for (int i = 0; i < n; i++) {
				if (systemDefaultInputMethod.equals(mMInputMethodInfoList
						.get(i).value)) {
					entries[0] = mMInputMethodInfoList.get(i).label;
					values[0] = mMInputMethodInfoList.get(i).value;
				}
			}
			if (n > 1) {
				int count = 1;
				for (int i = 0; i < n; i++) {
					if (!systemDefaultInputMethod.equals(mMInputMethodInfoList
							.get(i).value)) {
						entries[count] = mMInputMethodInfoList.get(i).label;
						values[count] = mMInputMethodInfoList.get(i).value;
						count++;
					}
				}
			}
		}

		mAdapter = new ArrayAdapter<String>(this,
				R.layout.aurora_simple_list_item_single_choice,
				android.R.id.text1, entries);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(mAdapter);

		String currentInputMethodId = Settings.Secure.getString(
				getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		if (TextUtils.isEmpty(currentInputMethodId))
			return;
		for (int i = 0; i < values.length; i++) {
			if (currentInputMethodId.equals(values[i])) {
				mListView.setItemChecked(i, true);
			}
		}
	}

	public static String getSpell(String str) {
		StringBuffer buffer = new StringBuffer();
		if (str != null && !str.equals("")) {
			char[] cc = str.toCharArray();
			for (int i = 0; i < cc.length; i++) {
				ArrayList<Token> mArrayList = HanziToPinyin.getInstance().get(
						String.valueOf(cc[i]));
				if (mArrayList.size() > 0) {
					String n = mArrayList.get(0).target;
					buffer.append(n);
				}
			}
		}
		String spellStr = buffer.toString();
		return spellStr.toUpperCase();
	}

	@Override
	public void onNavigationBarCreated(SetupWizardNavBar bar) {

	}

	@Override
	public void onNavigateBack() {
		onBackPressed();
	}

	@Override
	public void onNavigateNext() {

		// Intent intent = new Intent();
		// intent.setData(Uri.parse("openaccount://com.aurora.account.login"));
		// intent.putExtra("type", 2);
		// startActivity(intent);
		// intent.setDataAndType(data, type)

//		String fingerStr = SystemProperties.get("ro.gn.fingerprint.support",
//				"no");
//		if (fingerStr.equals("no")) {
//			// Intent intent = new Intent(this,
//			// FingerPrintSettingActivity.class);
//			Intent intent = new Intent("com.aurora.setupwizard.complete");
//			startActivity(intent);
//		} else {
//			Intent intent = new Intent("com.aurora.setupwizard.settingfingerprint");
//			startActivity(intent);
//		}
goNext();
	}
	
	/**
	 * 跳到下一页
	 */
	public void goNext(){
		
		//是否指纹支持
		String fingerStr = SystemProperties.get("ro.gn.fingerprint.support",
				"no");
		if (fingerStr.equals("no")) {
			//是否有应用推荐的列表
			File file = new File("/data/apk_recommend/appdatas.json");//这个路径有可能悔改
			boolean exists = file.exists();//根据此文件来判断是否跳到推荐页
			if(exists){
				//应用推荐页
				Intent intent = new Intent("com.aurora.setupwizard.apprecommend");
				startActivity(intent);
			}else{
				//应用完成页
				Intent intent = new Intent("com.aurora.setupwizard.complete");
				startActivity(intent);
			}
		} else {
			//指纹页
			Intent intent = new Intent("com.aurora.setupwizard.settingfingerprint");
			startActivity(intent);
		}
	}

	class InputMethodInformation {
		String label;
		String value;
	}

	private final static Comparator<InputMethodInformation> sDisplayNameComparator = new Comparator<InputMethodInformation>() {
		public final int compare(InputMethodInformation a,
				InputMethodInformation b) {
			return collator.compare(getSpell(a.label), getSpell(b.label));
		}

		private final Collator collator = Collator.getInstance();
	};
}
