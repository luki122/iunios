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

package com.android.settings.wifi;

import com.android.settings.R;
import com.android.settings.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class WifiSleepPolicyActivity extends AuroraActivity {

	private AuroraActivity mActivity;
	private String[] entries;
	private String[] values;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_wifi_sleep_policy,  AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.aurora_wifi_sleep_policy_title);

		mActivity = this;
		int entriesResId = Utils.isWifiOnly(this) ? R.array.aurora_wifi_sleep_policy_entries_wifi_only :
				R.array.aurora_wifi_sleep_policy_entries;
		entries = getResources().getStringArray(entriesResId);
		values = getResources().getStringArray(R.array.wifi_sleep_policy_values);

		ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.aurora_simple_list_item_single_choice,
				android.R.id.text1, entries);

		final ListView listView = (ListView) findViewById(R.id.sleep_policy_list);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position <= values.length) {
					Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
	                        Integer.parseInt(values[position]));

					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				}
			}
		});

		int value = Settings.Global.getInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(String.valueOf(value))) {
				listView.setItemChecked(i, true);
			}
		}
	}

}