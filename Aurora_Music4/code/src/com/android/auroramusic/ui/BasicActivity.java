package com.android.auroramusic.ui;

import android.os.Bundle;
import aurora.app.AuroraActivity;
// import com.android.music.R;

import com.android.auroramusic.util.AuroraMusicUtil;

public class BasicActivity extends AuroraActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AuroraMusicUtil.statusBarBackgroundTransparent(this);
	}

}
