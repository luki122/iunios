package com.gionee.autommi;

import android.util.Log;

public class HeadSetLoopTest extends MicTest {

	private static final String TAG = "HeadSetLoopTest";

	@Override
	protected void chooseMic() {
		// TODO Auto-generated method stub
		am.setParameters("MMIMic=5");
	}

	/*
	@Override
	protected void setAudioParameters() { 
		Log.d(TAG, "--------setAudioParameters dumb------------");
	}
	*/
}
