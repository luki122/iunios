package com.aurora.voiceassistant.updateinterface;

import android.R.integer;
import android.view.ViewGroup;

public interface LayoutUpdateListener {	
	void registLayoutUpdateListener(ViewGroup viewGroup);
	void unRegistLayoutUpdateListener(ViewGroup viewGroup);
	void unRegistAllLayout();
	void updateVoiceButtonAlpha(boolean flag);
	
	void addLayoutIdMap(String id, String urString);
	void clearLayoutIdMap();
	void updateMessageSentState(String id, String urString, int errorCode);
}
