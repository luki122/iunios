package com.gionee.autommi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.media.AudioManager;

public class CustMicRoute extends BaseActivity {
	private static final String EXTRA_STATE = "state";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		Intent it = this.getIntent();
		String state = it.getStringExtra(EXTRA_STATE);
		if ("yes".equals(state))
			am.setParameters("AUTOMMI_HEADSET_NOMIC=1");
		else if ("no".equals(state))
			am.setParameters("AUTOMMI_HEADSET_NOMIC=0");
		else {
			
		}
		this.finish();	
	}
}
