package com.android.auroramusic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

import com.android.auroramusic.dts.DtsEffects;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;
import com.android.music.R;

public class HeasetSelectActivity extends AuroraActivity {

	private static final String TAG = "HeasetSelectActivity";
	public static final int AURORA_HEADSET_CHANGDE = 10010;
	private int mHeadSetPos = 0;
	private boolean mbOnclick = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setAuroraContentView(R.layout.headset_select);
		View id_rock_headset = findViewById(R.id.id_rock_headset);
		View id_rock_headset_line = findViewById(R.id.id_rock_headset_line);
		View id_iuni_twist_tone = findViewById(R.id.id_iuni_twist_tone);
		View id_iuni_twist_tone_line = findViewById(R.id.id_iuni_twist_tone_line);
		if (AuroraMusicUtil.isIndiaVersion()) {
			id_rock_headset.setVisibility(View.GONE);
			id_rock_headset_line.setVisibility(View.GONE);
			id_iuni_twist_tone.setVisibility(View.GONE);
			id_iuni_twist_tone_line.setVisibility(View.GONE);
		}else {
			id_rock_headset.setVisibility(View.VISIBLE);
			id_rock_headset_line.setVisibility(View.VISIBLE);
			id_iuni_twist_tone.setVisibility(View.VISIBLE);
			id_iuni_twist_tone_line.setVisibility(View.VISIBLE);
		}
		if(Globals.currentMode.equalsIgnoreCase("IUNI U0003")){
			 ((RadioButton)id_rock_headset).setText(getString(R.string.aurora_iuni_new_headset));
		}
		mbOnclick = false;
		onLoadData();

		AuroraActionBar mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setTitle(R.string.aurora_headset_select);
		initview();

		mDtsEffects = DtsEffects.getInstance(HeasetSelectActivity.this);
	}

	private void onLoadData() {
		mHeadSetPos = MusicUtils.getIntPref(this,
				AuroraSoundControl.AURORA_DATA_DTS_HEADSET_STATUS, 0);
		return;
	}

	private void initview() {
		RadioGroup group = (RadioGroup) findViewById(R.id.id_group);
		group.setOnCheckedChangeListener(mOnCheckedChangeListener);

		int size = group.getChildCount();
		int num = size / 2;
		int index = 0;
		if (mHeadSetPos < 0) {
			index = 0;
		} else if (mHeadSetPos >= num - 1) {
			index = size - 2;
		} else {
			index = 2 * mHeadSetPos;
		}
		LogUtil.d(TAG, "xh----initview index:"+index+",mHeadSetPos:"+mHeadSetPos+",size:"+size);
		RadioButton checkedbuttoButton = (RadioButton) group.getChildAt(index);
		if (checkedbuttoButton != null) {
			checkedbuttoButton.setChecked(true);
		}
	}

	private void onSaveData() {
		// Log.i(TAG, "zll ---- onSaveData mHeadSetPos:"+mHeadSetPos);
		LogUtil.d(TAG, " ---- onSaveData mHeadSetPos:"+mHeadSetPos);
		MusicUtils.setIntPref(this,
				AuroraSoundControl.AURORA_DATA_DTS_HEADSET_STATUS, mHeadSetPos);
		return;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mbOnclick) {
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
		}
	}

	private DtsEffects mDtsEffects;

	private void setHeaderDtsEffect(int i) {
		if (mDtsEffects == null) {
			return;
		}
		LogUtil.d(TAG, "--------setHeaderDtsEffect i:"+i);
		mDtsEffects.setHeadphoneDTSParams(i);
		mHeadSetPos = i;
		onSaveData();
		return;
	}

	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			switch (arg1) {
			case R.id.id_null:
				setHeaderDtsEffect(0);
				break;
			case R.id.id_rock_headset:
				setHeaderDtsEffect(1);
				break;
			case R.id.id_iuni_twist_tone:
				setHeaderDtsEffect(2);
				break;
			case R.id.id_iphone_earpodes:
				setHeaderDtsEffect(3);
				break;
			case R.id.id_akg450:
				setHeaderDtsEffect(4);
				break;
			case R.id.id_urbeats:
				setHeaderDtsEffect(5);
				break;
			default:
				break;
			}

			mbOnclick = true;
		}

	};
}
