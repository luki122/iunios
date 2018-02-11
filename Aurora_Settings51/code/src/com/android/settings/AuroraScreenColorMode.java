package com.android.settings;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import android.os.SystemProperties;

public class AuroraScreenColorMode extends AuroraActivity implements
		AdapterView.OnItemClickListener {

	private AuroraActionBar auroraActionBar;

	private ListView mScreenTimeoutList;
	private String[] mScreenColorModeEntries;
	private String[] mScreenColorModeValues;

	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setAuroraContentView(R.layout.aurora_screen_color_mode,
				AuroraActionBar.Type.Normal);
		setAuroraActionbarSplitLineVisibility(View.GONE);
		mScreenColorModeEntries = getResources().getStringArray(
				R.array.aurora_screen_color_mode_entries);
		mScreenColorModeValues = getResources().getStringArray(
				R.array.aurora_screen_color_mode_values);
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(getResources().getString(
				R.string.color_temp)); // title
		auroraActionBar
				.setmOnActionBarBackItemListener(auroActionBarItemBackListener);

		ArrayAdapter adapter = new ArrayAdapter(this,
				R.layout.aurora_screen_timeout_listitem,
				mScreenColorModeEntries);

		mScreenTimeoutList = (ListView) findViewById(R.id.screen_color_mode);
		mScreenTimeoutList.setAdapter(adapter);
		mScreenTimeoutList.setOnItemClickListener(this);
		mScreenTimeoutList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);// .CHOICE_MODE_SINGLE);

		getColorModeValue();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mScreenTimeoutList.setItemChecked(getInitItemPosition(), true);
	}

	private int getColorModeValue() {
		int value = SystemProperties.getInt(
				MiraVisionJni.GAMMA_INDEX_PROPERTY_NAME, 10);
		if (value == 10) {
			MiraVisionJni.setGammaIndex(7);
			value = 7;
		}
		return value;
	}

	private void setColorModeValue(int index) {
		MiraVisionJni.setGammaIndex(index);
	}

	private int getInitItemPosition() {
		int value = getColorModeValue();
		for (int i = 0; i < mScreenColorModeValues.length; i++) {
			if (value == Integer.parseInt(mScreenColorModeValues[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		int value = Integer.parseInt(mScreenColorModeValues[position]);
		setColorModeValue(value);
//		finish();
	}

	private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener =
            new OnAuroraActionBarBackItemClickListener() {
		public void onAuroraActionBarBackItemClicked(int itemId) {
			switch (itemId) {
			case -1:
				finish();
				break;
			default:
				break;
			}
		}
	};

}