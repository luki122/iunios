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

package com.android.settings.inputmethod;

import com.android.settings.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import java.util.List;
import java.util.ArrayList;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import com.android.settings.Utils;

public class AuroraInputMethodPickerActivity extends AuroraActivity {

	private AuroraActivity mActivity;

	private InputMethodManager mImm;
    private List<InputMethodInfo> mImis;

	private String[] entries;
	private String[] values;
	private boolean mIsIUNIInputMethod = false;
	
	class InputMethodInformation 
	{
		String label;
		String value;
	}

	private final static Comparator<InputMethodInformation> sDisplayNameComparator
            = new Comparator<InputMethodInformation>() {
         public final int
         compare(InputMethodInformation a, InputMethodInformation b) {
             return collator.compare(Utils.getSpell(a.label), Utils.getSpell(b.label));
         }
 
         private final Collator collator = Collator.getInstance();
     };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_inputmethod_picker_layout,  AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.aurora_inputmethod_picker_title);

		mActivity = this;

		mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mImis = mImm.getEnabledInputMethodList();

        PackageManager pm = getPackageManager();

		final List<InputMethodInformation> mInputMethodInfoList = new ArrayList<InputMethodInformation>();

        int n = (mImis == null ? 0 : mImis.size());
        entries = new String[n];
        values = new String[n];
        for (int i = 0; i < n; i++) {
			InputMethodInformation inputMethodInformation = new InputMethodInformation();
            inputMethodInformation.label = mImis.get(i).loadLabel(pm).toString();
			inputMethodInformation.value = mImis.get(i).getId();
			mInputMethodInfoList.add(inputMethodInformation);
        }
	
		Collections.sort(mInputMethodInfoList, sDisplayNameComparator);
		
		for(int i = 0; i < mInputMethodInfoList.size(); i++) {
			entries[i] = mInputMethodInfoList.get(i).label;
            values[i] = mInputMethodInfoList.get(i).value;
		}
	
		//String current = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		String systemDefaultInputMethod = "com.sohu.inputmethod.sogouoem/.SogouIME";
		for(int i = 0; i < n; i++) {
			if(systemDefaultInputMethod.equals(mImis.get(i).getId())) {
				mIsIUNIInputMethod = true;
			}
		}
/*
		if(mIsIUNIInputMethod) {
			for(int i = 0; i < n; i++) {
				if(systemDefaultInputMethod.equals(mImis.get(i).getId())) {
					entries[0] = mImis.get(i).loadLabel(pm).toString();
		        	values[0] = mImis.get(i).getId();
				}
			}
			if(n > 1) {
				int count = 1;
				for(int i = 0; i < n; i++) {
					if(!systemDefaultInputMethod.equals(mImis.get(i).getId())) {
					entries[count] = mImis.get(i).loadLabel(pm).toString();
				    values[count] = mImis.get(i).getId();
					count++;
					}
				}
			}
		}
*/
		if(mIsIUNIInputMethod) {
			for(int i = 0; i < n; i++) {
				if(systemDefaultInputMethod.equals(mInputMethodInfoList.get(i).value)) {
					entries[0] = mInputMethodInfoList.get(i).label;
            		values[0] = mInputMethodInfoList.get(i).value;
				}
			}
			if(n > 1) {
				int count = 1;
				for(int i = 0; i < n; i++) {
					if(!systemDefaultInputMethod.equals(mInputMethodInfoList.get(i).value)) {
					entries[count] = mInputMethodInfoList.get(i).label;
            		values[count] = mInputMethodInfoList.get(i).value;
					count++;
					}
				}
			}
		}
		
		ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.aurora_simple_list_item_single_choice,
				android.R.id.text1, entries);

		final ListView listView = (ListView) findViewById(R.id.inputmethod_list);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < values.length) {
					Settings.Secure.putString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD,
							values[position]);

					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				}
			}
		});

		String currentInputMethodId = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		if (TextUtils.isEmpty(currentInputMethodId)) return;
		for (int i = 0; i < values.length; i++) {
			if (currentInputMethodId.equals(values[i])) {
				listView.setItemChecked(i, true);
			}
		}
	}

}
